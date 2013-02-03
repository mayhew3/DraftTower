package com.mayhew3.drafttower.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayhew3.drafttower.shared.LoginResponse;
import com.mayhew3.drafttower.shared.SharedModule.Commissioner;

/**
 * Info about this client's team.  Holds a {@link LoginResponse}.
 */
@Singleton
public class TeamInfo {

  private final int commissionerTeam;
  private LoginResponse loginResponse;

  @Inject
  public TeamInfo(@Commissioner int commissionerTeam) {
    this.commissionerTeam = commissionerTeam;
  }

  public String getTeamToken() {
    return loginResponse.getTeamToken();
  }

  public int getTeam() {
    return loginResponse.getTeam();
  }

  public void setLoginResponse(LoginResponse loginResponse) {
    this.loginResponse = loginResponse;
  }

  public boolean isCommissionerTeam() {
    return loginResponse.getTeam() == commissionerTeam;
  }

  public boolean isLoggedIn() {
    return loginResponse != null;
  }
}