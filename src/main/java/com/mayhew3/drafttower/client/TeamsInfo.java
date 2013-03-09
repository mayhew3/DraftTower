package com.mayhew3.drafttower.client;

import com.google.inject.Singleton;
import com.mayhew3.drafttower.shared.LoginResponse;

/**
 * Info about teams, including this client's team.  Holds a {@link LoginResponse}.
 */
@Singleton
public class TeamsInfo {

  private LoginResponse loginResponse;

  public String getTeamToken() {
    return loginResponse.getTeamToken();
  }

  public int getTeam() {
    return loginResponse.getTeam();
  }

  public String getShortTeamName(int team) {
    if (loginResponse == null) {
      return "Team " + team;
    }
    return loginResponse.getTeams().get(Integer.toString(team)).getShortName();
  }

  public String getLongTeamName(int team) {
    if (loginResponse == null) {
      return "Team " + team;
    }
    return loginResponse.getTeams().get(Integer.toString(team)).getLongName();
  }

  public void setLoginResponse(LoginResponse loginResponse) {
    this.loginResponse = loginResponse;
  }

  public boolean isCommissionerTeam() {
    return loginResponse.isCommissionerTeam();
  }

  public boolean isLoggedIn() {
    return loginResponse != null;
  }
}