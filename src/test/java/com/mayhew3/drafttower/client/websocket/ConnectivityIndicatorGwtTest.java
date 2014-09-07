package com.mayhew3.drafttower.client.websocket;

import com.google.gwt.user.client.Cookies;
import com.mayhew3.drafttower.client.TestBase;
import com.mayhew3.drafttower.shared.LoginResponse;

/**
 * Tests connectivity indicator widget.
 */
public class ConnectivityIndicatorGwtTest extends TestBase {

  @Override
  public void gwtSetUp() {
    Cookies.removeCookie(LoginResponse.TEAM_TOKEN_COOKIE);
    super.gwtSetUp();
  }

  public void testConnectivityIndicatorGreenAfterLogin() {
    type(USERNAME, "1");
    type(PASSWORD, "1");
    click(LOGIN_BUTTON);

    assertTrue(hasStyle(CONNECTIVITY_INDICATOR, ConnectivityIndicator.CSS.connected()));
  }

  public void testConnectivityIndicatorRedAfterDisconnect() {
    type(USERNAME, "1");
    type(PASSWORD, "1");
    click(LOGIN_BUTTON);

    ginjector.getWebSocket().close();

    assertFalse(hasStyle(CONNECTIVITY_INDICATOR, ConnectivityIndicator.CSS.connected()));
  }
}