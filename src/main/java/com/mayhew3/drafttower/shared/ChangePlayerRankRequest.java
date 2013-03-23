package com.mayhew3.drafttower.shared;

/**
 * Request object sent when user changes a player's rank.
 */
public interface ChangePlayerRankRequest {

  String getTeamToken();
  void setTeamToken(String teamToken);

  long getPlayerId();
  void setPlayerId(long playerId);

  int getNewRank();
  void setNewRank(int newRank);

  int getPrevRank();
  void setPrevRank(int newRank);
}