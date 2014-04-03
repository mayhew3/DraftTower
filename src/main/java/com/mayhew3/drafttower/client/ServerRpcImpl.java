package com.mayhew3.drafttower.client;

import com.google.common.base.Function;
import com.google.common.net.HttpHeaders;
import com.google.gwt.http.client.*;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.mayhew3.drafttower.server.GinBindingAnnotations.LoginUrl;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.LoginResponse;
import com.mayhew3.drafttower.shared.SocketTerminationReason;

import javax.inject.Inject;

import static com.google.gwt.http.client.RequestBuilder.POST;

/**
 * Live implementation of {@link ServerRpc}.
 */
public class ServerRpcImpl implements ServerRpc {

  private final String loginUrl;
  private final BeanFactory beanFactory;

  @Inject
  public ServerRpcImpl(@LoginUrl String loginUrl,
      BeanFactory beanFactory) {
    this.loginUrl = loginUrl;
    this.beanFactory = beanFactory;
  }

  @Override
  public void sendLoginRequest(String username, String password,
      final Function<LoginResponse, Void> responseCallback,
      final Function<SocketTerminationReason, Void> failureCallback) {
    RequestBuilder requestBuilder = new RequestBuilder(POST, loginUrl);
    requestBuilder.setHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
    try {
      requestBuilder.sendRequest("username=" + username + "&password=" + password,
          new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
              if (response.getStatusCode() == 200) {
                LoginResponse loginResponse =
                    AutoBeanCodex.decode(beanFactory, LoginResponse.class, response.getText()).as();
                responseCallback.apply(loginResponse);
              } else {
                failureCallback.apply(SocketTerminationReason.BAD_TEAM_TOKEN);
              }
            }

            @Override
            public void onError(Request request, Throwable exception) {
              failureCallback.apply(SocketTerminationReason.UNKNOWN_REASON);
            }
          });
    } catch (RequestException e) {
      failureCallback.apply(SocketTerminationReason.UNKNOWN_REASON);
    }
  }
}