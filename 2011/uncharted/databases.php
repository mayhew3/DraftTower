<?php
// Version 0.1 - 3/14/2011

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
  $sql = "SELECT ID FROM Teams WHERE UserID = '$user'";
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

?>