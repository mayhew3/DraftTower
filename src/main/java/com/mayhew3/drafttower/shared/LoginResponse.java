package com.mayhew3.drafttower.shared;

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
}