<?php

// protocol summary:
// (live Machineid) - indicates that the algorithm is working.
// (live Machineid NextComm) - same as previous, but indicates the expected time
// until the next communication. no communication for more than 'NextComm' time
// raises a yellow flag -- the actions to take are up to the server.
// (live Machineid NextComm NextCommCritical) - NextComm < NextCommCritical. same as previous but also
// indicates the critical no communication time. no communication for more time than NextCommCritical means
// serious problems. it is up to the server to take actions. -- the actions to take are defined by the server.
// (finish Machineid) - indicates that the algorithm as finished

if( isset($_REQUEST['inform'])) 
{
    $fname="statusserver.messages";
    $msg=$_REQUEST['inform'];
    $msg= time() . " " .$msg;
    $fh = fopen($fname, 'a') or die("fail");
    fwrite($fh, $msg."\n");
    fclose($fh);
    echo "ok";
} else 
{
    $fname="statusserver.messages"; // TODO - change to the processed version of the file.
    $fh = fopen($fname, 'r') or die("fail");
    $contents = fread($fh, filesize($fname));
    echo "<pre>";
    echo $contents;
    echo "</pre>";
}

?>
