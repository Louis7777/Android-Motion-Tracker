<?php

// This file contains the database access information. 
// This file also establishes a connection to MySQL and selects the database.

// Set the database access information as constants:
DEFINE ('DB_USER', 'radar');
DEFINE ('DB_PASSWORD', 'radar7777');
DEFINE ('DB_HOST', 'db16.papaki.gr');
DEFINE ('DB_NAME', 'q56051comp_radar');

// Make the connection:
$dbc = @mysqli_connect (DB_HOST, DB_USER, DB_PASSWORD, DB_NAME);
$dbc->query("SET NAMES 'utf8'");
if (!$dbc) {
	trigger_error ('Could not connect to MySQL: ' . mysqli_connect_error() );
}

?>