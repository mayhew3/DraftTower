package com.mayhew3.drafttower.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayhew3.drafttower.shared.DraftStatus;
import com.mayhew3.drafttower.shared.LoginResponse;
import com.mayhew3.drafttower.shared.SharedModule.NumTeams;

/**
 * Info about teams, including this client's team.  Holds a {@link LoginResponse}.
 */
@Singleton
public class TeamsInfo {

  private final int numTeams;

  private LoginResponse loginResponse;

  @Inject
  public TeamsInfo(@NumTeams int numTeams) {
    this.numTeams = numTeams;
  }

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

  public boolean isGuest() {
    return loginResponse.getTeam() == -1;
  }

  public boolean isMyPick(DraftStatus status) {
    return status.getCurrentTeam() == getTeam();
  }

  public boolean isOnDeck(DraftStatus status) {
    int nextTeam = status.getCurrentTeam() + 1;
    if (nextTeam > numTeams) {
      nextTeam -= numTeams;
    }
    while (status.getNextPickKeeperTeams().contains(nextTeam)) {
      nextTeam++;
      if (nextTeam > numTeams) {
        nextTeam -= numTeams;
      }
    }
    return nextTeam == getTeam();
  }

  public int getWebSocketPort() {
    return loginResponse.getWebSocketPort();
  }
}