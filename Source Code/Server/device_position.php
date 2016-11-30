<?php

    include_once "mysqli_connect.php";
    $lat = $_POST['user_lat'];  // latitude of mobile device
  	$lng = $_POST['user_lng'];  // longitude of mobile device
	$device = $_POST['device'];
	$name = $_POST['name'];
	
  	if (isset($_POST['user_lat']) && isset($_POST['user_lng']) && isset($_POST['device']) && isset($_POST['name'])) 
	{
	
		// check if device exists
		$querySearch = "SELECT device_id FROM devices WHERE device_id='$device'";
		$queryResult = mysqli_query ($dbc, $querySearch) or trigger_error("Query: $query\n<br />MySQL Error: " . mysqli_error($dbc));
		if (mysqli_num_rows($queryResult) == 0) 
		{
			// a new device
			$q = "INSERT INTO devices (device_id, lat,lng,name,updated_at) VALUES ('$device', '$lat', '$lng', '$name',NOW())";
			$r = mysqli_query ($dbc, $q) or trigger_error("Query: $q\n<br />MySQL Error: " . mysqli_error($dbc));
			if (mysqli_affected_rows($dbc) == 0) 
				echo '{"error":"true","message":"failed"}';
			else
				echo '{"error":"false","message":"success"}';
		}
		else
		{
			// device is already registered
			$q = "UPDATE devices SET lat='$lat', lng='$lng',updated_at=NOW() WHERE device_id='$device' LIMIT 1";
			$r = mysqli_query ($dbc, $q) or trigger_error("Query: $q\n<br />MySQL Error: " . mysqli_error($dbc));
			if (mysqli_affected_rows($dbc) == 0) 
				echo '{"error":"true","message":"failed"}';
			else
				echo '{"error":"false","message":"success"}';
		}		
			
	}
  	else
  	{
  		echo '{"error":"true","message":"data missed"}';
  	}
    	
	mysqli_close($dbc);
	
?>