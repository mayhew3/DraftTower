<?php
  require_once "rosterManager.php";
/**
 * Created by IntelliJ IDEA.
 * User: Mayhew Seavey
 * Date: 3/23/11
 * Time: 2:42 PM
 * To change this template use File | Settings | File Templates.
 */

function getOverallPick($round, $pick) {
  return ($round-1)*12 + $pick;
}

function getRound($overallPick) {
  return floor(($overallPick-1)/12) + 1;
}

function getPickInRound($overallPick) {
  return (($overallPick-1) % 12) + 1;
}

function insertKeeper($round, $pick, $player) {
  $overallPick = getOverallPick($round, $pick);

  $playerID = getPlayerIDIfValid($player);

  if (is_null($playerID)) {
    die("ERROR 567: Player '".$player."' not found!");
  }

  $teamID = getTeamUpInDraft($pick);

  $sql = "INSERT INTO Keepers (Year, Round, Pick, PlayerID, OverallPick, TeamID)
         VALUES (2012, $round, $pick, $playerID, $overallPick, $teamID)";
  query($sql);
}

function getExistingKeeperForRound($round, $pick) {
  $overallPick = getOverallPick($round, $pick);
  $sql = "SELECT *
            FROM Keepers
            WHERE Year = 2012
            AND OverallPick = $overallPick";
  return getAtMostOneRowFromQuery($sql);
}

function getExistingKeeperForPlayer($player) {
  $playerID = getPlayerIDIfValid($player);
  if (is_null($playerID)) {
    die("ERROR 591: Invalid player $player");
  }

  $sql = "SELECT *
            FROM Keepers
            WHERE Year = 2012
            AND PlayerID = $playerID";
  return getAtMostOneRowFromQuery($sql);
}

function insertKeeperDraftPick($round, $pick, $player, $playerID) {
  insertDraftPick($round, $pick, $player, $playerID, 1);
}

function insertNonKeeperDraftPickAndAnyFollowingKeepers($round, $pick, $player) {
  $followingKeepers = array();

  insertNonKeeperDraftPick($round, $pick, $player);

  $overallPick = getOverallPick($round, $pick);

  $potentiallyMoreKeepers = true;
  $keeperPick = $overallPick;

  while ($potentiallyMoreKeepers) {
    $keeperPick++;
    $row = getExistingKeeperForRound(getRound($keeperPick), getPickInRound($keeperPick));

    if (is_null($row)) {
      $potentiallyMoreKeepers = false;
    } else {
      $keeperID = $row['PlayerID'];
      $keeperString = getPlayerStringFromID($keeperID);

      insertKeeperIfNotAlreadyPresent($keeperPick, $keeperID, $keeperString);

      $followingKeepers[] = array($keeperPick, $keeperString);
    }
  }

  return $followingKeepers;
}

function insertKeeperIfNotAlreadyPresent($overallPick, $playerID, $playerString) {
  $sql = "SELECT 1
              FROM DraftResults
              WHERE Year = 2012
              AND OverallPick = $overallPick";
  $existingRow = getAtMostOneRowFromQuery($sql);

  if (is_null($existingRow)) {
    insertKeeperDraftPick(getRound($overallPick), getPickInRound($overallPick), $playerString, $playerID);
  }
}

function insertNonKeeperDraftPick($round, $pick, $player) {
  insertDraftPick($round, $pick, $player, null, 0);
}

function insertDraftPick($round, $pick, $player, $playerID, $keeper) {
  $overallPick = getOverallPick($round, $pick);

  if (is_null($playerID)) {
    $playerID = getPlayerIDIfValid($player);
  }

  if (is_null($playerID)) {
    die("ERROR 567: Player '".$player."' not found!");
  }

  $teamID = getTeamUpInDraft($pick);
  $teamName = addslashes(getTeamName($teamID));

  $optimalPosition = getOptimalPositionForDraftPick($playerID, $teamID);

  $sql = "INSERT INTO DraftResults (Year, Round, Pick, PlayerID, BackedOut, PlayerString, OverallPick, TeamID, TeamName, DraftPos, Keeper)
         VALUES (2012, $round, $pick, $playerID, 0, '$player', $overallPick, $teamID, '$teamName', '$optimalPosition', $keeper)";
  query($sql);
}

function getOptimalPositionForDraftPick($playerID, $teamID) {
  $openPositions = getOpenPositionsFromDraftLineup($teamID);
  $positions = getPositionsForPlayerOrderedByRating($playerID);
  foreach ($positions as $position) {
    if (in_array($position, $openPositions)) {
      return $position;
    }
  }
  if (in_array("P", $positions)) {
    return "RS";
  }
  if (in_array("DH", $openPositions)) {
    return "DH";
  }
  return "RS";
}

function getDraftedLineup($teamID) {
  $lineup = new Lineup();
  $players = getPlayersForTeam($teamID);
  foreach ($players as $player) {
    $lineup->placePlayerIntoLineup($player, $player->getDraftedPosition());
  }
  return $lineup;
}

function getOpenPositionsFromDraftLineup($teamID) {
  $lineup = getDraftedLineup($teamID);
  return $lineup->getOpenPositions();
}

function getPositionsForPlayerOrderedByRating($playerID) {
  $positions = array();
  $sql = "SELECT Position
        FROM AllPlayersByQuality
        WHERE PlayerID = $playerID
        AND Position <> 'DH'
        AND Year = 2012
        ORDER BY Total DESC";
  $rs = query($sql);
  while ($row = mysql_fetch_assoc($rs)) {
    $positions[] = $row['Position'];
  }
  return $positions;
}

// todo: make this split and match on first and last name.
function getPlayerIDIfValid($player) {
  $sql = "SELECT ID FROM Players WHERE PlayerString = '$player' AND Year = 2012";
  return getAtMostOneValueFromQuery($sql, "ID");
}

function getPlayerStringFromID($playerID) {
  $sql = "SELECT PlayerString FROM Players WHERE ID = '$playerID'";
  return getAtMostOneValueFromQuery($sql, "PlayerString");
}

function existingDraftPick($round, $pick) {
  $sql = "SELECT PlayerString FROM DraftResults WHERE Round = $round AND Pick = $pick AND BackedOut = 0 AND Year = 2012";
  return getAtMostOneValueFromQuery($sql, "PlayerString");
}

function isPlayerAlreadyDrafted($playerString) {
  $sql = "SELECT ID FROM DraftResults WHERE PlayerString = '$playerString' AND BackedOut = 0 AND Year = 2012";
  return !is_null(getAtMostOneRowFromQuery($sql, "ID"));
}

function getTeamCurrentlyUpInDraft() {
  $currentOverall = getCurrentOverallPick();
  return getTeamUpInDraft(getPickInRound($currentOverall));
}

function getTeamUpInDraft($pick) {
  $sql = "SELECT ID FROM Teams WHERE DraftOrder = $pick AND Year = 2012";
  return getExactlyOneValueFromQuery($sql, "ID");
}

function getCurrentOverallPick() {
  $currentPick = 1;

  $sql = "SELECT MAX(ID) AS MaxID FROM DraftResults WHERE BackedOut = 0 AND Year = 2012";
  $resultID = getAtMostOneValueFromQuery($sql, "MaxID");
  if (!is_null($resultID)) {
    $sql = "SELECT OverallPick FROM DraftResults WHERE ID = $resultID";
    $row = getExactlyOneRowFromQuery($sql);
    $currentPick = $row['OverallPick'] + 1;
  }
  return $currentPick;
}

 
?>