package com.mayhew3.drafttower.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.DraftCommand;
import com.mayhew3.drafttower.shared.DraftStatus;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class responsible for tracking draft state and handling commands from clients.
 */
@Singleton
public class DraftController implements DraftTowerWebSocketServlet.DraftCommandListener {

  private static final long PICK_LENGTH_MS = 90 * 1000;

  private final Lock lock = new ReentrantLock();

  private final DraftTowerWebSocketServlet socketServlet;
  private final BeanFactory beanFactory;

  private long currentPickDeadline;
  private boolean paused;
  private long pausedPickTime;

  private ScheduledThreadPoolExecutor pickTimer = new ScheduledThreadPoolExecutor(1);
  private ScheduledFuture currentPickTimer;

  @Inject
  public DraftController(DraftTowerWebSocketServlet socketServlet, BeanFactory beanFactory) {
    this.socketServlet = socketServlet;
    this.beanFactory = beanFactory;
    socketServlet.addListener(this);
  }

  public void onClientConnected() {
    if (currentPickDeadline > 0) {
      socketServlet.sendMessage(createStatusMessage());
    }
  }

  public void onDraftCommand(String cmd) {
    lock.lock();
    try {
      DraftCommand draftCommand = AutoBeanCodex.decode(beanFactory, DraftCommand.class, cmd).as();
      switch (draftCommand.getCommandType()) {
        case START_DRAFT:
          newPick();
          break;
        case DO_PICK:
          newPick();
          break;
        case PAUSE:
          pausePick();
          break;
        case RESUME:
          resumePick();
          break;
      }
    } finally {
      lock.unlock();
    }
  }

  public void onClientDisconnected() {
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
        lock.lock();
        try {
          currentPickTimer = null;
          newPick();
        } finally {
          lock.unlock();
        }
      }
    }, timeMs, TimeUnit.MILLISECONDS);
  }

  private void cancelPickTimer() {
    if (currentPickTimer != null) {
      currentPickTimer.cancel(true);
    }
  }

  private void sendStatusUpdates() {
    socketServlet.sendMessage(createStatusMessage());
  }

  private String createStatusMessage() {
    AutoBean<DraftStatus> statusBean = beanFactory.createDraftStatus();
    DraftStatus status = statusBean.as();
    status.setCurrentPickDeadline(currentPickDeadline);
    status.setPaused(paused);
    return AutoBeanCodex.encode(statusBean).getPayload();
  }
}