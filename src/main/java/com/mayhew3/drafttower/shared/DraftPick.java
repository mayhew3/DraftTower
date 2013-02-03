package com.mayhew3.drafttower.shared;

/**
 * Represents a single pick.
 */
public interface DraftPick {
  int getTeam();
  void setTeam(int team);

  String getTeamName();
  void setTeamName(String teamName);

  long getPlayerId();
  void setPlayerId(long playerId);

  String getPlayerName();
  void setPlayerName(String playerName);
}