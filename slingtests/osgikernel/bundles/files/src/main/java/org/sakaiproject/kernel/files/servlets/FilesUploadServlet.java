/*
 * Licensed to the Sakai Foundation (SF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The SF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.sakaiproject.kernel.files.servlets;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestParameter;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.jcr.resource.JcrResourceConstants;
import org.sakaiproject.kernel.api.files.FileUtils;
import org.sakaiproject.kernel.api.files.FilesConstants;
import org.sakaiproject.kernel.util.ExtendedJSONWriter;
import org.sakaiproject.kernel.util.JcrUtils;
import org.sakaiproject.kernel.util.PathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

/**
 * Create a file
 * 
 * @scr.component metatype="no" immediate="true" label="FilesUploadServlet"
 *                description="Servlet to allow uploading of files to the store."
 * @scr.property name="service.description" value="Uploads files to the store."
 * @scr.property name="service.vendor" value="The Sakai Foundation"
 * @scr.service interface="javax.servlet.Servlet"
 * @scr.property name="sling.servlet.paths" value="/_user/files/incoming"
 * @scr.property name="sling.servlet.methods" value="POST"
 */
public class FilesUploadServlet extends SlingAllMethodsServlet {

  private static final long serialVersionUID = -2582970789079249113L;

  public static final Logger LOG = LoggerFactory.getLogger(FilesUploadServlet.class);

  @Override
  protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
      throws ServletException, IOException {
    Session session = request.getResourceResolver().adaptTo(Session.class);
    LOG.info("Attempted upload for " + session.getUserID());
    ExtendedJSONWriter writer = new ExtendedJSONWriter(response.getWriter());
    try {
      writer.object();
      writer.key("files");
      writer.array();
      try {
        // Handle multi files
        RequestParameter[] files = request.getRequestParameters("Filedata");
        if (files == null) {
          response.sendError(400, "Missing file parameter.");
          return;
        }
        for (RequestParameter file : files) {
          createFile(session, file, writer);
        }
      } catch (RepositoryException e) {
        LOG.warn("Failed to create file.");
        e.printStackTrace();
        response.sendError(500, "Failed to save file.");
      }
      writer.endArray();
      writer.endObject();
      response.setStatus(HttpServletResponse.SC_CREATED);
    } catch (JSONException e) {
      LOG.warn("Failed to write JSON format.");
      response.sendError(500, "Failed to write JSON format.");
      e.printStackTrace();
    }

  }

  private void createFile(Session session, RequestParameter file,
      ExtendedJSONWriter writer) throws RepositoryException, IOException, JSONException {
    // Get the nescecary parameters/
    InputStream is = file.getInputStream();
    String fileName = file.getFileName();
    String contentType = file.getContentType();

    if (fileName != null && !fileName.equals("")) {

      // Create the path in the store to the file.
      String id = FileUtils.generateID();
      String path = PathUtils.toInternalHashedPath(FilesConstants.USER_FILESTORE, id, "");

      // Clean the filename.
      fileName = fileName.replaceAll("[^a-zA-Z0-9_-~\\.]", "");

      LOG.info("Trying to save file {} to {} for user {}", new Object[] { fileName, path,
          session.getUserID() });
      Node n = JcrUtils.deepGetOrCreateNode(session, path);
      n.setProperty(FilesConstants.SAKAI_USER, session.getUserID());
      n.setProperty(JcrResourceConstants.SLING_RESOURCE_TYPE_PROPERTY,
          FilesConstants.RT_SAKAI_FILE);
      n.setProperty(FilesConstants.SAKAI_ID, id);
      n.setProperty("sakai:filename", fileName); // Used to order in search query.
      session.save();

      // get content type
      if (contentType != null) {
        int idx = contentType.indexOf(';');
        if (idx > 0) {
          contentType = contentType.substring(0, idx);
        }
      }
      if (contentType == null) {
        contentType = "application/octet-stream";
      }

      // Create the file node.
      Node fileNode = n.addNode(fileName, "nt:file");

      // Create the content node.
      Node content = fileNode.addNode("jcr:content", "nt:resource");
      content.setProperty("jcr:lastModified", Calendar.getInstance());
      content.setProperty("jcr:mimeType", contentType);
      content.setProperty("jcr:data", is);
      session.save();

      writer.object();
      writer.key("id");
      writer.value(id);
      writer.key("filename");
      writer.value(fileName);
      writer.key("path");
      writer.value(FilesConstants.USER_FILESTORE + "/" + id);
      writer.endObject();
    }
  }

}
