#!/usr/bin/perl

# takes a file path to a csv file as an argument and then adds the users from that file to nakamura
$clean=100;
$counter_time=0;
$counter=0;
$first=time;

$file = $ARGV[0];
$first = $ARGV[1];
$last = $ARGV[2];

open (F, $file) || die ("Could not open $file!");
open (FN, $first) || die ("Could not open $first!");
open (LN, $last) || die ("Could not open $last!");

while ($line = <F>)
{
  ($name,$password) = split ',', $line;
  
  $firstName = <FN>;
  if ( ! $firstName ) {
    close(FN);
    open (FN, $first) || die ("Could not open $first!");
    $firstName = <FN>;
  }
  $lastName = <LN>;
  if ( ! $lastName ) {
    close(LN);
    open (LN, $last) || die ("Could not open $last!");
    $lastName = <LN>;
  }
  chomp($firstName);
  chomp($lastName);
  chomp($name);
  chomp($password);
  $firstName =~ s/(\w+)/\u\L$firstName/g;
  $lastName =~ s/(\w+)/\u\L$lastName/g;
  
  print "$name,$password,$firstName,$lastName\n";
}


close (F);
close (FN);
close (LN);



