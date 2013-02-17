package com.mayhew3.drafttower.shared;

/**
 * Request object to enqueue or dequeue a player.
 */
public interface EnqueueOrDequeuePlayerRequest {

  String getTeamToken();
  void setTeamToken(String teamToken);

  long getPlayerId();
  void setPlayerId(long playerId);

  Integer getPosition();
  void setPosition(Integer position);
}