<?php
  require_once "rosterManager.php";
/**
 * Created by IntelliJ IDEA.
 * User: Mayhew Seavey
 * Date: 3/23/11
 * Time: 2:42 PM
 * To change this template use File | Settings | File Templates.
 */

function insertKeeper($round, $pick, $player) {
  insertDraftPick($round, $pick, $player, 1);
}

function insertNonKeeperDraftPick($round, $pick, $player) {
  insertDraftPick($round, $pick, $player, 0);
}

function insertDraftPick($round, $pick, $player, $keeper) {
  $overallPick = ($round-1)*12 + $pick;

  $playerID = getPlayerIDIfValid($player);

  $teamID = getTeamUpInDraft($round, $pick);
  $teamName = addslashes(getTeamName($teamID));

  $optimalPosition = getOptimalPositionForDraftPick($playerID, $teamID);

  if (is_null($playerID)) {
    die("ERROR 567: Player '".$player."' not found!");
  }

  $sql = "INSERT INTO DraftResults (Year, Round, Pick, PlayerID, BackedOut, PlayerString, OverallPick, TeamID, TeamName, DraftPos, Keeper)
         VALUES (2011, $round, $pick, $playerID, 0, '$player', $overallPick, $teamID, '$teamName', '$optimalPosition', $keeper)";
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
        AND SourceData = 2011
        ORDER BY Total DESC";
  $rs = query($sql);
  while ($row = mysql_fetch_assoc($rs)) {
    $positions[] = $row['Position'];
  }
  return $positions;
}

// todo: make this split and match on first and last name.
function getPlayerIDIfValid($player) {
  $sql = "SELECT ID FROM Players WHERE PlayerString = '" . $player . "'";
  return getAtMostOneValueFromQuery($sql, "ID");
}

function existingDraftPick($round, $pick) {
  $sql = "SELECT PlayerString FROM DraftResults WHERE Round = $round AND Pick = $pick AND BackedOut = 0 AND Year = 2011";
  return getAtMostOneValueFromQuery($sql, "PlayerString");
}

function isPlayerAlreadyDrafted($playerString) {
  $sql = "SELECT ID FROM DraftResults WHERE PlayerString = '$playerString' AND BackedOut = 0 AND Year = 2011";
  return !is_null(getAtMostOneRowFromQuery($sql, "ID"));
}

function getTeamUpInDraft($round, $pick) {
  if ($round % 2 == 0) {
    $pick = 13 - $pick;
  }
  $sql = "SELECT ID FROM Teams WHERE DraftOrder = $pick AND Year = 2011";
  return getExactlyOneValueFromQuery($sql, "ID");
}

 
?>