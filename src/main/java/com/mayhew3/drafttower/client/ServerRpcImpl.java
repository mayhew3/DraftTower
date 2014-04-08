package com.mayhew3.drafttower.client;

import com.google.common.base.Function;
import com.google.common.net.HttpHeaders;
import com.google.gwt.http.client.*;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.mayhew3.drafttower.server.GinBindingAnnotations.*;
import com.mayhew3.drafttower.shared.*;

import javax.inject.Inject;

import static com.google.gwt.http.client.RequestBuilder.POST;

/**
 * Live implementation of {@link ServerRpc}.
 */
public class ServerRpcImpl implements ServerRpc {

  private final String loginUrl;
  private final String queuesUrl;
  private final String graphsUrl;
  private final String playerInfoUrl;
  private final String changePlayerRankUrl;
  private final String copyPlayerRanksUrl;
  private final String setAutoPickWizardUrl;
  private final BeanFactory beanFactory;

  @Inject
  public ServerRpcImpl(@LoginUrl String loginUrl,
      @QueuesUrl String queuesUrl,
      @GraphsUrl String graphsUrl,
      @UnclaimedPlayerInfoUrl String playerInfoUrl,
      @ChangePlayerRankUrl String changePlayerRankUrl,
      @CopyPlayerRanksUrl String copyPlayerRanksUrl,
      @SetAutoPickWizardUrl String setAutoPickWizardUrl,
      BeanFactory beanFactory) {
    this.loginUrl = loginUrl;
    this.queuesUrl = queuesUrl;
    this.graphsUrl = graphsUrl;
    this.playerInfoUrl = playerInfoUrl;
    this.changePlayerRankUrl = changePlayerRankUrl;
    this.copyPlayerRanksUrl = copyPlayerRanksUrl;
    this.setAutoPickWizardUrl = setAutoPickWizardUrl;
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

  @Override
  public void sendGetPlayerQueueRequest(AutoBean<GetPlayerQueueRequest> requestBean,
      final Function<GetPlayerQueueResponse, Void> callback) {
    sendRequest(queuesUrl + "/" + ServletEndpoints.QUEUE_GET,
        requestBean, GetPlayerQueueResponse.class, callback);
  }

  @Override
  public void sendEnqueueOrDequeueRequest(String action,
      AutoBean<EnqueueOrDequeuePlayerRequest> requestBean,
      Runnable callback) {
    sendRequest(queuesUrl + "/" + action, requestBean, callback);
  }

  @Override
  public void sendReorderQueueRequest(AutoBean<ReorderPlayerQueueRequest> requestBean,
      Runnable callback) {
    sendRequest(queuesUrl + "/" + ServletEndpoints.QUEUE_REORDER, requestBean, callback);
  }

  @Override
  public void sendGraphsRequest(AutoBean<GetGraphsDataRequest> requestBean,
      final Function<GraphsData, Void> callback) {
    sendRequest(graphsUrl, requestBean, GraphsData.class, callback);
  }

  @Override
  public void sendPlayerListRequest(AutoBean<UnclaimedPlayerListRequest> requestBean,
      final Function<UnclaimedPlayerListResponse, Void> callback) {
    sendRequest(playerInfoUrl, requestBean, UnclaimedPlayerListResponse.class, callback);
  }

  @Override
  public void sendChangePlayerRankRequest(AutoBean<ChangePlayerRankRequest> requestBean,
      Runnable callback) {
    sendRequest(changePlayerRankUrl, requestBean, callback);
  }

  @Override
  public void sendCopyRanksRequest(AutoBean<CopyAllPlayerRanksRequest> requestBean,
      Runnable callback) {
    sendRequest(copyPlayerRanksUrl, requestBean, callback);
  }

  @Override
  public void sendSetWizardTableRequest(AutoBean<SetWizardTableRequest> requestBean, Runnable callback) {
    sendRequest(setAutoPickWizardUrl, requestBean, callback);
  }

  private <Q> void sendRequest(String url,
      AutoBean<Q> requestBean,
      final Runnable callback) {
    RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.POST, url);

    RequestCallbackWithBackoff.sendRequest(requestBuilder,
        AutoBeanCodex.encode(requestBean).getPayload(),
        new RequestCallbackWithBackoff() {
          @Override
          public void onResponseReceived(Request request, Response response) {
            callback.run();
          }
        });
  }

  private <Q, R> void sendRequest(String url,
      AutoBean<Q> requestBean,
      final Class<R> responseClass,
      final Function<R, Void> callback) {
    RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.POST, url);

    RequestCallbackWithBackoff.sendRequest(requestBuilder,
        AutoBeanCodex.encode(requestBean).getPayload(),
        new RequestCallbackWithBackoff() {
          @Override
          public void onResponseReceived(Request request, Response response) {
            R decodedResponse = AutoBeanCodex.decode(
                beanFactory, responseClass, response.getText()).as();
            callback.apply(decodedResponse);
          }
        });
  }
}