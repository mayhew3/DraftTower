<?php
// Version 0.2 - 3/20/2012

require_once "mysql.php";

ini_set('memory_limit', '32M');
ini_set("session.gc_maxlifetime", "18000"); 
 
session_start();
determineServer();
connect_uncharted();
CheckMode();

function determineServer() {
    $serverName = getenv("SERVER_NAME");
    $_SESSION['debug'] = false;
    if ($serverName == "uncharted.mayhew3.com") {
        $_SESSION['server'] = "live";
    } elseif (strpos($serverName, "mayhew3.com") === false) {
        $_SESSION['server'] = "localDebug";
        $_SESSION['debug'] = true;
    } else {
        $_SESSION['server'] = "sandbox";
        $_SESSION['debug'] = true;
    }
}

function IsDebug() {
  return $_SESSION['debug'];
}

function CheckMode() {
  if (!isset($_SESSION['mode'])) {
    $_SESSION['mode'] = "all";
  }
}

function CheckUser() {
  if (!isset($_SESSION["UserID"])) {
    header("Location: login.php");
  }
}

function CheckAdminUser() {
  if($_SESSION["Role"] != "admin") {
    header("Location: index.php");
  }
}

function getExactlyOneValueFromQuery($query, $columnName) {
  $row = getExactlyOneRowFromQuery($query);
  return $row[$columnName];
}

function getAtMostOneValueFromQuery($query, $columnName) {
  $row = getAtMostOneRowFromQuery($query);
  if (is_null($row)) {
    return NULL;
  } else {
    return $row[$columnName];
  }
}

function getExactlyOneRowFromQuery($query) {
  $rs = query($query);
  $numRows = mysql_num_rows($rs);
  if ($numRows == 1) {
    return mysql_fetch_assoc($rs);
  } else {
    throw new RuntimeException("Expected exactly one row returned from query '".$query."', but found ".$numRows.".");
  }
}

function getAtMostOneRowFromQuery($query) {
  $rs = query($query);
  $numRows = mysql_num_rows($rs);
  if ($numRows == 1) {
    return mysql_fetch_assoc($rs);
  } elseif ($numRows == 0) {
    return NULL;
  } else {
    die("Expected at most one row returned from query '".$query."', but found ".$numRows.".");
  }
}

function getCurrentUser() {
  return $_SESSION['UserID'];
}

function getMyTeamID() {
  $user = getCurrentUser();
  $sql = "SELECT ID FROM Teams WHERE UserID = '$user' AND Year = 2012";
  return getExactlyOneValueFromQuery($sql, "ID");
}
 
function GetFullName($username) {
  $queryString = "SELECT FirstName, LastName FROM Users WHERE user = '".$username."'";
  $rs = query($queryString);
  $row = mysql_fetch_assoc($rs);
  return $row["FirstName"]." ".$row["LastName"];
}

function GetFirstName($username) {
  $queryString = "SELECT FirstName FROM Users WHERE user = '".$username."'";
  $rs = query($queryString);
  $row = mysql_fetch_assoc($rs);
  return $row["FirstName"];
}

function IsFullUser() {
  return ($_SESSION["Role"] == "user" || $_SESSION["Role"] == "admin");
}

function IsAdmin() {
  return $_SESSION["Role"] == "admin";
}

function IsMayhew() {
  return $_SESSION['UserID'] == "mayhew";
}


function checkNullOrBlank($value, $default) {
  if (is_null($value) || $value === "") {
    return $default;
  }
  return $value;
}

function getSQLStringOrNULL($value) {
  if (is_null($value) || $value === "") {
    return "NULL";
  } else {
    return "'".$value."'";
  }
}

function curPageName() {
   return substr($_SERVER["SCRIPT_NAME"],strrpos($_SERVER["SCRIPT_NAME"],"/")+1);
}

function fixBadCharacters($str) {
  $str = str_replace("�", "'", $str);
  $str = str_replace("’", "'", $str);
  $str = str_replace("–", "-", $str);
  $str = str_replace("—", "-", $str);
  $str = str_replace("�", "-", $str);
  return $str;
}

function getTeamName($id) {
  return getExactlyOneValueFromQuery("SELECT Name FROM Teams WHERE ID = $id", "Name");
}

function extractFirstAndLastName($playerString) {
  $playerString = trim($playerString);
  $commaParts = explode(", ", $playerString);

  if (sizeof($commaParts) != 2) {
    die("Found player " . $playerString . " without exactly 1 comma.");
  }

  $lastName = $commaParts[0];

  $remainderString = $commaParts[1];

  $spaceParts = explode(" ", $remainderString);
  $numParts = sizeof($spaceParts);

  if ($numParts < 3) {
    die("Found player with fewer than 3 symbols after the comma: '" . $remainderString . "', Player " . $playerString);
  }

  $teamName = $spaceParts[$numParts-1];
  $position = $spaceParts[$numParts-2];

  if (strlen($teamName) < 2) {
    die("Incorrect team name '" . $teamName . "', from remainder string '" . $remainderString . "'");
  }
  if (strlen($position) < 1) {
    die("Incorrect position '" . $position . "', from remainder string '" . $remainderString . "'");
  }

  $firstNameParts = array_slice($spaceParts, 0, $numParts-2);

  if (sizeof($firstNameParts) < 1) {
    die("Found no parts in first name piece.");
  }

  $firstName = implode(" ", $firstNameParts);

  return array($firstName, $lastName, $teamName);
}

?>