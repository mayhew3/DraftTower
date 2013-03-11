package com.mayhew3.drafttower.shared;

import java.util.List;
import java.util.Set;

/**
 * Message object for communicating current draft status to clients.
 */
public interface DraftStatus {

  long getCurrentPickDeadline();
  void setCurrentPickDeadline(long deadline);

  boolean isPaused();
  void setPaused(boolean paused);

  Set<Integer> getConnectedTeams();
  void setConnectedTeams(Set<Integer> connectedTeams);

  Set<Integer> getRobotTeams();
  void setRobotTeams(Set<Integer> robotTeams);

  int getCurrentTeam();
  void setCurrentTeam(int currentTeam);

  List<DraftPick> getPicks();
  void setPicks(List<DraftPick> picks);

  boolean isOver();
  void setOver(boolean over);
}