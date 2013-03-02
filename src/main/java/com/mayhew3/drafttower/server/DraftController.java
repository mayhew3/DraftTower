package com.mayhew3.drafttower.server;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.mayhew3.drafttower.server.ServerModule.Keepers;
import com.mayhew3.drafttower.server.ServerModule.Queues;
import com.mayhew3.drafttower.server.ServerModule.TeamTokens;
import com.mayhew3.drafttower.shared.*;
import com.mayhew3.drafttower.shared.SharedModule.Commissioner;
import com.mayhew3.drafttower.shared.SharedModule.NumTeams;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

/**
 * Class responsible for tracking draft state and handling commands from clients.
 */
@Singleton
public class DraftController implements DraftTowerWebSocketServlet.DraftCommandListener {

  private static final Logger logger = Logger.getLogger(DraftController.class.getName());

  private static final long PICK_LENGTH_MS = 15 * 1000;
  private static final long ROBOT_PICK_LENGTH_MS = 10 * 1000;

  private final Lock lock = new ReentrantLock();

  private final DraftTowerWebSocketServlet socketServlet;
  private final BeanFactory beanFactory;
  private final PlayerDataSource playerDataSource;

  private final Map<String, Integer> teamTokens;
  private final ListMultimap<Integer, Integer> keepers;
  private final ListMultimap<Integer, QueueEntry> queues;

  private final int commissionerTeam;
  private final int numTeams;

  private DraftStatus status;
  private long pausedPickTime;

  private ScheduledThreadPoolExecutor pickTimer = new ScheduledThreadPoolExecutor(1);
  private ScheduledFuture currentPickTimer;

  @Inject
  public DraftController(DraftTowerWebSocketServlet socketServlet,
      BeanFactory beanFactory,
      PlayerDataSource playerDataSource,
      @TeamTokens Map<String, Integer> teamTokens,
      @Keepers ListMultimap<Integer, Integer> keepers,
      @Queues ListMultimap<Integer, QueueEntry> queues,
      @Commissioner int commissionerTeam,
      @NumTeams int numTeams) {
    this.socketServlet = socketServlet;
    this.beanFactory = beanFactory;
    this.playerDataSource = playerDataSource;
    this.teamTokens = teamTokens;
    this.keepers = keepers;
    this.queues = queues;
    this.commissionerTeam = commissionerTeam;
    this.numTeams = numTeams;
    this.status = beanFactory.createDraftStatus().as();
    status.setConnectedTeams(Sets.<Integer>newHashSet());
    status.setRobotTeams(Sets.<Integer>newHashSet());
    status.setPicks(Lists.<DraftPick>newArrayList());
    status.setCurrentTeam(1);
    socketServlet.addListener(this);
  }

  @Override
  public void onClientConnected() {
    socketServlet.sendMessage(getEncodedStatus());
  }

  @Override
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
          break;
        case START_DRAFT:
          newPick();
          break;
        case DO_PICK:
          if (team == status.getCurrentTeam()) {
            doPick(team, cmd.getPlayerId(), false);
          }
          break;
        case PAUSE:
          pausePick();
          break;
        case RESUME:
          resumePick();
          break;
        case BACK_OUT:
          backOutLastPick();
          break;
        case FORCE_PICK:
          autoPick();
          break;
        case WAKE_UP:
          status.getRobotTeams().remove(team);
          break;
      }
      sendStatusUpdates();
    } finally {
      lock.unlock();
    }
  }

  private void doPick(Integer team, long playerId, boolean auto) {
    if (playerId == Player.BEST_DRAFT_PICK) {
      try {
        playerId = playerDataSource.getBestPlayerId();
      } catch (SQLException e) {
        logger.log(SEVERE, "SQL error looking up the best draft pick", e);
        return;
      }
    }

    logger.info("Team " + team
        + (auto ? " auto-picked" : " picked")
        + " player " + playerId);

    DraftPick pick = beanFactory.createDraftPick().as();
    pick.setTeam(team);
    pick.setPlayerId(playerId);
    try {
      playerDataSource.populateDraftPick(pick);
    } catch (SQLException e) {
      logger.log(SEVERE, "SQL error looking up player name/eligibility for ID " + playerId, e);
    }
    status.getPicks().add(pick);

    try {
      playerDataSource.postDraftPick(pick, status);
    } catch (SQLException e) {
      logger.log(SEVERE, "SQL error posting draft pick for player ID " + playerId, e);
    }

    Collection<Entry<Integer,QueueEntry>> queueEntries = queues.entries();
    synchronized (queues) {
      // TODO(m3): persist to database
      for (Iterator<Entry<Integer, QueueEntry>> iterator = queueEntries.iterator(); iterator.hasNext();) {
        Entry<Integer, QueueEntry> entry = iterator.next();
        if (entry.getValue().getPlayerId() == playerId) {
          iterator.remove();
        }
      }
    }

    advanceTeam();
    newPick();
  }

  @Override
  public void onClientDisconnected(String teamToken) {
    status.getConnectedTeams().remove(teamTokens.get(teamToken));
    socketServlet.sendMessage(getEncodedStatus());
  }

  private void newPick() {
    cancelPickTimer();
    long pickLengthMs = status.getRobotTeams().contains(status.getCurrentTeam())
        ? ROBOT_PICK_LENGTH_MS
        : PICK_LENGTH_MS;
    status.setCurrentPickDeadline(System.currentTimeMillis() + pickLengthMs);
    status.setPaused(false);

    int round = status.getPicks().size() / numTeams;
    List<Integer> currentTeamKeepers = keepers.get(status.getCurrentTeam());
    if (currentTeamKeepers != null && round < currentTeamKeepers.size()) {
      doPick(status.getCurrentTeam(), currentTeamKeepers.get(round), true);
    } else {
      startPickTimer(pickLengthMs);
    }
  }

  private void advanceTeam() {
    int currentTeam = status.getCurrentTeam() + 1;
    if (currentTeam > numTeams) {
      currentTeam -= numTeams;
    }
    status.setCurrentTeam(currentTeam);
  }

  private void goBackOneTeam() {
    int currentTeam = status.getCurrentTeam() - 1;
    if (currentTeam < 1) {
      currentTeam += numTeams;
    }
    status.setCurrentTeam(currentTeam);
  }

  private void pausePick() {
    cancelPickTimer();
    status.setPaused(true);
    pausedPickTime = status.getCurrentPickDeadline() - System.currentTimeMillis();
  }

  private void resumePick() {
    status.setCurrentPickDeadline(System.currentTimeMillis() + pausedPickTime);
    status.setPaused(false);
    startPickTimer(pausedPickTime);
    pausedPickTime = 0;
  }

  private void backOutLastPick() {
    logger.info("Backed out pick " + status.getPicks().size());
    status.getPicks().remove(status.getPicks().size() - 1);
    goBackOneTeam();
    newPick();
  }

  private void autoPick() {
    if (queues.containsKey(status.getCurrentTeam())) {
      List<QueueEntry> queue = queues.get(status.getCurrentTeam());
      synchronized (queues) {
        if (!queue.isEmpty()) {
          doPick(status.getCurrentTeam(), queue.remove(0).getPlayerId(), true);
          return;
        }
      }
    }

    doPick(status.getCurrentTeam(), Player.BEST_DRAFT_PICK, true);
  }

  private void startPickTimer(long timeMs) {
    cancelPickTimer();
    currentPickTimer = pickTimer.schedule(new Runnable() {
      @Override
      public void run() {
        lock.lock();
        try {
          currentPickTimer = null;
          status.getRobotTeams().add(status.getCurrentTeam());
          autoPick();
          sendStatusUpdates();
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