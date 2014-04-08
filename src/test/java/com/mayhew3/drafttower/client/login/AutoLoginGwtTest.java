package com.mayhew3.drafttower.client.login;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.Cookies;
import com.mayhew3.drafttower.client.TestBase;
import com.mayhew3.drafttower.server.ServerTestSafeModule;
import com.mayhew3.drafttower.server.TeamDraftOrder;
import com.mayhew3.drafttower.shared.LoginResponse;

import java.util.HashMap;

/**
 * Tests auto-login.
 */
public class AutoLoginGwtTest extends TestBase {

  public void testSuccessfulLogin() {
    Cookies.setCookie(LoginResponse.TEAM_TOKEN_COOKIE, "asdf");
    ServerTestSafeModule.teamTokensForTest = new HashMap<>();
    ServerTestSafeModule.teamTokensForTest.put("asdf", new TeamDraftOrder(1));
    try {
      super.gwtSetUp();
      assertFalse(isVisible("-login"));
      assertFalse(ginjector.getDraftStatus().getConnectedTeams().contains(1));
    } finally {
      ServerTestSafeModule.teamTokensForTest = null;
    }
  }

  public void testBadLogin() {
    Cookies.setCookie(LoginResponse.TEAM_TOKEN_COOKIE, "asdf");
    super.gwtSetUp();
    assertTrue(isVisible("-login"));
    assertFalse(ginjector.getDraftStatus().getConnectedTeams().contains(1));
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      @Override
      public void execute() {
        assertTrue(isFocused("-login-username"));
        finishTest();
      }
    });
    delayTestFinish(500);
  }
}