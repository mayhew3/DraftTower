package com.mayhew3.drafttower.shared;

/**
 * Request object to enqueue or dequeue a player.
 */
public interface ReorderPlayerQueueRequest {

  String getTeamToken();
  void setTeamToken(String teamToken);

  long getPlayerId();
  void setPlayerId(long playerId);

  int getNewPosition();
  void setNewPosition(int newPosition);
}