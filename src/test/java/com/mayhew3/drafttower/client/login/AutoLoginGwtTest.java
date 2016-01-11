package com.mayhew3.drafttower.client.login;

import com.google.gwt.user.client.Cookies;
import com.mayhew3.drafttower.client.TestBase;
import com.mayhew3.drafttower.server.TeamDraftOrder;
import com.mayhew3.drafttower.shared.LoginResponse;

/**
 * Tests auto-login.
 */
public class AutoLoginGwtTest extends TestBase {

  public void testSuccessfulLogin() {
    Cookies.setCookie(LoginResponse.TEAM_TOKEN_COOKIE, "asdf");
    ginjector.getTeamTokens().put("asdf", new TeamDraftOrder(1));
    super.reset();
    assertFalse(isVisible("-login"));
    assertTrue(ginjector.getDraftStatus().getConnectedTeams().contains(1));
  }

  public void testBadLogin() {
    Cookies.setCookie(LoginResponse.TEAM_TOKEN_COOKIE, "asdf");
    super.reset();
    assertTrue(isVisible("-login"));
    assertFalse(ginjector.getDraftStatus().getConnectedTeams().contains(1));
    ginjector.getScheduler().flush();
    assertTrue(isFocused("-login-username"));
  }
}