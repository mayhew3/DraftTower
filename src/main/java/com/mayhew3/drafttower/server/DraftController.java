package com.mayhew3.drafttower.server;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.mayhew3.drafttower.server.BindingAnnotations.AutoPickWizards;
import com.mayhew3.drafttower.server.BindingAnnotations.Keepers;
import com.mayhew3.drafttower.server.BindingAnnotations.Queues;
import com.mayhew3.drafttower.server.BindingAnnotations.TeamTokens;
import com.mayhew3.drafttower.shared.*;
import com.mayhew3.drafttower.shared.SharedModule.NumTeams;

import java.sql.SQLException;
import java.util.*;
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

  private static final long PICK_LENGTH_MS = 75 * 1000;
  private static final long ROBOT_PICK_LENGTH_MS = 7 * 1000;

  private final Lock lock = new ReentrantLock();

  private final DraftTowerWebSocketServlet socketServlet;
  private final BeanFactory beanFactory;
  private final PlayerDataSource playerDataSource;
  private final TeamDataSource teamDataSource;

  private final Map<String, Integer> teamTokens;
  private final ListMultimap<Integer, Integer> keepers;
  private final ListMultimap<Integer, QueueEntry> queues;
  private final Map<Integer, PlayerDataSet> autoPickWizardTables;

  private final int numTeams;

  private final DraftStatus status;
  private long pausedPickTime;

  private final ScheduledThreadPoolExecutor pickTimer = new ScheduledThreadPoolExecutor(1);
  private ScheduledFuture<?> currentPickTimer;

  @Inject
  public DraftController(DraftTowerWebSocketServlet socketServlet,
      BeanFactory beanFactory,
      PlayerDataSource playerDataSource,
      TeamDataSource teamDataSource,
      DraftStatus status,
      @TeamTokens Map<String, Integer> teamTokens,
      @Keepers ListMultimap<Integer, Integer> keepers,
      @Queues ListMultimap<Integer, QueueEntry> queues,
      @AutoPickWizards Map<Integer, PlayerDataSet> autoPickWizardTables,
      @NumTeams int numTeams) throws SQLException {
    this.socketServlet = socketServlet;
    this.beanFactory = beanFactory;
    this.playerDataSource = playerDataSource;
    this.teamDataSource = teamDataSource;
    this.teamTokens = teamTokens;
    this.keepers = keepers;
    this.queues = queues;
    this.autoPickWizardTables = autoPickWizardTables;
    this.numTeams = numTeams;
    this.status = status;
    status.setConnectedTeams(new HashSet<Integer>());
    status.setRobotTeams(new HashSet<Integer>());
    status.setPicks(new ArrayList<DraftPick>());
    playerDataSource.populateDraftStatus(status);
    int round = status.getPicks().size() / numTeams;
    status.setNextPickKeeperTeams(getNextPickKeeperTeams(round));
    int currentTeam = status.getPicks().isEmpty()
        ? 1
        : (status.getPicks().get(status.getPicks().size() - 1).getTeam() + 1);
    if (currentTeam > numTeams) {
      currentTeam -= numTeams;
    }
    status.setCurrentTeam(currentTeam);
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
    if (cmd.getCommandType().isCommissionerOnly()) {
      try {
        if (!teamDataSource.isCommissionerTeam(team)) {
          return;
        }
      } catch (SQLException e) {
        logger.log(SEVERE, "Couldn't look up team for commissioner-only command.", e);
        return;
      }
    }
    try {
      switch (cmd.getCommandType()) {
        case IDENTIFY:
          if (status.getConnectedTeams().contains(team)) {
            throw new TerminateSocketException("Team already connected!");
          }
          status.getConnectedTeams().add(team);
          status.getRobotTeams().remove(team);
          break;
        case START_DRAFT:
          newPick();
          break;
        case DO_PICK:
          if (!status.isOver() && team == status.getCurrentTeam()) {
            doPick(team, cmd.getPlayerId(), false, false);
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
          if (!status.isOver()) {
            if (cmd.getPlayerId() == null) {
              autoPick();
            } else {
              doPick(status.getCurrentTeam(), cmd.getPlayerId(), true, false);
            }
          }
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

  private void doPick(final Integer team, long playerId, boolean auto, boolean keeper) {
    if (playerId == Player.BEST_DRAFT_PICK) {
      try {
        playerId = playerDataSource.getBestPlayerId(autoPickWizardTables.get(team),
            team,
            RosterUtil.getOpenPositions(
                Lists.newArrayList(Iterables.filter(status.getPicks(),
                    new Predicate<DraftPick>() {
                      @Override
                      public boolean apply(DraftPick input) {
                        return input.getTeam() == team;
                      }
                    }))));
      } catch (SQLException e) {
        logger.log(SEVERE, "SQL error looking up the best draft pick", e);
        return;
      }
    }

    for (DraftPick pick : status.getPicks()) {
      if (pick.getPlayerId() == playerId) {
        logger.log(SEVERE, "Player " + playerId + " was already picked");
        return;
      }
    }

    logger.info("Team " + team
        + (auto ? " auto-picked" : " picked")
        + " player " + playerId);

    DraftPick pick = beanFactory.createDraftPick().as();
    pick.setTeam(team);
    pick.setPlayerId(playerId);
    pick.setKeeper(keeper);
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

    status.setOver(round >= 22);
    status.setNextPickKeeperTeams(getNextPickKeeperTeams(round));

    if (isCurrentPickKeeper()) {
      List<Integer> currentTeamKeepers = keepers.get(status.getCurrentTeam());
      doPick(status.getCurrentTeam(), currentTeamKeepers.get(round), true, true);
    } else if (!status.isOver()) {
      startPickTimer(pickLengthMs);
    }
  }

  private boolean isCurrentPickKeeper() {
    List<Integer> currentTeamKeepers = keepers.get(status.getCurrentTeam());
    int round = status.getPicks().size() / numTeams;
    return currentTeamKeepers != null && round < currentTeamKeepers.size();
  }

  private Set<Integer> getNextPickKeeperTeams(int round) {
    Set<Integer> nextPickKeeperTeams = new HashSet<>();
    if (round <= 3) {
      for (int i = 1; i <= numTeams; i++) {
        if (round + (i < status.getCurrentTeam() ? 1 : 0) < keepers.get(i).size()) {
          nextPickKeeperTeams.add(i);
        }
      }
    }
    return nextPickKeeperTeams;
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

  @VisibleForTesting
  void backOutLastPick() {
    if (status.getPicks().isEmpty()) {
      logger.warning("Attempt to back out pick when there are no picks!");
    } else {
      boolean wasPaused = status.isPaused();
      do {
        int pickToRemove = status.getPicks().size();
        logger.info("Backed out pick " + pickToRemove);
        removePick(pickToRemove);
        goBackOneTeam();
      } while (isCurrentPickKeeper());
      newPick();
      if (wasPaused) {
        pausePick();
      }
    }
  }

  private void removePick(int pickToRemove) {
    status.getPicks().remove(pickToRemove - 1);
    try {
      playerDataSource.backOutLastDraftPick(pickToRemove);
    } catch (SQLException e) {
      logger.log(SEVERE, "SQL error backing out last draft pick.", e);
    }
  }

  /** Returns true if the team should go into robot mode. */
  private boolean autoPick() {
    if (queues.containsKey(status.getCurrentTeam())) {
      List<QueueEntry> queue = queues.get(status.getCurrentTeam());
      synchronized (queues) {
        if (!queue.isEmpty()) {
          doPick(status.getCurrentTeam(), queue.remove(0).getPlayerId(), true, false);
          return false;
        }
      }
    }

    doPick(status.getCurrentTeam(), Player.BEST_DRAFT_PICK, true, false);
    return true;
  }

  private void startPickTimer(long timeMs) {
    cancelPickTimer();
    currentPickTimer = pickTimer.schedule(new Runnable() {
      @Override
      public void run() {
        lock.lock();
        try {
          timerExpired();
        } finally {
          lock.unlock();
        }
      }
    }, timeMs, TimeUnit.MILLISECONDS);
  }

  @VisibleForTesting void timerExpired() {
    currentPickTimer = null;
    int currentTeam = status.getCurrentTeam();
    if (autoPick()) {
      status.getRobotTeams().add(currentTeam);
    }
    sendStatusUpdates();
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
    status.setSerialId(status.getSerialId() + 1);
    return AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(status)).getPayload();
  }
}