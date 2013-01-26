package com.mayhew3.drafttower.shared;

import java.util.Map;

/**
 * Data object for a player.
 */
public interface Player {

  long getPlayerId();
  void setPlayerId(long playerId);

  Map<PlayerColumn, String> getColumnValues();
  void setColumnValues(Map<PlayerColumn, String> columnValues);
}