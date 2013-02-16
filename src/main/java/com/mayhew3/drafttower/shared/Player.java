package com.mayhew3.drafttower.shared;

import java.util.Map;

/**
 * Data object for a player.
 */
public interface Player {

  // Marker constant for best draft pick.
  public static final long BEST_DRAFT_PICK = -1;

  long getPlayerId();
  void setPlayerId(long playerId);

  Map<PlayerColumn, String> getColumnValues();
  void setColumnValues(Map<PlayerColumn, String> columnValues);

  String getInjury();
  void setInjury(String injury);
}