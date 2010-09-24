<?php

// protocol summary:
// (live Machineid) - indicates that the algorithm is working.
// (live Machineid NextComm) - same as previous, but indicates the expected time
//  until the next communication. no communication for more than 'NextComm' time
//  indicates that something can be wrong. It is up to the server to take actions.
// (live Machineid NextComm NextCommCritical) - NextComm < NextCommCritical.
//  Same as previous but also indicates how much time without communications is
//  indicates a critical failure. No communication for more time than
//  NextCommCritical indicates serious problems. It is up to the server to take
//  actions.
// (crash Machineid) - indicates that the algorithm has crashed.
// (finish Machineid) - indicates that the algorithm as finished.

// NextComm and NextCommCritical must be in seconds.

$logfname="statusserver.messages";
$processedfname="statusserver.processedmessages";

if( isset($_REQUEST['inform'])) 
{
    $msg=$_REQUEST['inform'];
    $msg= time() . " " .$msg;
    $fh = fopen($logfname, 'a') or die("fail");
    fwrite($fh, $msg."\n");
    fclose($fh);
    echo "ok";
} else 
{
    process($logfname,$processedfname);
    $fh = fopen($processedfname, 'r') or die("fail");
    $contents = fread($fh, filesize($processedfname));
    $now=time();
    echo "<pre>";
    echo "current time:". strftime("%F %H:%M:%S") . " (" . $now . ")\n";
    echo $contents;
    echo "</pre>";
}

function process($infile,$outfile)
{
    $selected=array();
    $lines = file($infile);
    foreach ($lines as $line) 
    {
        $pline = myexplode($line);
	$selected[$pline['id']]=$pline;
    }

    $fh = fopen($outfile, 'w') or die("fail");
    foreach ($selected as $pline)
    {
        $expired=false;
        $criticalexpired=false;
        if(array_key_exists('nextcomm', $pline) && time() > $pline['time'] + $pline['nextcomm'])
        {
	    $expired=true;
	    if(array_key_exists('nextcommcritical', $pline) && time() > $pline['time'] + $pline['nextcommcritical'])
	        {
                    $criticalexpired=true;
	        }
        }
        $finalline = $pline['raw'] . ( ($expired) ? ( ($criticalexpired) ? '!!' : '!' ) : '' ) ."\n";
        fwrite($fh, $finalline);
    }
    fclose($fh);
}

function myexplode($line)
{
    $trimedline = trim($line);
    $replacedline = str_replace(array("(",")"),"",$trimedline);
    $exploded = explode(" ",$replacedline);
    $rv=array();
    $rv['raw']=$trimedline;
    $rv['time']=$exploded[0];
    $rv['type']=$exploded[1];
    $rv['id']=$exploded[2];
    if($rv['type']=="live") 
    {
    	if(array_key_exists(3, $exploded))
	{
	    $rv['nextcomm']=$exploded[3];
    	    if(array_key_exists(4, $exploded))
	    {
	        $rv['nextcommcritical']=$exploded[4];
	    }
	}
    }

    return $rv;
}

?>
