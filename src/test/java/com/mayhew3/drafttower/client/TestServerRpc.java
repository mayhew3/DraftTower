package com.mayhew3.drafttower.client;

import com.google.common.base.Function;
import com.google.gwt.user.client.Cookies;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.mayhew3.drafttower.server.LoginHandler;
import com.mayhew3.drafttower.shared.LoginResponse;
import com.mayhew3.drafttower.shared.SocketTerminationReason;

import javax.inject.Inject;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Test implementation of {@link ServerRpc}.
 */
public class TestServerRpc implements ServerRpc {

  private final LoginHandler loginHandler;

  @Inject
  public TestServerRpc(LoginHandler loginHandler) {
    this.loginHandler = loginHandler;
  }

  @Override
  public void sendLoginRequest(String username, String password,
      Function<LoginResponse, Void> successCallback,
      Function<SocketTerminationReason, Void> failureCallback) {
    Map<String, String> cookiesMap = new HashMap<>();
    for (String cookieName : Cookies.getCookieNames()) {
      cookiesMap.put(cookieName, Cookies.getCookie(cookieName));
    }
    try {
      AutoBean<LoginResponse> responseBean =
          loginHandler.doLogin(cookiesMap, username, password);
      if (responseBean != null) {
        LoginResponse loginResponse = responseBean.as();
        if (loginResponse.getTeamToken() != null) {
          Cookies.setCookie(LoginResponse.TEAM_TOKEN_COOKIE,
              loginResponse.getTeamToken());
        }
        successCallback.apply(loginResponse);
      } else {
        failureCallback.apply(SocketTerminationReason.BAD_TEAM_TOKEN);
      }
    } catch (IOException | ServletException e) {
      failureCallback.apply(SocketTerminationReason.UNKNOWN_REASON);
    }
  }
}