<?php
  require_once "databases.php";
require_once "rosterManager.php";
require_once "draftManager.php";
/**
 * Created by IntelliJ IDEA.
 * User: Mayhew Seavey
 * Date: 3/21/11
 * Time: 12:56 AM
 * To change this template use File | Settings | File Templates.
 */
if (!IsDebug()) {
  //  die("Shouldn't run this on live server!");
}

$startingPick = 1;
if (isset($_GET['mode'])) {
  if ($_GET['mode'] == "reset") {
    $sql = "DELETE FROM DraftResults WHERE Year = 2012 AND Keeper = 0";
    query($sql);
  } elseif ($_GET['mode'] == "resetall") {
    $sql = "DELETE FROM DraftResults WHERE Year = 2012";
    query($sql);
  }
  echo "Reset draft and beginning again.<br>";
} else {
  $startingPick = getCurrentOverallPick();
}

$numPicks = 1;
if (isset($_GET['picks'])) {
  $numPicks = $_GET['picks'];
}

runDraft($startingPick, $numPicks);

function runDraft($startingPick, $maxPick) {
  for ($pick = $startingPick; $pick < $startingPick+$maxPick; $pick++) {
    $sql = "SELECT TeamID FROM DraftResults WHERE OverallPick = $pick AND Year = 2012 AND BackedOut = 0";
    $alreadyGone = getAtMostOneValueFromQuery($sql, "TeamID");

    if (!isset($alreadyGone)) {
      $round = floor(($pick-1)/12) + 1;
      $inRound = (($pick-1) % 12) + 1;

      $teamID = getTeamUpInDraft($inRound);
      $myTeamID = getMyTeamID();

      if (!maybeInsertKeeperPick($round, $pick, $teamID)) {
        $teamName = getTeamName($teamID);

        echo "Round $round, Pick $inRound, Team $teamName ($teamID) <br>";

        $openPositions = getOpenPositionsForTeam($teamID);

  //      echo implode(", ", $openPositions) . "<br>";
        if (empty($openPositions) ||
            (sizeof($openPositions) == 1 && $openPositions["RS"] == "RS")) {
          $joinClause = "WHERE p.Year = 2012";
        } else {
          $positionStr = "'" . implode("', '", $openPositions) . "'";
          $joinClause = "INNER JOIN Eligibilities e
                        ON p.PlayerID = e.PlayerID
                        WHERE p.Year = 2012
                         AND e.Position IN ($positionStr)";
        }

        if ($teamID == $myTeamID) {
          $sql = "SELECT p.Player
                  FROM UnclaimedDisplayPlayersWithCatsByQuality p
                  $joinClause
                  LIMIT 1";
        } else {
          $sql = "SELECT p.Player
              FROM UnclaimedPlayersOrdered p
              $joinClause
              LIMIT 1";
        }
  //      echo $sql . "<br>";
        $player = getExactlyOneValueFromQuery($sql, "Player");

  //      $seconds = rand(1, 5);
  //      echo "Waiting $seconds seconds...<br>";
  //      sleep($seconds);

        $followingKeepers = insertNonKeeperDraftPickAndAnyFollowingKeepers($round, $inRound, $player);
        echo "Inserted $player during Round $round, Pick $inRound.<br>";
        echo "<br>";

        foreach ($followingKeepers as $keeper) {
          $keeperRound = getRound($keeper[0]);
          $keeperPick = getPickInRound($keeper[0]);
          echo "Following last pick, inserted KEEPER $keeper[1] during Round $keeperRound, Pick $keeperPick.<br>";
          echo "<br>";
        }
      }
    }
  }


}


function maybeInsertKeeperPick($round, $pick, $teamID) {
  $overallPick = getOverallPick($round, $pick);

  $row = getExistingKeeperForRound($round, $pick);

  if (is_null($row)) {
    return false;
  } else {
    $keeperTeam = $row['TeamID'];
    if ($keeperTeam != $teamID) {
      $keeperTeamName = getTeamName($keeperTeam);
      $expectedTeamName = getTeamName($teamID);
      die("Keeper exists for pick #$overallPick, but it's for team '$keeperTeamName', not '$expectedTeamName'");
    } else {
      $playerID = $row['PlayerID'];
      $playerString = getPlayerStringFromID($playerID);
      insertKeeperDraftPick($round, $pick, $playerString, $playerID);
      echo "Inserted KEEPER $playerString during Round $round, Pick $pick.<br><br>";
      return true;
    }
  }

}
?>
