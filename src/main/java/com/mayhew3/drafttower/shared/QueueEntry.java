package com.mayhew3.drafttower.shared;

import java.util.List;

/**
 * Represents an entry in the player queue.
 */
public interface QueueEntry {
  long getPlayerId();
  void setPlayerId(long playerId);

  String getPlayerName();
  void setPlayerName(String playerName);

  List<String> getEligibilities();
  void setEligibilities(List<String> eligibilities);
}