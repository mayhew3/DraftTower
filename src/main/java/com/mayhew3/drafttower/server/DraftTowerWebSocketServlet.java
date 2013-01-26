package com.mayhew3.drafttower.server;

import com.google.common.collect.Sets;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.DraftStatus;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Servlet for WebSocket communication with clients.
 */
public class DraftTowerWebSocketServlet extends WebSocketServlet {

  // TODO: factor out draft status to a dependency.
  private static final long PICK_LENGTH_MS = 90 * 1000;
  private static final BeanFactory beanFactory = AutoBeanFactorySource.create(BeanFactory.class);
  private long currentPickDeadline;
  private boolean paused;
  private long pausedPickTime;
  private ScheduledThreadPoolExecutor pickTimer = new ScheduledThreadPoolExecutor(1);
  @Nullable private ScheduledFuture currentPickTimer;

  private class DraftTowerWebSocket implements WebSocket.OnTextMessage {

    private Connection connection;

    public void onOpen(Connection connection) {
      openSockets.add(this);
      this.connection = connection;
    }

    public void sendMessage(String data) {
      try {
        connection.sendMessage(data);
      } catch (IOException e) {
        // TODO?
        e.printStackTrace();
      }
    }

    public void sendMessage(AutoBean message) {
      sendMessage(AutoBeanCodex.encode(message).getPayload());
    }

    public void onMessage(String data) {
      // TODO: parse structured request
      if (data.equals("start") || data.equals("doPick")) {
        newPick();
      } else if (data.equals("pause")) {
        pausePick();
      } else if (data.equals("resume")) {
        resumePick();
      }
    }

    public void onClose(int closeCode, String message) {
      openSockets.remove(this);
    }
  }

  private void newPick() {
    cancelPickTimer();
    currentPickDeadline = System.currentTimeMillis() + PICK_LENGTH_MS;
    paused = false;
    startPickTimer(PICK_LENGTH_MS);
    sendStatusUpdates();
  }

  private void pausePick() {
    cancelPickTimer();
    paused = true;
    pausedPickTime = currentPickDeadline - System.currentTimeMillis();
    sendStatusUpdates();
  }

  private void resumePick() {
    currentPickDeadline = System.currentTimeMillis() + pausedPickTime;
    paused = false;
    startPickTimer(pausedPickTime);
    pausedPickTime = 0;
    sendStatusUpdates();
  }

  private void startPickTimer(long timeMs) {
    cancelPickTimer();
    currentPickTimer = pickTimer.schedule(new Runnable() {
      public void run() {
        newPick();
      }
    }, timeMs, TimeUnit.MILLISECONDS);
  }

  private void cancelPickTimer() {
    if (currentPickTimer != null) {
      currentPickTimer.cancel(true);
    }
  }

  private void sendStatusUpdates() {
    AutoBean<DraftStatus> statusBean = beanFactory.createDraftStatus();
    DraftStatus status = statusBean.as();
    status.setCurrentPickDeadline(currentPickDeadline);
    status.setPaused(paused);
    for (DraftTowerWebSocket socket : openSockets) {
      socket.sendMessage(statusBean);
    }
  }

  private Set<DraftTowerWebSocket> openSockets = Sets.newHashSet();

  public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
    return new DraftTowerWebSocket();
  }
}