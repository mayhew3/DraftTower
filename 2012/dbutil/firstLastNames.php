<?php
require_once "databases.php";


$sql = "SELECT * FROM Players WHERE Year = 2012 ORDER BY PlayerString";
$rs = query($sql);

while ($row = mysql_fetch_assoc($rs)) {
  $playerID = $row['ID'];
  $playerString = $row['PlayerString'];

  $nameArray = extractFirstAndLastName($playerString);
  $firstName = addslashes($nameArray[0]);
  $lastName = addslashes($nameArray[1]);
  $mlbTeam = addslashes($nameArray[2]);

  echo "'$firstName' '$lastName' '$mlbTeam'...";

  $sql = "UPDATE Players
          SET FirstName = '$firstName',
              LastName = '$lastName',
              MLBTeam = '$mlbTeam'
          WHERE ID = '$playerID'";
  query($sql);

  echo "Complete.<br>";
}

?>