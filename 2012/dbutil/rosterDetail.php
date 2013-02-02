<?php
/**
 * Created by IntelliJ IDEA.
 * User: mseavey
 * Date: 3/22/12
 * Time: 1:44 PM
 * To change this template use File | Settings | File Templates.
 */

require_once "databases.php";
require_once "draftManager.php";
/**
 * Created by IntelliJ IDEA.
 * User: Mayhew Seavey
 * Date: 3/21/11
 * Time: 12:56 AM
 * To change this template use File | Settings | File Templates.
 */

$teamID = 94;

echo implode(", ", getOpenPositionsForMyTeam()) . "<br><br>";

  echo "Open: " . implode(", ", getOpenPositionsForTeam($teamID)) . "<br>";
  $lineup = getDraftedLineup($teamID);
  $lineup->printLineup();

?>
