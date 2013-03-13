package com.mayhew3.drafttower.shared;

/**
 * Request object for bar graphs data.
 */
public interface GetGraphsDataRequest {

  String getTeamToken();
  void setTeamToken(String teamToken);
}