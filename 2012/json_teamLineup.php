<?php
  require_once "databases.php";
  require_once "draftManager.php";

  CheckUser();

  $teamID = getMyTeamID();
  if (isset($_GET['commish']) && $_GET['commish'] == 1) {
    $teamID = getTeamCurrentlyUpInDraft();
  }

  $lineup = getDraftedLineup($teamID);

  $lineupArray = $lineup->getLineupArray();

  $output = array(
    "sEcho" => intval($_GET['sEcho']),
    "iTotalRecords" => 22,
    "iTotalDisplayRecords" => 22,
    "aaData" => array()
  );

  $openPositions = getOpenPositionsForTeam($teamID);

  foreach ($lineupArray as $playerPosition) {
    $position = $playerPosition[0];
    $player = $playerPosition[1];

    $playerName = "";
    if (!is_null($player)) {
      $playerName = $player->getName();
    }

    $isOpen = in_array($position, $openPositions);

    $output['aaData'][] = array($position, $playerName, $isOpen);
  }

  echo json_encode( $output );
?>
