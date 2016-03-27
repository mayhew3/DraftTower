package com.mayhew3.drafttower.server;

import com.google.common.base.Function;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.mayhew3.drafttower.client.TeamsInfo;
import com.mayhew3.drafttower.client.websocket.Websocket;
import com.mayhew3.drafttower.client.websocket.WebsocketListener;
import com.mayhew3.drafttower.server.BindingAnnotations.TeamTokens;
import com.mayhew3.drafttower.shared.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Simulates web socket communication for tests.
 */
@Singleton
public class TestDraftTowerWebSocket implements DraftTowerWebSocket, Websocket {

  private final List<DraftCommandListener> serverListeners = new ArrayList<>();
  private WebsocketListener clientListener;
  private final TeamsInfo teamsInfo;
  private final BeanFactory beanFactory;
  private final CurrentTimeProvider currentTimeProvider;
  private final Map<String, TeamDraftOrder> teamTokens;

  private boolean clientOpened;

  @Inject
  public TestDraftTowerWebSocket(TeamsInfo teamsInfo,
      BeanFactory beanFactory,
      CurrentTimeProvider currentTimeProvider,
      @TeamTokens Map<String, TeamDraftOrder> teamTokens) {
    this.teamsInfo = teamsInfo;
    this.beanFactory = beanFactory;
    this.currentTimeProvider = currentTimeProvider;
    this.teamTokens = teamTokens;
  }

  // Server side.

  @Override
  public void addListener(DraftCommandListener listener) {
    serverListeners.add(listener);
  }

  @Override
  public void sendMessage(Function<? super String, String> messageForTeamToken) {
    clientListener.onMessage(messageForTeamToken.apply(teamTokens.keySet().iterator().next()));
  }

  @Override
  public void forceDisconnect(String teamToken, SocketTerminationReason reason) {
    // No-op.
  }

  // Client side.

  @Override
  public void addListener(WebsocketListener listener) {
    clientListener = listener;
  }

  @Override
  public void close() {
    clientOpened = false;
    for (DraftCommandListener serverListener : serverListeners) {
      serverListener.onClientDisconnected(teamsInfo.getTeamToken());
    }
    clientListener.onClose(SocketTerminationReason.UNKNOWN_REASON);
  }

  @Override
  public int getState() {
    return clientOpened ? 1 : 0;
  }

  @Override
  public void open() {
    clientOpened = true;
    clientListener.onOpen();
  }

  @Override
  public void send(String msg) {
    if (msg.startsWith(ServletEndpoints.CLOCK_SYNC)) {
      String clockSyncResponse = msg + ServletEndpoints.CLOCK_SYNC_SEP + currentTimeProvider.getCurrentTimeMillis();
      clientListener.onMessage(clockSyncResponse);
    } else {
      DraftCommand cmd = AutoBeanCodex.decode(beanFactory, DraftCommand.class, msg).as();
      try {
        for (DraftCommandListener listener : serverListeners) {
          listener.onDraftCommand(cmd);
        }
      } catch (TerminateSocketException e) {
        clientOpened = false;
        clientListener.onClose(e.getReason());
      }
    }
  }
}