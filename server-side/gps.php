<?php

$file = "/tmp/gps-position.txt";

if ( isset($_GET["lat"]) && preg_match("/^-?\d+.\d+$/", $_GET["lat"])
    && isset($_GET["lon"]) && preg_match("/^-?\d+.\d+$/", $_GET["lon"]) ) {
	$f = fopen($file,"w");
	fwrite($f, date("Y-m-d H:i:s")."_".$_GET["lat"]."_".$_GET["lon"]);
	fclose($f);
    echo "OK";
} else {
    echo 'Please type this URL in the <a href="https://play.google.com/store/apps/details?id=fr.herverenault.selfhostedgpstracker">Self-Hosted GPS Tracker</a> Android app on your phone.';
}

