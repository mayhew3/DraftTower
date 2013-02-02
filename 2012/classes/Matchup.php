<?php
/**
 * Created by IntelliJ IDEA.
 * User: Mayhew Seavey
 * Date: 3/14/11
 * Time: 11:31 PM
 * To change this template use File | Settings | File Templates.
 */
 
class Matchup {
  private $team1;
  private $team2;

  function __construct($team1, $team2) {
    $this->team1 = $team1;
    $this->team2 = $team2;
  }

  function getTeamStats($teamID) {
    if ($this->team1['TeamID'] == $teamID) {
      return $this->team1;
    } elseif ($this->team2['TeamID'] == $teamID) {
      return $this->team2;
    } else {
      die("Team with ID " . $teamID . " was not in this matchup.");
    }
  }

  function getHeadToHead($teamID) {
    $team = $this->getTeamStats($teamID);
    return array($team['BFPTS'], $team['PFPTS']);
  }


}
