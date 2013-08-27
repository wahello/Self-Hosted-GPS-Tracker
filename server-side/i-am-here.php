<!DOCTYPE html>
<html lang="en">
<head>
<meta charset=utf-8>
<title>I am here</title>
</head>
<body>

<?php
$date = $lat = $lon = '';
$date_lat_lon = rtrim(file_get_contents("/tmp/gps-position.txt"));
if ($date_lat_lon) {
	list($date, $lat, $lon) = explode("_", $date_lat_lon);
}
?>

<h1>I was here on <span id="date"><?php echo $date ? $date : "…" ?></span></h1>
<p>(last known position where I had a GPS signal, a network connection, and some battery power)</p>

<div id="mapcanvas" style="width: 800px; height: 600px">
	<div id="intercalaire" style="text-align: center; line-height: 600px; font-weight: bold; border: 1px dotted grey; background-color: #eee;">
		Map currently unavailable.
	</div>
</div>

<script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=false"></script>
<script>
// map
var map, marker;
<?php if ($lat && $lon): ?>
createMap(<?php echo $lat.",".$lon ?>);
<?php endif; ?>
function createMap(lat, lon) {
	var latlng = new google.maps.LatLng(lat, lon);
	var myOptions = {
	    zoom: 12,
	    center: latlng,
	    mapTypeControl: false,
	    navigationControlOptions: {style: google.maps.NavigationControlStyle.SMALL},
	    mapTypeId: google.maps.MapTypeId.ROADMAP
	};
	map = new google.maps.Map(document.getElementById("mapcanvas"), myOptions);
	marker = new google.maps.Marker({
	      position: latlng, 
	      map: map, 
	      title:"I'm here"
	});
	google.maps.event.addListener(marker, "click", function(e) {
		alert("GPS coordinates:\nLatitude: " + marker.getPosition().lat() + "\nLongitude: " + marker.getPosition().lng());
	});
}

doRefresh();
function doRefresh() {
	var xhr; 
	try {
		xhr = new XMLHttpRequest();
	} catch (e) {
		xhr = false;
	}

	xhr.onreadystatechange  = function() { 
		if (xhr.readyState  == 4) {
			if (xhr.status  == 200) {
				dte = xhr.responseText.split('_')[0];
				lat = xhr.responseText.split('_')[1];
				lon = xhr.responseText.split('_')[2];
				if (dte && lat && lon) {
					if (!map) {
						createMap(lat, lon);
					}
					if (map) {
						updateMap(dte, lat, lon);
					}
				}
			}
		}
	};
	xhr.open("GET", "i-am-here-position?" + Math.random(),  true); 
	xhr.send(null);
	setTimeout('doRefresh()', 30000);
}

function updateMap(dte, lat, lon) {
	var latlng = new google.maps.LatLng(lat, lon);
	marker.setPosition(latlng);
	map.panTo(latlng);
	document.querySelector("#date").innerHTML = dte;
}
</script>
