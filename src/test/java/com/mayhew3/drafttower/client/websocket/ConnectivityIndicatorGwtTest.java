package com.mayhew3.drafttower.client.websocket;

import com.mayhew3.drafttower.client.TestBase;

/**
 * Tests connectivity indicator widget.
 */
public class ConnectivityIndicatorGwtTest extends TestBase {

  public void testConnectivityIndicatorGreenAfterLogin() {
    login(1);
    assertTrue(hasStyle(CONNECTIVITY_INDICATOR, ConnectivityIndicator.CSS.connected()));
  }

  public void testConnectivityIndicatorRedAfterDisconnect() {
    login(1);
    testComponent.webSocket().close();
    assertFalse(hasStyle(CONNECTIVITY_INDICATOR, ConnectivityIndicator.CSS.connected()));
  }

  public void testConnectivityIndicatorGreenAfterDisconnectAndAutoReconnect() {
    login(1);
    testComponent.webSocket().close();
    testComponent.scheduler().flush();
    assertTrue(hasStyle(CONNECTIVITY_INDICATOR, ConnectivityIndicator.CSS.connected()));
  }
}