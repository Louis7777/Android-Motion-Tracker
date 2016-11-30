<?php

    include_once "mysqli_connect.php";
    $lat = $_POST['user_lat'];  // latitude of mobile device
  	$lon = $_POST['user_lng'];  // longitude of mobile device
	$rad = $_POST['rad'];  // radius of bounding circle in kilometers
	$R = 6371;  // earth's radius, km
	 
	// first-cut bounding box (in degrees)
  	$maxLat = $lat + rad2deg($rad/$R);
  	$minLat = $lat - rad2deg($rad/$R);
  	// compensate for degrees longitude getting smaller with increasing latitude
  	$maxLon = $lon + rad2deg($rad/$R/cos(deg2rad($lat)));
  	$minLon = $lon - rad2deg($rad/$R/cos(deg2rad($lat)));
  
  	// convert origin of filter circle to radians
  	$lat = deg2rad($lat);
  	$lon = deg2rad($lon);
  	
	
	$sql = "
    Select device_id,name, lat, lng, 
           acos(sin($lat)*sin(radians(lat)) + cos($lat)*cos(radians(lat))*cos(radians(lng)-$lon))*$R As D
    From (
      Select device_id,name, lat, lng
      From devices
      Where lat>$minLat And lat<$maxLat
        And lng>$minLon And lng<$maxLon  and updated_at>DATE_SUB(NOW(), INTERVAL 1 HOUR) 
      ) As FirstCut 
    Where acos(sin($lat)*sin(radians(lat)) + cos($lat)*cos(radians(lat))*cos(radians(lng)-$lon))*$R < $rad
    Order by D";
    	
	$r = @mysqli_query ($dbc, $sql);
		
	$deviceArray = array();
	while($rt = mysqli_fetch_array ($r, MYSQLI_ASSOC)) {
    	$deviceArray[] = $rt;
	}
	$num_rows = mysqli_num_rows($r);
	
	echo '{"total_rows":'.$num_rows.',"devices":'.json_encode($deviceArray).'}';
	
	mysqli_close($dbc);
	
?>