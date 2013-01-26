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
$sql = "DELETE FROM DraftResults WHERE Year = 2011 AND Keeper = 0";
query($sql);

runDraft(150);

function runDraft($maxPick) {
  for ($pick = 1; $pick <= $maxPick; $pick++) {
    $sql = "SELECT TeamID FROM DraftResults WHERE OverallPick = $pick AND Year = 2011 AND BackedOut = 0";
    $alreadyGone = getAtMostOneValueFromQuery($sql, "TeamID");

    if (!isset($alreadyGone)) {
      $round = floor(($pick-1)/12) + 1;
      $inRound = (($pick-1) % 12) + 1;

      $teamID = getTeamUpInDraft($round, $inRound);
      $myTeamID = getMyTeamID();

      echo "Round $round, Pick $inRound, Team $teamID <br>";

      $openPositions = getOpenPositionsForTeam($teamID);
      if (empty($openPositions)) {
        $joinClause = "";
      } else {
        $positionStr = "'" . implode("', '", $openPositions) . "'";
        $joinClause = "INNER JOIN Eligibilities e
                      ON p.PlayerID = e.PlayerID
                      WHERE e.Position IN ($positionStr)";
      }

      if ($teamID == $myTeamID) {
        $sql = "SELECT p.Player
                FROM AllPlayersByQuality p
                $joinClause
                LIMIT 1";
      } else {
        $sql = "SELECT p.Player
            FROM UnclaimedPlayersOrdered2011 p
            $joinClause
            LIMIT 1";
      }
      $player = getExactlyOneValueFromQuery($sql, "Player");

      $seconds = rand(1, 5);
      echo "Waiting $seconds seconds...<br>";
      sleep($seconds);

      insertNonKeeperDraftPick($round, $inRound, $player);
      echo "Inserted $player during Round $round, Pick $inRound.<br>";
    }
  }
}
?>
