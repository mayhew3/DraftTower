package com.mayhew3.drafttower.server;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.mayhew3.drafttower.server.BindingAnnotations.Keepers;
import com.mayhew3.drafttower.server.BindingAnnotations.Queues;
import com.mayhew3.drafttower.server.BindingAnnotations.TeamTokens;
import com.mayhew3.drafttower.shared.*;
import com.mayhew3.drafttower.shared.SharedModule.NumTeams;

import java.util.*;
import java.util.Map.Entry;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

/**
 * Implementation of {@link DraftController}.
 */
@Singleton
public class DraftControllerImpl implements DraftController {

  private static final Logger logger = Logger.getLogger(DraftControllerImpl.class.getName());

  private static final long PICK_LENGTH_MS = 75 * 1000;
  private static final long ROBOT_PICK_LENGTH_MS = 7 * 1000;

  private final Lock lock;
  private final DraftTowerWebSocket socketServlet;
  private final BeanFactory beanFactory;
  private final PlayerDataProvider playerDataProvider;
  private final PickProbabilityPredictor pickProbabilityPredictor;
  private final TeamDataSource teamDataSource;
  private final CurrentTimeProvider currentTimeProvider;
  private final DraftTimer draftTimer;
  private final RosterUtil rosterUtil;

  private final Map<String, TeamDraftOrder> teamTokens;
  private final ListMultimap<TeamDraftOrder, Integer> keepers;
  private final ListMultimap<TeamDraftOrder, QueueEntry> queues;
  private final int numTeams;

  private final DraftStatus status;
  private long pausedPickTime;

  @Inject
  public DraftControllerImpl(DraftTowerWebSocket socketServlet,
      BeanFactory beanFactory,
      PlayerDataProvider playerDataProvider,
      PickProbabilityPredictor pickProbabilityPredictor,
      TeamDataSource teamDataSource,
      CurrentTimeProvider currentTimeProvider,
      DraftTimer draftTimer,
      DraftStatus status,
      Lock lock,
      RosterUtil rosterUtil,
      @TeamTokens Map<String, TeamDraftOrder> teamTokens,
      @Keepers ListMultimap<TeamDraftOrder, Integer> keepers,
      @Queues ListMultimap<TeamDraftOrder, QueueEntry> queues,
      @NumTeams int numTeams) throws DataSourceException {
    this.socketServlet = socketServlet;
    this.beanFactory = beanFactory;
    this.playerDataProvider = playerDataProvider;
    this.pickProbabilityPredictor = pickProbabilityPredictor;
    this.teamDataSource = teamDataSource;
    this.currentTimeProvider = currentTimeProvider;
    this.draftTimer = draftTimer;
    this.rosterUtil = rosterUtil;
    this.teamTokens = teamTokens;
    this.keepers = keepers;
    this.queues = queues;
    this.numTeams = numTeams;
    this.status = status;
    this.lock = lock;

    status.setConnectedTeams(new HashSet<Integer>());
    status.setRobotTeams(new HashSet<Integer>());
    status.setPicks(new ArrayList<DraftPick>());
    playerDataProvider.populateDraftStatus(status);
    int round = status.getPicks().size() / numTeams;
    int currentTeam = status.getPicks().isEmpty()
        ? 1
        : (status.getPicks().get(status.getPicks().size() - 1).getTeam() + 1);
    if (currentTeam > numTeams) {
      currentTeam -= numTeams;
    }
    status.setCurrentTeam(currentTeam);
    status.setNextPickKeeperTeams(getNextPickKeeperTeams(round));

    draftTimer.addListener(this);
    socketServlet.addListener(this);
  }

  @Override
  public void onClientConnected() {
    socketServlet.sendMessage(getStatusEncoder());
  }

  @Override
  public void onDraftCommand(DraftCommand cmd) throws TerminateSocketException {
    try (Lock ignored = lock.lock()) {
      TeamDraftOrder teamDraftOrder = teamTokens.get(cmd.getTeamToken());
      if (teamDraftOrder == null) {
        throw new TerminateSocketException(SocketTerminationReason.BAD_TEAM_TOKEN);
      }
      if (cmd.getCommandType().isCommissionerOnly()) {
        try {
          if (!teamDataSource.isCommissionerTeam(teamDraftOrder)) {
            return;
          }
        } catch (DataSourceException e) {
          logger.log(SEVERE, "Couldn't look up team for commissioner-only command.", e);
          return;
        }
      }
      switch (cmd.getCommandType()) {
        case IDENTIFY:
          if (status.getConnectedTeams().contains(teamDraftOrder.get())) {
            throw new TerminateSocketException(SocketTerminationReason.TEAM_ALREADY_CONNECTED);
          }
          status.getConnectedTeams().add(teamDraftOrder.get());
          status.getRobotTeams().remove(teamDraftOrder.get());
          break;
        case START_DRAFT:
          newPick();
          break;
        case DO_PICK:
          if (!status.isOver() && teamDraftOrder.get() == status.getCurrentTeam()) {
            doPick(teamDraftOrder, cmd.getPlayerId(), false, false);
          } else {
            return;
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
              doPick(new TeamDraftOrder(status.getCurrentTeam()), cmd.getPlayerId(), true, false);
            }
          } else {
            return;
          }
          break;
        case WAKE_UP:
          status.getRobotTeams().remove(teamDraftOrder.get());
          break;
        case RESET_DRAFT:
          try {
            resetDraft();
          } catch (DataSourceException e) {
            logger.log(SEVERE, "Failed to reset draft.", e);
            return;
          }
      }
      sendStatusUpdates();
    }
  }

  @VisibleForTesting
  void doPick(final TeamDraftOrder teamDraftOrder, long playerId, boolean auto, boolean keeper) {
    try (Lock ignored = lock.lock()) {
      if (playerId == Player.BEST_DRAFT_PICK) {
        try {
          playerId = playerDataProvider.getBestPlayerId(
              teamDraftOrder,
              status.getPicks(),
              rosterUtil.getOpenPositions(
                  Lists.newArrayList(Iterables.filter(status.getPicks(),
                      new Predicate<DraftPick>() {
                        @Override
                        public boolean apply(DraftPick draftPick) {
                          return draftPick.getTeam() == teamDraftOrder.get();
                        }
                      }))),
              pickProbabilityPredictor.getTeamPredictions(teamDraftOrder));
        } catch (DataSourceException e) {
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

      logger.info("Team " + teamDraftOrder
          + (auto ? " auto-picked" : " picked")
          + " player " + playerId);

      DraftPick pick = beanFactory.createDraftPick().as();
      pick.setTeam(teamDraftOrder.get());
      pick.setPlayerId(playerId);
      pick.setKeeper(keeper);
      try {
        playerDataProvider.populateDraftPick(pick);
      } catch (DataSourceException e) {
        logger.log(SEVERE, "SQL error looking up player name/eligibility for ID " + playerId, e);
        return;
      }
      status.getPicks().add(pick);

      try {
        playerDataProvider.postDraftPick(pick, status);
      } catch (DataSourceException e) {
        logger.log(SEVERE, "SQL error posting draft pick for player ID " + playerId, e);
      }

      Collection<Entry<TeamDraftOrder, QueueEntry>> queueEntries = queues.entries();
      synchronized (queues) {
        for (Iterator<Entry<TeamDraftOrder, QueueEntry>> iterator = queueEntries.iterator(); iterator.hasNext();) {
          Entry<TeamDraftOrder, QueueEntry> entry = iterator.next();
          if (entry.getValue().getPlayerId() == playerId) {
            iterator.remove();
          }
        }
      }

      advanceTeam();
      newPick();
    }
  }

  @Override
  public void onClientDisconnected(String teamToken) {
    try (Lock ignored = lock.lock()) {
      if (teamTokens.containsKey(teamToken)) {
        status.getConnectedTeams().remove(teamTokens.get(teamToken).get());
      }
      socketServlet.sendMessage(getStatusEncoder());
    }
  }

  private void newPick() {
    try (Lock ignored = lock.lock()) {
      draftTimer.cancel();
      long pickLengthMs = status.getRobotTeams().contains(status.getCurrentTeam())
          ? ROBOT_PICK_LENGTH_MS
          : PICK_LENGTH_MS;
      status.setCurrentPickDeadline(currentTimeProvider.getCurrentTimeMillis() + pickLengthMs);
      status.setPaused(false);

      int round = status.getPicks().size() / numTeams;

      status.setOver(round >= 22);
      status.setNextPickKeeperTeams(getNextPickKeeperTeams(round));

      if (isCurrentPickKeeper()) {
        TeamDraftOrder currentTeam = new TeamDraftOrder(status.getCurrentTeam());
        List<Integer> currentTeamKeepers = keepers.get(currentTeam);
        doPick(currentTeam, currentTeamKeepers.get(round), true, true);
      } else if (!status.isOver()) {
        startPickTimer(pickLengthMs);
      }
    }
  }

  private boolean isCurrentPickKeeper() {
    try (Lock ignored = lock.lock()) {
      List<Integer> currentTeamKeepers = keepers.get(new TeamDraftOrder(status.getCurrentTeam()));
      int round = status.getPicks().size() / numTeams;
      return round < currentTeamKeepers.size();
    }
  }

  private boolean areAllPicksKeepers() {
    try (Lock ignored = lock.lock()) {
      if (status.getPicks().size() < numTeams) {
        for (int i = status.getPicks().size(); i > 0; i--) {
          if (keepers.get(new TeamDraftOrder(i)).isEmpty()) {
            return false;
          }
        }
        return true;
      }
    }
    return false;
  }

  private Set<Integer> getNextPickKeeperTeams(int round) {
    try (Lock ignored = lock.lock()) {
      Set<Integer> nextPickKeeperTeams = new HashSet<>();
      if (round < 3) {
        for (int i = 1; i <= numTeams; i++) {
          if (round + (i < status.getCurrentTeam() ? 1 : 0) < keepers.get(new TeamDraftOrder(i)).size()) {
            nextPickKeeperTeams.add(i);
          }
        }
      }
      return nextPickKeeperTeams;
    }
  }

  private void advanceTeam() {
    try (Lock ignored = lock.lock()) {
      int currentTeam = status.getCurrentTeam() + 1;
      if (currentTeam > numTeams) {
        currentTeam -= numTeams;
      }
      status.setCurrentTeam(currentTeam);
    }
  }

  private void goBackOneTeam() {
    try (Lock ignored = lock.lock()) {
      int currentTeam = status.getCurrentTeam() - 1;
      if (currentTeam < 1) {
        currentTeam += numTeams;
      }
      status.setCurrentTeam(currentTeam);
    }
  }

  private void pausePick() {
    try (Lock ignored = lock.lock()) {
      draftTimer.cancel();
      status.setPaused(true);
      pausedPickTime = status.getCurrentPickDeadline() - currentTimeProvider.getCurrentTimeMillis();
    }
  }

  private void resumePick() {
    try (Lock ignored = lock.lock()) {
      status.setCurrentPickDeadline(currentTimeProvider.getCurrentTimeMillis() + pausedPickTime);
      status.setPaused(false);
      startPickTimer(pausedPickTime);
      pausedPickTime = 0;
    }
  }

  @VisibleForTesting
  void backOutLastPick() {
    try (Lock ignored = lock.lock()) {
      if (status.getPicks().isEmpty() || areAllPicksKeepers()) {
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
  }

  private void resetDraft() throws DataSourceException {
    playerDataProvider.resetDraft();
    status.getPicks().clear();
    status.setCurrentTeam(1);
    status.setCurrentPickDeadline(0);
    status.setPaused(true);
    status.setNextPickKeeperTeams(getNextPickKeeperTeams(0));
    status.setOver(false);
  }

  private void removePick(int pickToRemove) {
    try (Lock ignored = lock.lock()) {
      status.getPicks().remove(pickToRemove - 1);
      try {
        playerDataProvider.backOutLastDraftPick(pickToRemove);
      } catch (DataSourceException e) {
        logger.log(SEVERE, "SQL error backing out last draft pick.", e);
      }
    }
  }

  /** Returns true if the team should go into robot mode. */
  private boolean autoPick() {
    try (Lock ignored = lock.lock()) {
      TeamDraftOrder currentTeam = new TeamDraftOrder(status.getCurrentTeam());
      List<QueueEntry> queue = queues.get(currentTeam);
      synchronized (queues) {
        if (!queue.isEmpty()) {
          doPick(currentTeam, queue.remove(0).getPlayerId(), true, false);
          return false;
        }
      }

      doPick(currentTeam, Player.BEST_DRAFT_PICK, true, false);
    }
    return true;
  }

  private void startPickTimer(long timeMs) {
    draftTimer.start(timeMs);
  }

  @Override
  public void timerExpired() {
    try (Lock ignored = lock.lock()) {
      int currentTeam = status.getCurrentTeam();
      if (autoPick()) {
        status.getRobotTeams().add(currentTeam);
      }
      sendStatusUpdates();
    }
  }

  @VisibleForTesting
  public void sendStatusUpdates() {
    if (pickProbabilityPredictor != null) {
      pickProbabilityPredictor.onDraftStatusChanged(status);
    }
    socketServlet.sendMessage(getStatusEncoder());
  }

  private Function<String, String> getStatusEncoder() {
    try (Lock ignored = lock.lock()) {
      final Map<String, String> statusPerTeam = new HashMap<>();
      status.setSerialId(status.getSerialId() + 1);
      ClientDraftStatus clientStatus = beanFactory.createClientDraftStatus().as();
      clientStatus.setDraftStatus(status);
      clientStatus.setPickPredictions(Collections.<Long, Float>emptyMap());
      statusPerTeam.put(null, encodeStatus(clientStatus));
      for (Entry<String, TeamDraftOrder> teamToken : teamTokens.entrySet()) {
        clientStatus.setPickPredictions(pickProbabilityPredictor.getTeamPredictions(teamToken.getValue()));
        statusPerTeam.put(teamToken.getKey(), encodeStatus(clientStatus));
      }
      return Functions.forMap(statusPerTeam);
    }
  }

  private String encodeStatus(ClientDraftStatus status) {
    return AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(status)).getPayload();
  }
}