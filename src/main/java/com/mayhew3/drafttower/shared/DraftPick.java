package com.mayhew3.drafttower.shared;

import java.util.List;

/**
 * Represents a single pick.
 */
public interface DraftPick {
  int getTeam();
  void setTeam(int team);

  long getPlayerId();
  void setPlayerId(long playerId);

  String getPlayerName();
  void setPlayerName(String playerName);

  List<String> getEligibilities();
  void setEligibilities(List<String> eligibilities);

  boolean isKeeper();
  void setKeeper(boolean keeper);

  boolean isCloser();
  void setCloser(boolean closer);
}