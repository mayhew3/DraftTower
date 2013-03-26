package com.mayhew3.drafttower.shared;

import java.util.Map;

/**
 * Response to successful login.
 */
public interface LoginResponse {
  String getTeamToken();
  void setTeamToken(String teamToken);

  int getTeam();
  void setTeam(int team);

  boolean isCommissionerTeam();
  void setCommissionerTeam(boolean commissionerTeam);

  // Can't use Integer as key - see https://code.google.com/p/google-web-toolkit/issues/detail?id=7395
  Map<String, Team> getTeams();
  void setTeams(Map<String, Team> teams);

  TableSpec getInitialTableSpec();
}