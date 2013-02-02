<?php
  require_once "databases.php";
  require_once "draftManager.php";
  // Set the JSON header
header("Content-type: text/json");

  $categories = array("OBP", "SLG", "RHR", "RBI", "HR", "SBC", "INN", "ERA", "WHIP", "WL", "K", "S");

  $teamID = getMyTeamID();
  if (isset($_GET['commish']) && $_GET['commish'] == 1) {
    $teamID = getTeamCurrentlyUpInDraft();
  }

  $sql = "SELECT * FROM TeamScoring
          WHERE TeamID = $teamID
          AND Year = 2012";
  $myRow = getAtMostOneRowFromQuery($sql);

  foreach ($categories as $category) {
    $avgClauses[] = "AVG($category) AS $category";
  }
  $fullSelect = implode(", ", $avgClauses);

  $sql = "SELECT $fullSelect
          FROM TeamScoring
          WHERE TeamID <> $teamID
          AND Year = 2012";
  $theirRow = getExactlyOneRowFromQuery($sql);

  $sql = "SELECT *
          FROM TeamCatRankings
          WHERE TeamID = $teamID
          AND Year = 2012";
  $rankings = getExactlyOneRowFromQuery($sql);

  foreach ($categories as $category) {
    $myStat = isset($myRow) ? $myRow[$category] : 0;
    if (is_null($myStat)) {
      $myStat = 0;
    }
    $myRank = $rankings[$category."Rank"];
    $datapoints[] = array($theirRow[$category], $myStat, $myRank);
  }

	echo json_encode( $datapoints );
?>
