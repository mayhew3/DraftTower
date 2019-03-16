package com.mayhew3.drafttower.server;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Ordering;
import com.mayhew3.drafttower.server.BindingAnnotations.AutoPickWizards;
import com.mayhew3.drafttower.server.BindingAnnotations.MaxClosers;
import com.mayhew3.drafttower.server.BindingAnnotations.MinClosers;
import com.mayhew3.drafttower.server.BindingAnnotations.TeamTokens;
import com.mayhew3.drafttower.shared.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static com.mayhew3.drafttower.shared.Position.P;

/**
 * Handles caching and processing of player-related data.
 */
@Singleton
public class PlayerDataProvider {

  private static final Logger logger = Logger.getLogger(PlayerDataProvider.class.getName());

  private static final Predicate<DraftPick> PICK_IS_CLOSER = new Predicate<DraftPick>() {
    @Override
    public boolean apply(DraftPick pick) {
      return pick.isCloser();
    }
  };

  private final PlayerDataSource dataSource;
  private final BeanFactory beanFactory;
  private final TeamDataSource teamDataSource;
  private final Map<TeamDraftOrder, PlayerDataSet> autoPickWizardTables;
  private final Map<TeamDraftOrder, Integer> minClosers;
  private final Map<TeamDraftOrder, Integer> maxClosers;
  private final Map<String, TeamDraftOrder> teamTokens;

  private final Map<String, List<Player>> cache = new ConcurrentHashMap<>();

  @Inject
  public PlayerDataProvider(PlayerDataSource dataSource,
      BeanFactory beanFactory,
      TeamDataSource teamDataSource,
      @AutoPickWizards Map<TeamDraftOrder, PlayerDataSet> autoPickWizardTables,
      @MinClosers Map<TeamDraftOrder, Integer> minClosers,
      @MaxClosers Map<TeamDraftOrder, Integer> maxClosers,
      @TeamTokens Map<String, TeamDraftOrder> teamTokens) {
    this.dataSource = dataSource;
    this.beanFactory = beanFactory;
    this.teamDataSource = teamDataSource;
    this.autoPickWizardTables = autoPickWizardTables;
    this.minClosers = minClosers;
    this.maxClosers = maxClosers;
    this.teamTokens = teamTokens;

    try {
      warmCaches();
    } catch (DataSourceException e) {
      throw new RuntimeException(e);
    }
  }

  protected void warmCaches() throws DataSourceException {
    TableSpec tableSpec = beanFactory.createTableSpec().as();
    tableSpec.setSortCol(PlayerColumn.MYRANK);
    tableSpec.setAscending(true);
    for (int i = 1; i <= 10; i++) {
      logger.info("Warming caches: " + i + "/10");
      TeamId team = new TeamId(i);
      // todo: auto-detect presence of other data sources
      // Disable other data sets for 2015 draft.
//      for (PlayerDataSet playerDataSet : PlayerDataSet.values()) {
//        tableSpec.setPlayerDataSet(playerDataSet);
//        getPlayers(team, tableSpec);
//      }
      tableSpec.setPlayerDataSet(PlayerDataSet.CBSSPORTS);
      getPlayers(team, tableSpec);
    }
  }

  public UnclaimedPlayerListResponse lookupUnclaimedPlayers(UnclaimedPlayerListRequest request) throws DataSourceException {
    Stopwatch stopwatch = Stopwatch.createStarted();
    UnclaimedPlayerListResponse response = beanFactory.createUnclaimedPlayerListResponse().as();

    TeamId teamId = request.getTeamToken() == null
        ? null
        : teamDataSource.getTeamIdByDraftOrder(teamTokens.get(request.getTeamToken()));
    TableSpec tableSpec = request.getTableSpec();
    List<Player> players = getPlayers(teamId, tableSpec);

    response.setPlayers(players);

    stopwatch.stop();
    logger.info("Player table request took " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
    return response;
  }

  public List<Player> getPlayers(TeamId teamId, TableSpec tableSpec) throws DataSourceException {
    List<Player> players;
    synchronized (cache) {
      PlayerDataSet playerDataSet = tableSpec.getPlayerDataSet();
      String cacheKey = getKey(teamId, playerDataSet);
      if (cache.containsKey(cacheKey)) {
        players = cache.get(cacheKey);
      } else {
        players = dataSource.getPlayers(teamId, playerDataSet);
        cache.put(cacheKey, players);
      }
      synchronized (players) {
        PlayerColumn sortCol = tableSpec.getSortCol() == PlayerColumn.WIZARD ? PlayerColumn.PTS : tableSpec.getSortCol();
        Comparator<Player> comparator = sortCol.getComparator(tableSpec.isAscending());
        players = Ordering.from(comparator).sortedCopy(players);
      }
      return players;
    }
  }

  public ListMultimap<TeamDraftOrder, Integer> getAllKeepers() throws DataSourceException {
    return dataSource.getAllKeepers();
  }

  public long getBestPlayerId(final TeamDraftOrder teamDraftOrder,
      List<DraftPick> picks,
      final EnumSet<Position> openPositions,
      Map<Long, Float> pickProbabilityPredictions) throws DataSourceException {
    TeamId teamId = teamDataSource.getTeamIdByDraftOrder(teamDraftOrder);

    final Set<Long> selectedPlayerIds = new HashSet<>();
    for (DraftPick pick : picks) {
      selectedPlayerIds.add(pick.getPlayerId());
    }
    Predicate<Player> unselected = new Predicate<Player>() {
      @Override
      public boolean apply(Player player) {
        return !selectedPlayerIds.contains(player.getPlayerId());
      }
    };

    Predicate<Player> openPosition = new Predicate<Player>() {
      @Override
      public boolean apply(Player player) {
        return openPositions.isEmpty()
            || Position.apply(player, openPositions);
      }
    };

    Integer teamMinClosers = minClosers.get(teamDraftOrder);
    Integer teamMaxClosers = maxClosers.get(teamDraftOrder);
    int numPitcherSlots = RosterUtil.POSITIONS_AND_COUNTS.get(P);
    if ((teamMinClosers != null && teamMinClosers > 0) ||
        (teamMaxClosers != null && teamMaxClosers < numPitcherSlots)) {
      Iterable<DraftPick> teamPitcherPicks = Iterables.filter(picks,
          new Predicate<DraftPick>() {
            @Override
            public boolean apply(DraftPick pick) {
              return pick.getTeam() == teamDraftOrder.get() &&
                  pick.getEligibilities().contains("P");
            }
          });
      int numPitcherPicks = Iterables.size(teamPitcherPicks);
      if (numPitcherPicks < numPitcherSlots) {
        int closers = Iterables.size(Iterables.filter(teamPitcherPicks, PICK_IS_CLOSER));
        final boolean noClosers = teamMaxClosers != null && closers >= teamMaxClosers;
        final boolean closersOnly = teamMinClosers != null &&
            numPitcherSlots - numPitcherPicks + closers <= teamMinClosers;
        if (noClosers || closersOnly) {
          openPosition = Predicates.and(openPosition, new Predicate<Player>() {
            @Override
            public boolean apply(Player player) {
              if (Position.apply(player, EnumSet.of(P))) {
                if (noClosers) {
                  return Integer.parseInt(PlayerColumn.GS.get(player)) > Integer.parseInt(PlayerColumn.S.get(player));
                } else { // closersOnly
                  return Integer.parseInt(PlayerColumn.GS.get(player)) < Integer.parseInt(PlayerColumn.S.get(player));
                }
              }
              return true;
            }
          });
        }
      }

    }

    TableSpec tableSpec = beanFactory.createTableSpec().as();
    PlayerDataSet wizardTable = autoPickWizardTables.get(teamDraftOrder);
    tableSpec.setPlayerDataSet(wizardTable == null ? PlayerDataSet.CBSSPORTS : wizardTable);
    tableSpec.setSortCol(wizardTable == null ? PlayerColumn.MYRANK : PlayerColumn.PTS);
    tableSpec.setAscending(wizardTable == null);
    List<Player> players = getPlayers(teamId, tableSpec);
    Iterable<Player> unselectedPlayers = Iterables.filter(players, unselected);
    if (wizardTable != null) {
      PlayerColumn.calculateWizardScores(unselectedPlayers, pickProbabilityPredictions);
      unselectedPlayers = Ordering.from(PlayerColumn.getWizardComparator(false, openPositions))
          .sortedCopy(unselectedPlayers);
    }
    Player player = Iterables.getFirst(Iterables.filter(unselectedPlayers, openPosition), null);
    if (player == null) {
      player = Iterables.getFirst(unselectedPlayers, null);
      if (player == null) {
        throw new IllegalStateException("No players left???");
      }
    }
    return player.getPlayerId();
  }

  public void changePlayerRank(ChangePlayerRankRequest request) throws DataSourceException {
    if (teamTokens.containsKey(request.getTeamToken())) {
      TeamId teamID = teamDataSource.getTeamIdByDraftOrder(teamTokens.get(request.getTeamToken()));
      long playerId = request.getPlayerId();
      int prevRank = request.getPrevRank();
      int newRank = request.getNewRank();

      logger.info("Change player rank for team " + teamID
          + " player " + playerId + " from rank " + prevRank + " to new rank " + newRank);

      shiftInBetweenRanks(teamID, prevRank, newRank);
      updatePlayerRank(teamID, newRank, playerId);
    }
  }

  private void shiftInBetweenRanks(TeamId teamID, int prevRank, int newRank) {
    int lesserRank = prevRank + 1;
    int greaterRank = newRank;

    if (prevRank > newRank) {
      lesserRank = newRank;
      greaterRank = prevRank - 1;
    }

    dataSource.shiftInBetweenRanks(teamID, lesserRank, greaterRank, prevRank > newRank);

    // Update in caches
    synchronized (cache) {
      for (PlayerDataSet playerDataSet : PlayerDataSet.values()) {
        List<Player> players = cache.get(getKey(teamID, playerDataSet));
        if (players != null) {
          synchronized (players) {
            for (Player player : players) {
              int rank = Integer.parseInt(player.getMyRank());
              if (rank >= lesserRank && rank <= greaterRank) {
                if (prevRank > newRank) {
                  player.setMyRank(Integer.toString(rank + 1));
                } else {
                  player.setMyRank(Integer.toString(rank - 1));
                }
              }
            }
          }
        }
      }
    }
  }

  private void updatePlayerRank(TeamId teamID, int newRank, long playerId) {
    dataSource.updatePlayerRank(teamID, newRank, playerId);

    // Update in caches
    synchronized (cache) {
      for (PlayerDataSet playerDataSet : PlayerDataSet.values()) {
        List<Player> players = cache.get(getKey(teamID, playerDataSet));
        if (players != null) {
          synchronized (players) {
            for (Player player : players) {
              if (player.getPlayerId() == playerId) {
                player.setMyRank(Integer.toString(newRank));
                break;
              }
            }
          }
        }
      }
    }
  }

  public void copyTableSpecToCustom(CopyAllPlayerRanksRequest request) throws DataSourceException {
    final TeamId teamID = teamDataSource.getTeamIdByDraftOrder(teamTokens.get(request.getTeamToken()));
    TableSpec tableSpec = request.getTableSpec();
    if (tableSpec.getSortCol() == PlayerColumn.WIZARD || tableSpec.getSortCol() == PlayerColumn.MYRANK) {
      return;
    }
    dataSource.copyTableSpecToCustom(teamID, tableSpec);
    synchronized (cache) {
      for (PlayerDataSet playerDataSet : PlayerDataSet.values()) {
        cache.remove(getKey(teamID, playerDataSet));
      }
    }
  }

  public void addOrRemoveFavorite(AddOrRemoveFavoriteRequest request) throws DataSourceException {
    final TeamId teamID = teamDataSource.getTeamIdByDraftOrder(teamTokens.get(request.getTeamToken()));
    long playerId = request.getPlayerId();
    if (request.isAdd()) {
      dataSource.addFavorite(teamID, playerId);
    } else {
      dataSource.removeFavorite(teamID, playerId);
    }

    // Update in caches
    synchronized (cache) {
      for (PlayerDataSet playerDataSet : PlayerDataSet.values()) {
        List<Player> players = cache.get(getKey(teamID, playerDataSet));
        if (players != null) {
          synchronized (players) {
            for (Player player : players) {
              if (player.getPlayerId() == playerId) {
                player.setFavorite(request.isAdd());
                break;
              }
            }
          }
        }
      }
    }
  }

  public GraphsData getGraphsData(TeamDraftOrder teamDraftOrder) throws DataSourceException {
    return dataSource.getGraphsData(teamDraftOrder);
  }

  public void populateDraftStatus(DraftStatus status) throws DataSourceException {
    dataSource.populateDraftStatus(status);
  }

  public void populateDraftPick(DraftPick pick) throws DataSourceException {
    dataSource.populateDraftPick(pick);
  }

  public void postDraftPick(DraftPick pick, DraftStatus status) throws DataSourceException {
    dataSource.postDraftPick(pick, status);
  }

  public void backOutLastDraftPick(int pickToRemove) throws DataSourceException {
    dataSource.backOutLastDraftPick(pickToRemove);
  }

  public void populateQueueEntry(QueueEntry queueEntry) throws DataSourceException {
    dataSource.populateQueueEntry(queueEntry);
  }

  private static String getKey(TeamId teamId, PlayerDataSet playerDataSet) {
    return playerDataSet.ordinal() + ""
        + (teamId == null ? ServletEndpoints.LOGIN_GUEST : teamId.get());
  }

  public void reset() throws DataSourceException {
    synchronized (cache) {
      cache.clear();
      warmCaches();
    }
  }

  public void resetDraft() throws DataSourceException {
    dataSource.resetDraft();
  }
}