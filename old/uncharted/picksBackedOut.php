<?php
  require_once "databases.php";

if (!isset($_GET['round'])) {
  die ("ERROR 563: Round # not specified.");
}
if (!isset($_GET['pick'])) {
  die ("ERROR 564: Pick # not specified.");
}

$round = $_GET['round'];
$pick = $_GET['pick'];

$overallPick = ($round-1)*12 + $pick;

$sql = "UPDATE DraftResults SET BackedOut = 1
        WHERE Year = 2011 AND Keeper = 0 AND OverallPick > ".$overallPick;
query($sql);

echo "Success!";
?>