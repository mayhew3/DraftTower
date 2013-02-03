package com.mayhew3.drafttower.server;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.mayhew3.drafttower.server.ServerModule.TeamTokens;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.DraftCommand;
import com.mayhew3.drafttower.shared.DraftStatus;
import com.mayhew3.drafttower.shared.SharedModule.Commissioner;

import java.util.Map;
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
  private static final int NUM_TEAMS = 12;

  private final Lock lock = new ReentrantLock();

  private final DraftTowerWebSocketServlet socketServlet;

  private final Map<String, Integer> teamTokens;

  private final int commissionerTeam;

  private DraftStatus status;
  private long pausedPickTime;

  private ScheduledThreadPoolExecutor pickTimer = new ScheduledThreadPoolExecutor(1);
  private ScheduledFuture currentPickTimer;

  @Inject
  public DraftController(DraftTowerWebSocketServlet socketServlet,
      BeanFactory beanFactory,
      @TeamTokens Map<String, Integer> teamTokens,
      @Commissioner int commissionerTeam) {
    this.socketServlet = socketServlet;
    this.teamTokens = teamTokens;
    this.commissionerTeam = commissionerTeam;
    this.status = beanFactory.createDraftStatus().as();
    status.setConnectedTeams(Sets.<Integer>newHashSet());
    socketServlet.addListener(this);
  }

  public void onClientConnected() {
    if (status.getCurrentPickDeadline() > 0) {
      socketServlet.sendMessage(getEncodedStatus());
    }
  }

  public void onDraftCommand(DraftCommand cmd) throws TerminateSocketException {
    lock.lock();
    Integer team = teamTokens.get(cmd.getTeamToken());
    if (cmd.getCommandType().isCommissionerOnly()
        && team != commissionerTeam) {
      return;
    }
    try {
      switch (cmd.getCommandType()) {
        case IDENTIFY:
          if (status.getConnectedTeams().contains(team)) {
            throw new TerminateSocketException("Team already connected!");
          }
          status.getConnectedTeams().add(team);
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

  public void onClientDisconnected(String teamToken) {
    status.getConnectedTeams().remove(teamTokens.get(teamToken));
  }

  private void newPick() {
    cancelPickTimer();
    status.setCurrentPickDeadline(System.currentTimeMillis() + PICK_LENGTH_MS);
    status.setPaused(false);
    startPickTimer(PICK_LENGTH_MS);
    sendStatusUpdates();
  }

  private void pausePick() {
    cancelPickTimer();
    status.setPaused(true);
    pausedPickTime = status.getCurrentPickDeadline() - System.currentTimeMillis();
    sendStatusUpdates();
  }

  private void resumePick() {
    status.setCurrentPickDeadline(System.currentTimeMillis() + pausedPickTime);
    status.setPaused(false);
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
    socketServlet.sendMessage(getEncodedStatus());
  }

  private String getEncodedStatus() {
    return AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(status)).getPayload();
  }
}