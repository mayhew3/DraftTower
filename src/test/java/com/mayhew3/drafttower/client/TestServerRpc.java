package com.mayhew3.drafttower.client;

import com.google.common.base.Function;
import com.google.gwt.user.client.Cookies;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.mayhew3.drafttower.client.serverrpc.ServerRpc;
import com.mayhew3.drafttower.server.DataSourceException;
import com.mayhew3.drafttower.server.LoginHandler;
import com.mayhew3.drafttower.shared.*;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * Test implementation of {@link com.mayhew3.drafttower.client.serverrpc.ServerRpc}.
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
    } catch (DataSourceException e) {
      failureCallback.apply(SocketTerminationReason.UNKNOWN_REASON);
    }
  }

  @Override
  public void sendGetPlayerQueueRequest(AutoBean<GetPlayerQueueRequest> requestBean,
      Function<GetPlayerQueueResponse, Void> callback) {
    // TODO(kprevas): implement
  }

  @Override
  public void sendEnqueueOrDequeueRequest(String action,
      AutoBean<EnqueueOrDequeuePlayerRequest> requestBean,
      Runnable callback) {
    // TODO(kprevas): implement
  }

  @Override
  public void sendReorderQueueRequest(AutoBean<ReorderPlayerQueueRequest> requestBean,
      Runnable callback) {
    // TODO(kprevas): implement
  }

  @Override
  public void sendGraphsRequest(AutoBean<GetGraphsDataRequest> requestBean, Function<GraphsData, Void> callback) {
    // TODO(kprevas): implement
  }

  @Override
  public void sendPlayerListRequest(AutoBean<UnclaimedPlayerListRequest> requestBean, Function<UnclaimedPlayerListResponse, Void> callback) {
    // TODO(kprevas): implement
  }

  @Override
  public void sendChangePlayerRankRequest(AutoBean<ChangePlayerRankRequest> requestBean, Runnable callback) {
    // TODO(kprevas): implement
  }

  @Override
  public void sendCopyRanksRequest(AutoBean<CopyAllPlayerRanksRequest> requestBean, Runnable callback) {
    // TODO(kprevas): implement
  }

  @Override
  public void sendSetWizardTableRequest(AutoBean<SetWizardTableRequest> requestBean, Runnable callback) {
    // TODO(kprevas): implement
  }
}