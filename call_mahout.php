<?php
/*  Copyright 2014 Tamber, Inc. 
	Developed by Geoffrey Lee

   	Licensed under the Apache License, Version 2.0 (the "License");
   	you may not use this file except in compliance with the License.
   	You may obtain a copy of the License at

       	http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/

/* USAGE: pass the script the field likes with ID's separated by \x1f;
 * For example: http://yourdomain.com/call_mahout.php?likes=100\x1f25
 */

// This is the IP address at which the Mahout server app is hosted. 
$MAHOUTIP = "127.0.0.1";
try {
	header("Cache-control: no-cache");
	header("Content-type: application/json; charset=utf-8");
	$time = microtime(true);
	if (isset($_GET["likes"])) {
		$result = parse_mahout($_GET['likes']);
		$result["time"] = microtime(true) - $time;
		echo json_encode($result);
	} else {
		echo json_encode(array("success" => false, "time" => microtime(true) - $time, "error" => "no likes given"));
	}
} catch (Exception $e) {
	echo json_encode(array("success" => false, "time" => microtime(true) - $time, "error" => "unexpected error " . $e->getMessage()));
}

function parse_mahout($likes) {
	$likearg = str_replace("\\x1f", " ", $likes);
	if(trim($likearg) == "") {
		return array("success"=>false, "error" => "please select some movies");
	}
	$r = call_backend('rec', $likearg);
	$sugg = array();
	$rp = explode(' ', $r);
	foreach($rp as $suggested) {
		$suggested = trim($suggested);
		if($suggested!= "") {
			$suggv = explode(":", $suggested);
			$item = array("movie_key" => $suggv[0], "rating" => $suggv[1]);
			array_push($sugg, $item);
		}	
	}
	return array("success" => "true", "result" => $sugg);
}

function call_backend($command, $arg) {
	$port = 24579;
	$conn = fsockopen($MAHOUTIP, $port);
	if ($conn === FALSE) {
		$conn = fsockopen($MAHOUTIP, $port);
		if ($conn === FALSE) {
			echo "Mahout Backend Server Not Found";
		}
	}
	fwrite($conn, $command . " " . $arg . "\n");
	
	$result = fgets($conn);
	while(!feof($conn)) {
		$result = $result . fgets($conn);
		if ($conn === FALSE) {
			break;
		}
	}
	return $result;
}
?>