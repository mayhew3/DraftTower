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
$player = trim(urldecode($_GET['player']));

$existingKeeperPickForRound = getExistingKeeperForRound($round, $pick);
$existingDraftPick = existingDraftPick($round, $pick);

$currentPick = getCurrentOverallPick();
$inputPick = getOverallPick($round, $pick);

if (is_null($existingDraftPick)) {
  if (is_null($existingKeeperPickForRound)) {
    $existingKeeperPickForPlayer = getExistingKeeperForPlayer($player);
    if (!is_null($existingKeeperPickForPlayer)) {
      $keeperRound = $existingKeeperPickForPlayer["Round"];
      $keeperPick = $existingKeeperPickForPlayer["Pick"];
      die ("ERROR 592: $player is a keeper for Round $keeperRound, Pick $keeperPick, and can't be selected in this round.");
    }

    if (isPlayerAlreadyDrafted($player)) {
      die ("ERROR 593: $player is already drafted. Cannot pick again.");
    }

    if ($inputPick > $currentPick) {
      $currentRound = getRound($currentPick);
      $currentPickInRound = getPickInRound($currentPick);
      die ("ERROR 589: Non-keeper pick inputted for Round $round, Pick $pick, but the draft is only at
           Round $currentRound, Pick $currentPickInRound.");
    }
    $followingKeepers = insertNonKeeperDraftPickAndAnyFollowingKeepers($round, $pick, $player);
    if (empty($followingKeepers)) {
      echo "Success!";
    } else {
      $numKeepers = sizeof($followingKeepers);
      echo "Successfully inserted draft pick, followed by $numKeepers keepers.";
    }
  } else {
    $keeperID = $existingKeeperPickForRound['PlayerID'];
    $keeperString = trim(getPlayerStringFromID($keeperID));

    if ($player != $keeperString) {
      echo "ERROR 588: Keeper exists for Round $round, Pick $pick, for player $keeperString. Request has
            conflicting player assignment, $player.";
    } else {
      if ($inputPick <= $currentPick) {
        insertKeeperDraftPick($round, $pick, $keeperID, $keeperString);
        echo "Successfully inserted Keeper!";
      } else {
        echo "Skipping keeper pick because draft hasn't yet reached Round $round, Pick $pick.";
      }
    }
  }
} else {
  if (!isPlayerAlreadyDrafted($player)) {
    echo "ERROR 587: Pick already exists for Round $round, Pick $pick, and player '$player' is not already
         drafted. Check the pick again and retry.";
  } else {
    echo "Skipping, pick already exists.";
  }
}

?>