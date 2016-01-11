package com.mayhew3.drafttower.client;

import com.google.common.base.Function;
import com.google.gwt.user.client.Cookies;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.mayhew3.drafttower.client.serverrpc.ServerRpc;
import com.mayhew3.drafttower.server.BindingAnnotations.TeamTokens;
import com.mayhew3.drafttower.server.*;
import com.mayhew3.drafttower.shared.*;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * Test implementation of {@link ServerRpc}.
 */
public class TestServerRpc implements ServerRpc {

  private final LoginHandler loginHandler;
  private final PlayerDataProvider playerDataProvider;
  private final TeamDataSource teamDataSource;
  private final QueueHandler queueHandler;
  private final CloserLimitsHandler closerLimitsHandler;
  private final BeanFactory beanFactory;

  private final Map<String, TeamDraftOrder> teamTokens;

  @Inject
  public TestServerRpc(LoginHandler loginHandler,
      PlayerDataProvider playerDataProvider,
      TeamDataSource teamDataSource,
      QueueHandler queueHandler, 
      CloserLimitsHandler closerLimitsHandler,
      @TeamTokens Map<String, TeamDraftOrder> teamTokens,
      BeanFactory beanFactory) {
    this.loginHandler = loginHandler;
    this.playerDataProvider = playerDataProvider;
    this.teamDataSource = teamDataSource;
    this.queueHandler = queueHandler;
    this.closerLimitsHandler = closerLimitsHandler;
    this.teamTokens = teamTokens;
    this.beanFactory = beanFactory;
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
    GetPlayerQueueResponse response = beanFactory.createPlayerQueueResponse().as();
    response.setQueue(queueHandler.getQueue(teamTokens.get(requestBean.as().getTeamToken())));
    callback.apply(response);
  }

  @Override
  public void sendEnqueueOrDequeueRequest(String action,
      AutoBean<EnqueueOrDequeuePlayerRequest> requestBean,
      Runnable callback) {
    EnqueueOrDequeuePlayerRequest request = requestBean.as();
    TeamDraftOrder team = teamTokens.get(request.getTeamToken());
    long playerId = request.getPlayerId();
    Integer position = request.getPosition();
    if (action.equals(ServletEndpoints.QUEUE_ADD)) {
      try {
        queueHandler.enqueue(team, playerId, position);
      } catch (DataSourceException e) {
        throw new RuntimeException(e);
      }
    } else {
      queueHandler.dequeue(team, playerId);
    }
    callback.run();
  }

  @Override
  public void sendReorderQueueRequest(AutoBean<ReorderPlayerQueueRequest> requestBean,
      Runnable callback) {
    ReorderPlayerQueueRequest request = requestBean.as();
    TeamDraftOrder team = teamTokens.get(request.getTeamToken());
    long playerId = request.getPlayerId();
    int newPosition = request.getNewPosition();
    queueHandler.reorderQueue(team, playerId, newPosition);
    callback.run();
  }

  @Override
  public void sendGraphsRequest(AutoBean<GetGraphsDataRequest> requestBean,
      Function<GraphsData, Void> callback) {
    try {
      callback.apply(playerDataProvider.getGraphsData(
          teamTokens.get(requestBean.as().getTeamToken())));
    } catch (DataSourceException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void sendPlayerListRequest(AutoBean<UnclaimedPlayerListRequest> requestBean, Function<UnclaimedPlayerListResponse, Void> callback) {
    try {
      callback.apply(playerDataProvider.lookupUnclaimedPlayers(requestBean.as()));
    } catch (DataSourceException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void sendChangePlayerRankRequest(AutoBean<ChangePlayerRankRequest> requestBean, Runnable callback) {
    try {
      playerDataProvider.changePlayerRank(requestBean.as());
      callback.run();
    } catch (DataSourceException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void sendCopyRanksRequest(AutoBean<CopyAllPlayerRanksRequest> requestBean, Runnable callback) {
    try {
      playerDataProvider.copyTableSpecToCustom(requestBean.as());
      callback.run();
    } catch (DataSourceException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void sendSetWizardTableRequest(AutoBean<SetWizardTableRequest> requestBean, Runnable callback) {
    SetWizardTableRequest request = requestBean.as();
    teamDataSource.updateAutoPickWizard(
        teamTokens.get(request.getTeamToken()), request.getPlayerDataSet());
    callback.run();
  }

  @Override
  public void sendSetCloserLimitsRequest(AutoBean<SetCloserLimitRequest> requestBean, Runnable callback) {
    SetCloserLimitRequest request = requestBean.as();
    closerLimitsHandler.setCloserLimits(
        teamTokens.get(request.getTeamToken()), request.getMinClosers(), request.getMaxClosers());
  }

  @Override
  public void sendAddOrRemoveFavoriteRequest(AutoBean<AddOrRemoveFavoriteRequest> requestBean, Runnable callback) {
    try {
      playerDataProvider.addOrRemoveFavorite(requestBean.as());
      callback.run();
    } catch (DataSourceException e) {
      throw new RuntimeException(e);
    }
  }
}