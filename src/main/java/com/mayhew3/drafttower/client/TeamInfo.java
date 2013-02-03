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
  private LoginResponse value;

  @Inject
  public TeamInfo(@Commissioner int commissionerTeam) {
    this.commissionerTeam = commissionerTeam;
  }

  public LoginResponse getValue() {
    return value;
  }

  public void setValue(LoginResponse value) {
    this.value = value;
  }

  public boolean isCommissionerTeam() {
    return value.getTeam() == commissionerTeam;
  }

  public boolean isLoggedIn() {
    return value != null;
  }
}