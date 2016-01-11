package com.mayhew3.drafttower.shared;

/**
 * Request object sent when user toggles a favorite player.
 */
public interface AddOrRemoveFavoriteRequest {

  String getTeamToken();
  void setTeamToken(String teamToken);

  long getPlayerId();
  void setPlayerId(long playerId);

  boolean isAdd();
  void setAdd(boolean add);
}