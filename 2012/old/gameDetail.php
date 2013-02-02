<?php
  require_once "databases.php";

if (!isset($_GET)) {
  header("Location:index.php");
}

$team1 = $_GET['T1'];
$team2 = $_GET['T2'];
$period = $_GET['Period'];

$sql = "SELECT * FROM WeeklyScoring WHERE TeamID = " . $team1 . " AND Period = " . $period;
$row1 = getExactlyOneRowFromQuery($sql);

$sql = "SELECT * FROM WeeklyScoring WHERE TeamID = " . $team2 . " AND Period = " . $period;
$row2 = getExactlyOneRowFromQuery($sql);

$battingCats = array("AB", "H", "BB", "SB", "CS", "R", "RBI", "HR", "TB", "BA", "OBP", "SLG");
$pitchingCats = array("INN", "ER", "HA", "BBI", "W", "L", "S", "K", "GS", "ERA", "WHIP");

function getColor($myScore, $theirScore, $stat) {
  if (in_array($stat, array("CS", "ER", "HA", "BBI", "L", "ERA", "WHIP"))) {
    $myScore *= -1;
    $theirScore *= -1;
  }
  if ($myScore > $theirScore) {
    return "#3B2F2F";
  } elseif ($myScore < $theirScore) {
    return "#2F2F3B";
  } else {
    return "#2F3B2F";
  }
}

function getBorderColor($myScore, $theirScore, $category) {
  if (in_array($category, array("CS", "ER", "HA", "BBI", "L", "ERA", "WHIP"))) {
    $myScore *= -1;
    $theirScore *= -1;
  }
  if ($myScore > $theirScore) {
    return "#734A4A";
  } elseif ($myScore < $theirScore) {
    return "#4A4A73";
  } else {
    return "#4A734A";
  }
}

function get2010BattingWins($myRow, $theirRow) {
  $posCats = array("BA", "R", "RBI", "HR", "SB");

  $wins = 0;

  foreach($posCats as $posCat) {
    if ($myRow[$posCat] > $theirRow[$posCat]) {
      $wins++;
    }
  }

  return $wins;
}

function get2010PitchingWins($myRow, $theirRow) {
  $posCats = array("W", "S", "K");
  $negCats = array("ERA", "WHIP");

  $wins = 0;

  foreach($posCats as $posCat) {
    if ($myRow[$posCat] > $theirRow[$posCat]) {
      $wins++;
    }
  }

  foreach($negCats as $negCat) {
    if ($myRow[$negCat] < $theirRow[$negCat]) {
      $wins++;
    }
  }
  return $wins;
}

function get2011BattingWins($myRow, $theirRow) {
  $posCats = array("OBP", "SLG", "RHR", "RBI", "HR", "SBC");

  $wins = 0;

  foreach($posCats as $posCat) {
    if ($myRow[$posCat] > $theirRow[$posCat]) {
      $wins++;
    }
  }

  return $wins;
}

function get2011PitchingWins($myRow, $theirRow) {
  $posCats = array("WL", "S", "K", "INN");
  $negCats = array("ER", "BRA");

  $wins = 0;

  foreach($posCats as $posCat) {
    if ($myRow[$posCat] > $theirRow[$posCat]) {
      $wins++;
    }
  }

  foreach($negCats as $negCat) {
    if ($myRow[$negCat] < $theirRow[$negCat]) {
      $wins++;
    }
  }
  return $wins;
}

function print_cell($myScore, $theirScore, $category) {
  $color = getColor($myScore, $theirScore, $category);
  $border = getBorderColor($myScore, $theirScore, $category);
  echo "<td align='right' style='background-color:".$color.";border:1px solid ".$border."'>".$myScore."</td>";
}
?>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
  <title>Uncharted Territories</title>
  <link href="../standard2.css" rel="stylesheet" type="text/css" />
  <!--[if IE]>
  <style type="text/css">
    /* place css fixes for all versions of IE in this conditional comment */
    .twoColHybLt #sidebar1 { padding-top: 30px; }
    .twoColHybLt #mainContent { padding-top: 15px; }
    /* the above proprietary zoom property gives IE the hasLayout it may need to avoid several bugs */
  </style>
  <![endif]--></head>

<body class="twoColHybLt">

<div id="container">
  <div id="header">
    <img src="../images/Banner3.jpg" alt="Uncharted Territories" />
    <!-- end #header --></div>
  <div id="sidebar1">
    <? include "navtable.php"?>
    <!-- end #sidebar1 --></div>
  <div id="mainContent">
    <div class="headerText">Season <?=$row1['BYear']?>, Period <?=$row1['Period']?> </div>
    <br/>
    <br/>

    <span class="subHeader">Batting: </span> <br/>
    <table>
      <tr>
        <td>Team</td>
    <? foreach ($battingCats as $battingCat) { ?>
        <td><?=$battingCat?></td>
    <? } ?>

        <td>&nbsp;</td>
        <td>H2H</td>
        <td>&nbsp;</td>
        <td>2010 Cats</td>
        <td>&nbsp;</td>
        <td>2011 Cats</td>
      </tr>

  <!-- Team 1 -->

      <tr>
        <td><?=$row1['BTeam']?></td>
    <? foreach ($battingCats as $battingCat) {
          $myScore = $row1[$battingCat];
          $theirScore = $row2[$battingCat];
          print_cell($myScore, $theirScore, $battingCat);
       } ?>

        <td>&nbsp;</td>
    <? print_cell($row1['BFPTS'], $row2['BFPTS'], "BFPTS"); ?>
        <td>&nbsp;</td>
    <? print_cell(get2010BattingWins($row1, $row2), get2010BattingWins($row2, $row1), "2010Cats"); ?>
        <td>&nbsp;</td>
    <? print_cell(get2011BattingWins($row1, $row2), get2011BattingWins($row2, $row1), "2011Cats"); ?>
      </tr>


    <!-- Team 2 -->

      <tr>
        <td><?=$row2['BTeam']?></td>
    <? foreach ($battingCats as $battingCat) {
         $myScore = $row2[$battingCat];
         $theirScore = $row1[$battingCat];
         print_cell($myScore, $theirScore, $battingCat);
    } ?>

      <td>&nbsp;</td>
    <? print_cell($row2['BFPTS'], $row1['BFPTS'], "BFPTS"); ?>
        <td>&nbsp;</td>
    <? print_cell(get2010BattingWins($row2, $row1), get2010BattingWins($row1, $row2), "2010Cats"); ?>
        <td>&nbsp;</td>
    <? print_cell(get2011BattingWins($row2, $row1), get2011BattingWins($row1, $row2), "2011Cats"); ?>
      </tr>
    </table>

   <br/>
    <br/>

    <span class="subHeader">Pitching: </span> <br/>
    <table>
      <tr>
        <td>Team</td>
    <? foreach ($pitchingCats as $pitchingCat) { ?>
        <td><?=$pitchingCat?></td>
    <? } ?>

        <td>&nbsp;</td>
        <td>H2H</td>
        <td>&nbsp;</td>
        <td>2010 Cats</td>
        <td>&nbsp;</td>
        <td>2011 Cats</td>
      </tr>

  <!-- Team 1 -->

      <tr>
        <td><?=$row1['BTeam']?></td>
    <? foreach ($pitchingCats as $pitchingCat) {
          $myScore = $row1[$pitchingCat];
          $theirScore = $row2[$pitchingCat];
          print_cell($myScore, $theirScore, $pitchingCat);
       } ?>

        <td>&nbsp;</td>
    <? print_cell($row1['PFPTS'], $row2['PFPTS'], "PFPTS"); ?>
        <td>&nbsp;</td>
    <? print_cell(get2010PitchingWins($row1, $row2), get2010PitchingWins($row2, $row1), "2010Cats"); ?>
        <td>&nbsp;</td>
    <? print_cell(get2011PitchingWins($row1, $row2), get2011PitchingWins($row2, $row1), "2011Cats"); ?>
      </tr>

    <!-- Team 2 -->

      <tr>
        <td><?=$row2['BTeam']?></td>
    <? foreach ($pitchingCats as $pitchingCat) {
         $myScore = $row2[$pitchingCat];
         $theirScore = $row1[$pitchingCat];
         print_cell($myScore, $theirScore, $pitchingCat);
      } ?>

      <td>&nbsp;</td>
    <? print_cell($row2['PFPTS'], $row1['PFPTS'], "PFPTS"); ?>
        <td>&nbsp;</td>
    <? print_cell(get2010PitchingWins($row2, $row1), get2010PitchingWins($row1, $row2), "2010Cats"); ?>
        <td>&nbsp;</td>
    <? print_cell(get2011PitchingWins($row2, $row1), get2011PitchingWins($row1, $row2), "2011Cats"); ?>
      </tr>


    </table>

    <br>
    <br>

    <a href="hybrid.php">Back up to Game List</a>

    <!-- end #mainContent --></div>
  <!-- This clearing element should immediately follow the #mainContent div in order to force the #container div to contain all child floats --><br class="clearfloat" />
  <!-- end #container --></div>
</body>
</html>
