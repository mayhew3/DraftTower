<?php
  require_once "databases.php";
  require_once "draftManager.php";
/**
 * Created by IntelliJ IDEA.
 * User: Mayhew Seavey
 * Date: 3/21/11
 * Time: 12:56 AM
 * To change this template use File | Settings | File Templates.
 */

  $sql = "SELECT ID, Name FROM Teams WHERE Year = 2012";
  $rs = query($sql);

  echo getTeamName(getMyTeamID()) . "<br>";
  echo implode(", ", getOpenPositionsForMyTeam()) . "<br><br>";

  while ($row = mysql_fetch_assoc($rs)) {
    $id = $row['ID'];
    $name = $row['Name'];
    echo "$name: <br>";
    echo "Open: " . implode(", ", getOpenPositionsForTeam($id)) . "<br>";
    $lineup = getDraftedLineup($id);
    $lineup->printLineup();
  }

?>
