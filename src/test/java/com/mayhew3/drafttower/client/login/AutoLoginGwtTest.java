package com.mayhew3.drafttower.client.login;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
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
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      @Override
      public void execute() {
        assertTrue(isFocused("-login-username"));
        finishTest();
      }
    });
    delayTestFinish(500);
  }

  public void testDuplicateLogin() {
    Cookies.setCookie(LoginResponse.TEAM_TOKEN_COOKIE, "asdf");
    ginjector.getTeamTokens().put("asdf", new TeamDraftOrder(1));
    ginjector.getDraftStatus().getConnectedTeams().add(1);
    super.reset();
    assertTrue(isVisible("-login"));
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