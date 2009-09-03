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
package org.sakaiproject.kernel.proxy;

import org.apache.sling.api.SlingHttpServletResponse;
import org.sakaiproject.kernel.api.proxy.ProxyPostProcessor;
import org.sakaiproject.kernel.api.proxy.ProxyResponse;
import org.sakaiproject.kernel.util.IOUtils;

import java.io.IOException;
import java.util.Map.Entry;

/**
 *
 */
public class DefaultProxyPostProcessorImpl implements ProxyPostProcessor {

  /**
   * {@inheritDoc}
   * @throws IOException 
   * @see org.sakaiproject.kernel.api.proxy.ProxyPostProcessor#process(org.apache.sling.api.SlingHttpServletResponse, org.sakaiproject.kernel.api.proxy.ProxyResponse)
   */
  public void process(SlingHttpServletResponse response, ProxyResponse proxyResponse) throws IOException {
    for (Entry<String, String[]> h : proxyResponse.getResponseHeaders().entrySet()) {
      for (String v : h.getValue()) {
        response.setHeader(h.getKey(), v);
      }
    }
    int code = proxyResponse.getResultCode();
    response.setStatus(code);
    if (code > 199 && code < 300) {
      IOUtils.stream(proxyResponse.getResponseBodyAsInputStream(), response
          .getOutputStream());
    }    
  }

  /**
   * {@inheritDoc}
   * @see org.sakaiproject.kernel.api.proxy.ProxyPostProcessor#getName()
   */
  public String getName() {
    return "default";
  }

}