<?php
  require_once "databases.php";
  // Set the JSON header
header("Content-type: text/json");

  $categories = array("OBP", "SLG", "RHR", "RBI", "HR", "SBC", "INN", "ER", "BRA", "WL", "K", "S");

  $teamID = getMyTeamID();
  $sql = "SELECT * FROM TeamScoring
          WHERE TeamID = $teamID
          AND SourceData = 2011
          AND DraftYear = 2011";
  $myRow = getAtMostOneRowFromQuery($sql);

  if (isset($myRow)) {
    $myStats = array($myRow['OBP'], $myRow['RBI']);
  } else {
    $myStats = array(0, 0, 0, 0, 0, 0);
  }

  foreach ($categories as $category) {
    $avgClauses[] = "AVG($category) AS $category";
  }
  $fullSelect = implode(", ", $avgClauses);

  $sql = "SELECT $fullSelect
          FROM TeamScoring
          WHERE TeamID <> $teamID
          AND SourceData = 2011
          AND DraftYear = 2011";
  $theirRow = getExactlyOneRowFromQuery($sql);

  foreach ($categories as $category) {
    $myStat = isset($myRow) ? $myRow[$category] : 0;
    if (is_null($myStat)) {
      $myStat = 0;
    }
    $datapoints[] = array($theirRow[$category], $myStat);
  }

	echo json_encode( $datapoints );
?>
