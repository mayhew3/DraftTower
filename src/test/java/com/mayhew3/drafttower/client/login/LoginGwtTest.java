package com.mayhew3.drafttower.client.login;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Cookies;
import com.mayhew3.drafttower.client.TestBase;
import com.mayhew3.drafttower.server.TeamDraftOrder;
import com.mayhew3.drafttower.server.TestTeamDataSource;
import com.mayhew3.drafttower.shared.DraftStatus;
import com.mayhew3.drafttower.shared.LoginResponse;

import java.util.Map;

/**
 * Tests logging in.
 */
public class LoginGwtTest extends TestBase {

  private DraftStatus draftStatus;
  private Map<String,TeamDraftOrder> teamTokens;

  @Override
  public void gwtSetUp() {
    Cookies.removeCookie(LoginResponse.TEAM_TOKEN_COOKIE);
    super.gwtSetUp();
    draftStatus = ginjector.getDraftStatus();
    teamTokens = ginjector.getTeamTokens();
  }

  public void testUsernameFocused() {
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      @Override
      public void execute() {
        assertTrue(isFocused(USERNAME));
        finishTest();
      }
    });
    delayTestFinish(500);
  }

  public void testSuccessfulLoginButton() {
    type(USERNAME, "1");
    type(PASSWORD, "1");
    click(LOGIN_BUTTON);

    assertFalse(isVisible(LOGIN_WIDGET));
    assertTrue(teamTokens.values().contains(new TeamDraftOrder(1)));
  }

  public void testSuccessfulLoginEnterKey() {
    type(USERNAME, "1");
    type(PASSWORD, "1");
    pressKey(PASSWORD, KeyCodes.KEY_ENTER);

    assertFalse(isVisible(LOGIN_WIDGET));
    assertTrue(teamTokens.values().contains(new TeamDraftOrder(1)));
  }

  public void testBadLogin() {
    type(USERNAME, "1");
    type(PASSWORD, TestTeamDataSource.BAD_PASSWORD);
    click(LOGIN_BUTTON);

    assertTrue(isVisible(LOGIN_WIDGET));
    assertTrue(isVisible(INVALID_LOGIN));
    assertFalse(teamTokens.values().contains(new TeamDraftOrder(1)));
  }

  public void testDuplicateLogin() {
    draftStatus.getConnectedTeams().add(1);

    type(USERNAME, "1");
    type(PASSWORD, "1");
    click(LOGIN_BUTTON);

    assertTrue(isVisible(LOGIN_WIDGET));
    assertTrue(isVisible(ALREADY_LOGGED_IN));
  }

  public void testLogout() {
    login(1);
    click(LOGOUT_LINK);
    assertNull(Cookies.getCookie(LoginResponse.TEAM_TOKEN_COOKIE));
  }
}