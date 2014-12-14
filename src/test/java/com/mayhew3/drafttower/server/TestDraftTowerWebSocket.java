package com.mayhew3.drafttower.server;

import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.mayhew3.drafttower.client.TeamsInfo;
import com.mayhew3.drafttower.client.websocket.Websocket;
import com.mayhew3.drafttower.client.websocket.WebsocketListener;
import com.mayhew3.drafttower.shared.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Simulates web socket communication for tests.
 */
@Singleton
public class TestDraftTowerWebSocket implements DraftTowerWebSocket, Websocket {

  private final List<DraftCommandListener> serverListeners = new ArrayList<>();
  private final List<WebsocketListener> clientListeners = new ArrayList<>();
  private final TeamsInfo teamsInfo;
  private final BeanFactory beanFactory;
  private final CurrentTimeProvider currentTimeProvider;

  private boolean clientOpened;

  @Inject
  public TestDraftTowerWebSocket(TeamsInfo teamsInfo,
      BeanFactory beanFactory,
      CurrentTimeProvider currentTimeProvider) {
    this.teamsInfo = teamsInfo;
    this.beanFactory = beanFactory;
    this.currentTimeProvider = currentTimeProvider;
  }

  // Server side.

  @Override
  public void addListener(DraftCommandListener listener) {
    serverListeners.add(listener);
  }

  @Override
  public void sendMessage(String message) {
    for (WebsocketListener clientListener : clientListeners) {
      clientListener.onMessage(message);
    }
  }

  // Client side.

  @Override
  public void addListener(WebsocketListener listener) {
    clientListeners.add(listener);
  }

  @Override
  public void close() {
    clientOpened = false;
    for (DraftCommandListener serverListener : serverListeners) {
      serverListener.onClientDisconnected(teamsInfo.getTeamToken());
    }
    for (WebsocketListener clientListener : clientListeners) {
      clientListener.onClose(SocketTerminationReason.UNKNOWN_REASON);
    }
  }

  @Override
  public int getState() {
    return clientOpened ? 1 : 0;
  }

  @Override
  public void open() {
    clientOpened = true;
    for (DraftCommandListener serverListener : serverListeners) {
      serverListener.onClientConnected();
    }
    for (WebsocketListener clientListener : clientListeners) {
      clientListener.onOpen();
    }
  }

  @Override
  public void send(String msg) {
    if (msg.startsWith(ServletEndpoints.CLOCK_SYNC)) {
      String clockSyncResponse = msg + ServletEndpoints.CLOCK_SYNC_SEP + currentTimeProvider.getCurrentTimeMillis();
      for (WebsocketListener clientListener : clientListeners) {
        clientListener.onMessage(clockSyncResponse);
      }
    } else {
      DraftCommand cmd = AutoBeanCodex.decode(beanFactory, DraftCommand.class, msg).as();
      try {
        for (DraftCommandListener listener : serverListeners) {
          listener.onDraftCommand(cmd);
        }
      } catch (TerminateSocketException e) {
        clientOpened = false;
        for (WebsocketListener clientListener : clientListeners) {
          clientListener.onClose(e.getReason());
        }
      }
    }
  }
}