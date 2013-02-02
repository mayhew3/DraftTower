<?php
  function connect($servername, $dbname, $username, $password) {
  @mysql_pconnect($servername, $username, $password)
      or die("Could not connect to MySQL server: " . $servername);
  @mysql_select_db($dbname)
      or die("Could not select database: " . $dbname);
}

function connect_uncharted() {
  $server = $_SESSION['server'];
  if ($server == "localDebug") {
    connect_uncharted_debug();
  } elseif ($server == "live") {
    connect_uncharted_live();
  } else {
    die("Server session variable not recognized: " . $server);
  }
}

function connect_users() {
  $server = $_SESSION['server'];
  if ($server == "live" || $server == "sandbox") {
    connect_users_live();
  } elseif ($server == "localDebug") {
    connect_users_debug();
  } else {
    die("Server session variable not recognized: " . $server);
  }
}

function connect_users_live() {
  connect("db2438.perfora.net", "db329655675", "dbo329655675", "ape11sod");
}

function connect_users_debug() {
  connect("localhost", "UsersDebug", "root", "EvKLeNYWVNPRPWVS");
}


function connect_uncharted_debug() {
  connect("localhost", "UnchartedDebug", "root", "EvKLeNYWVNPRPWVS");
}

function connect_uncharted_live() {
  connect("db1444.perfora.net", "db241476257", "dbo241476257", "ape11sod");
}

function disconnect() {
  mysql_close();
}

function query($query) {
  $result = @mysql_query($query)
      or die($query . "<br>" . mysql_error());
  return $result;
}
?>