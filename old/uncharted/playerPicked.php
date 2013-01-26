<?php
  require_once "databases.php";
  require_once "draftManager.php";

if (!isset($_GET['round'])) {
  die ("ERROR 563: Round # not specified.");
}
if (!isset($_GET['pick'])) {
  die ("ERROR 564: Pick # not specified.");
}
if (!isset($_GET['player'])) {
  die ("ERROR 565: Player not specified.");
}

$round = $_GET['round'];
$pick = $_GET['pick'];
$player = urldecode($_GET['player']);

$existingDraftPick = existingDraftPick($round, $pick);
if (is_null($existingDraftPick)) {
  insertNonKeeperDraftPick($round, $pick, $player);
  echo "Success!";
} else {
  if (!isPlayerAlreadyDrafted($player)) {
    echo "ERROR 587: Pick already exists for Round $round, Pick $pick, and player '$player' is not already
         drafted. Check the pick again and retry.";
  } else {
    echo "Skipping, pick already exists.";
  }
}

?>