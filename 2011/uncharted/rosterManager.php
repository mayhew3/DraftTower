<?php
  require_once "databases.php";
/**
 * Created by IntelliJ IDEA.
 * User: mseavey
 * Date: 3/21/11
 * Time: 2:40 PM
 * To change this template use File | Settings | File Templates.
 */


$maxSize = 0;

function getOpenPositionsForMyTeam() {
  return getOpenPositionsForTeam(getMyTeamID());
}

function getOpenPositionsForTeam($teamID) {
  global $maxSize;

  $playerGroup = getPlayersForTeam($teamID);

  $startingLineup = new Lineup();
  $lineups = buildLineup($playerGroup, $startingLineup);

  $openPositions = array();
  foreach ($lineups as $lineup) {
    $playerCount = $lineup->getNumberOfPositionedPlayers();
    if ($playerCount >= $maxSize) {
      foreach ($lineup->getOpenPositions() as $openPosition) {
        $openPositions[$openPosition] = $openPosition;
      }
    }
  }

  return $openPositions;
}


// classes


// RosterPlayer: Player class with information about eligibilities.
class RosterPlayer {
  private $positions = array();
  private $id;
  private $name;
  private $draftedPosition;

  function __construct($playerID, $playerStr) {
    $this->id = $playerID;
    $this->name = $playerStr;
    $this->updatePositions();
  }

  function updatePositions() {
    $sql = "SELECT Position
              FROM Eligibilities
              WHERE PlayerID = $this->id
              AND Position <> 'DH'";
    $rs = query($sql);
    while ($row = mysql_fetch_assoc($rs)) {
      $this->positions[] = $row['Position'];
    }

    $sql = "SELECT DraftPos
            FROM DraftResults
            WHERE PlayerID = $this->id";
    $this->draftedPosition = getAtMostOneValueFromQuery($sql, "DraftPos");
  }

  function getEligibilities() {
    return $this->positions;
  }

  function getDraftedPosition() {
    return $this->draftedPosition;
  }

  function getName() {
    return $this->name;
  }

  function canPlayAnyPositions($wantedPositions) {
    $commonPositions = array_intersect($this->positions, $wantedPositions);
    return empty($commonPositions);
  }

  function printPlayer() {
    $positionList = implode(", ", $this->positions);
    echo "$this->name: ($positionList)<br>";
  }
}


// Lineup: Players in Positions

class Lineup {
  public static $positionsToFill = array("C", "1B", "2B", "3B", "SS", "OF", "OF", "OF", "DH",
    "P", "P", "P", "P", "P", "P", "P",
    "RS", "RS", "RS", "RS", "RS", "RS");
  private $playerPositions = array();

  function getPlayersInPosition($wantedPosition) {
    $players = array();
    foreach ($this->playerPositions as $playerPosition) {
      $position = $playerPosition[0];
      $player = $playerPosition[1];
      if ($position == $wantedPosition) {
        $players[] = $player;
      }
    }
    return $players;
  }

  function getCountOfLineupSpots($wantedPosition) {
    $positionSpots = 0;
    foreach (self::$positionsToFill as $position) {
      if ($position == $wantedPosition) {
        $positionSpots++;
      }
    }
    return $positionSpots;
  }

  function getPositionsThatAreFull() {
    $positionsThatAreFull = array();
    foreach (self::$positionsToFill as $position) {
      if ($this->getOpeningsForPosition($position) == 0) {
        $positionsThatAreFull[] = $position;
      }
    }
    return $positionsThatAreFull;
  }

  function getOpeningsForPosition($wantedPosition) {
    $lineupSpots = $this->getCountOfLineupSpots($wantedPosition);
    $playersInPosition = $this->getPlayersInPosition($wantedPosition);
    return $lineupSpots - sizeof($playersInPosition);
  }

  function placePlayerIntoLineup($player, $position) {
    $openings = $this->getOpeningsForPosition($position);
    if ($openings > 0) {
      $playerPosition = array($position, $player);
      $this->playerPositions[] = $playerPosition;
      return true;
    }
    return false;
  }

  function getNumberOfPositionedPlayers() {
    return sizeof($this->playerPositions);
  }

  function getOpenPositions() {
    $openPositions = array();
    foreach (self::$positionsToFill as $position) {
      if ($this->getOpeningsForPosition($position) > 0) {
        $openPositions[$position] = $position;
      }
    }
    return $openPositions;
  }

  function __clone() {
    $lineup = new Lineup();
    foreach ($this->playerPositions as $playerPosition) {
      $position = $playerPosition[0];
      $player = $playerPosition[1];
      $lineup->placePlayerIntoLineup($player, $position);
    }
    return $lineup;
  }

  function printLineup() {
    echo "======== LINEUP ========<br>";
    foreach ($this->playerPositions as $playerPosition) {
      $position = $playerPosition[0];
      $player = $playerPosition[1];
      echo "$position: " . $player->getName() . "<br>";
    }
    echo "========================<br><br>";
  }
}

  // functions

  function getMyPlayers() {
    if (is_null(getCurrentUser())) {
      return array();
    }
    return getPlayersForTeam(getMyTeamID());
  }

  function getPlayersForTeam($teamID) {
    $sql = "SELECT PlayerID, PlayerString
              FROM DraftResults dr
              WHERE TeamID = $teamID AND BackedOut = 0
              ORDER BY PlayerID";
    $rs = query($sql);

    $players = array();
    while ($row = mysql_fetch_assoc($rs)) {
      $playerID = $row['PlayerID'];
      $playerStr = $row['PlayerString'];
      $players[] = new RosterPlayer($playerID, $playerStr);
    }
    return $players;
  }


  function buildLineup($players, $lineup) {
    global $maxSize;

    if (sizeof($players) < 1) {
      return array($lineup);
    }

    $playerTmp = array_slice($players, 0, 1);
    $player = $playerTmp[0];

    $remainingPlayers = array_slice($players, 1);
    $positions = $player->getEligibilities();

    $subLineups = array();

    foreach ($positions as $position) {
      $expandedLineup = clone $lineup;
      $playerAdded = $expandedLineup->placePlayerIntoLineup($player, $position);

      if (!$playerAdded && $position != "P") {
        $expandedLineup->placePlayerIntoLineup($player, "DH");
      }

      $playerCount = $expandedLineup->getNumberOfPositionedPlayers();

      $maxSize = max($maxSize, $playerCount);

      if (sizeof($remainingPlayers) > 0) {
        $subLineupsForPlayer = buildLineup($remainingPlayers, $expandedLineup);
        $subLineups = array_merge($subLineups, $subLineupsForPlayer);
      } elseif ($playerCount >= $maxSize) {
//        $expandedLineup->printLineup();
        return array($expandedLineup);
      } else {
        return array();
      }
    }

    return $subLineups;
  }

?>