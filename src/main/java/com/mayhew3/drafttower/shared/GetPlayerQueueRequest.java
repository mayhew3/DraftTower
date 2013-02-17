package com.mayhew3.drafttower.shared;

/**
 * Request object for players queue.
 */
public interface GetPlayerQueueRequest {

  String getTeamToken();
  void setTeamToken(String teamToken);
}