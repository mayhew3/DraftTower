package com.mayhew3.drafttower.client;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.Cookies;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.mayhew3.drafttower.client.serverrpc.ServerRpc;
import com.mayhew3.drafttower.server.BindingAnnotations.TeamTokens;
import com.mayhew3.drafttower.server.*;
import com.mayhew3.drafttower.shared.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test implementation of {@link ServerRpc}.
 */
public class TestServerRpc implements ServerRpc {

  private final LoginHandler loginHandler;
  private final PlayerDataSource playerDataSource;
  private final TeamDataSource teamDataSource;
  private final BeanFactory beanFactory;

  private final Map<String, TeamDraftOrder> teamTokens;
  private Map<String, List<QueueEntry>> playerQueues = new HashMap<>();

  @Inject
  public TestServerRpc(LoginHandler loginHandler,
      PlayerDataSource playerDataSource,
      TeamDataSource teamDataSource,
      @TeamTokens Map<String, TeamDraftOrder> teamTokens,
      BeanFactory beanFactory) {
    this.loginHandler = loginHandler;
    this.playerDataSource = playerDataSource;
    this.teamDataSource = teamDataSource;
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
    String teamToken = requestBean.as().getTeamToken();
    response.setQueue(playerQueues.containsKey(teamToken)
        ? playerQueues.get(teamToken)
        : new ArrayList<QueueEntry>());
    callback.apply(response);
  }

  @Override
  public void sendEnqueueOrDequeueRequest(String action,
      AutoBean<EnqueueOrDequeuePlayerRequest> requestBean,
      Runnable callback) {
    EnqueueOrDequeuePlayerRequest request = requestBean.as();
    String teamToken = request.getTeamToken();
    if (!playerQueues.containsKey(teamToken)) {
      playerQueues.put(teamToken, new ArrayList<QueueEntry>());
    }
    List<QueueEntry> queue = playerQueues.get(teamToken);
    boolean removed = Iterables.removeIf(queue,
        new QueueEntryPredicate(request.getPlayerId()));
    if (!removed) {
      QueueEntry entry = beanFactory.createQueueEntry().as();
      Player player = ((TestPlayerDataSource) playerDataSource)
          .getPlayer(request.getPlayerId());
      entry.setPlayerId(player.getPlayerId());
      entry.setPlayerName(player.getName());
      entry.setEligibilities(RosterUtil.splitEligibilities(player.getEligibility()));
      if (request.getPosition() != null && !queue.isEmpty()) {
        queue.add(request.getPosition(), entry);
      } else {
        queue.add(entry);
      }
    }
    callback.run();
  }

  @Override
  public void sendReorderQueueRequest(AutoBean<ReorderPlayerQueueRequest> requestBean,
      Runnable callback) {
    ReorderPlayerQueueRequest request = requestBean.as();
    String teamToken = request.getTeamToken();
    if (!playerQueues.containsKey(teamToken)) {
      playerQueues.put(teamToken, new ArrayList<QueueEntry>());
    }
    List<QueueEntry> queue = playerQueues.get(teamToken);
    int oldPosition = Iterables.indexOf(queue,
        new QueueEntryPredicate(request.getPlayerId()));
    QueueEntry entry = queue.get(oldPosition);
    if (oldPosition != -1) {
      int newPosition = Math.min(request.getNewPosition(), queue.size());
      if (oldPosition != newPosition) {
        List<QueueEntry> newQueue = Lists.newArrayList(Iterables.concat(
            queue.subList(0, Math.min(oldPosition, newPosition)),
            newPosition < oldPosition
                ? Lists.newArrayList(entry)
                : Lists.<QueueEntry>newArrayList(),
            queue.subList(Math.min(oldPosition + 1, newPosition), Math.max(oldPosition, newPosition)),
            oldPosition < newPosition
                ? Lists.newArrayList(entry)
                : Lists.<QueueEntry>newArrayList(),
            queue.subList(Math.max(oldPosition + 1, newPosition), queue.size())));
        playerQueues.put(teamToken, newQueue);
      }
    }
    callback.run();
  }

  @Override
  public void sendGraphsRequest(AutoBean<GetGraphsDataRequest> requestBean,
      Function<GraphsData, Void> callback) {
    try {
      callback.apply(playerDataSource.getGraphsData(
          teamTokens.get(requestBean.as().getTeamToken())));
    } catch (DataSourceException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void sendPlayerListRequest(AutoBean<UnclaimedPlayerListRequest> requestBean, Function<UnclaimedPlayerListResponse, Void> callback) {
    try {
      callback.apply(playerDataSource.lookupUnclaimedPlayers(requestBean.as()));
    } catch (DataSourceException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void sendChangePlayerRankRequest(AutoBean<ChangePlayerRankRequest> requestBean, Runnable callback) {
    try {
      playerDataSource.changePlayerRank(requestBean.as());
      callback.run();
    } catch (DataSourceException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void sendCopyRanksRequest(AutoBean<CopyAllPlayerRanksRequest> requestBean, Runnable callback) {
    try {
      playerDataSource.copyTableSpecToCustom(requestBean.as());
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
}