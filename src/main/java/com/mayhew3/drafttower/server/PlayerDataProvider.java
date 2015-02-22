package com.mayhew3.drafttower.server;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Ordering;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayhew3.drafttower.server.BindingAnnotations.TeamTokens;
import com.mayhew3.drafttower.shared.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static com.mayhew3.drafttower.shared.Position.DH;
import static com.mayhew3.drafttower.shared.Position.P;

/**
 * Handles caching and processing of player-related data.
 */
@Singleton
public class PlayerDataProvider {

  private static final Logger logger = Logger.getLogger(PlayerDataProvider.class.getName());

  private final PlayerDataSource dataSource;
  private final BeanFactory beanFactory;
  private final TeamDataSource teamDataSource;
  private final Map<String, TeamDraftOrder> teamTokens;

  private final Map<String, List<Player>> cache = new ConcurrentHashMap<>();

  @Inject
  public PlayerDataProvider(PlayerDataSource dataSource,
      BeanFactory beanFactory,
      TeamDataSource teamDataSource,
      @TeamTokens Map<String, TeamDraftOrder> teamTokens) throws DataSourceException {
    this.dataSource = dataSource;
    this.beanFactory = beanFactory;
    this.teamDataSource = teamDataSource;
    this.teamTokens = teamTokens;

    warmCaches();
  }

  private void warmCaches() throws DataSourceException {
    TableSpec tableSpec = beanFactory.createTableSpec().as();
    tableSpec.setSortCol(PlayerColumn.MYRANK);
    tableSpec.setAscending(true);
    for (int i = 1; i <= 10; i++) {
      logger.info("Warming caches: " + i + "/10");
      TeamId team = new TeamId(i);
      for (PlayerDataSet playerDataSet : PlayerDataSet.values()) {
        tableSpec.setPlayerDataSet(playerDataSet);
        getPlayers(team, tableSpec);
      }
    }
  }

  public UnclaimedPlayerListResponse lookupUnclaimedPlayers(UnclaimedPlayerListRequest request) throws DataSourceException {
    Stopwatch stopwatch = Stopwatch.createStarted();
    UnclaimedPlayerListResponse response = beanFactory.createUnclaimedPlayerListResponse().as();

    TeamId teamId = teamDataSource.getTeamIdByDraftOrder(teamTokens.get(request.getTeamToken()));
    TableSpec tableSpec = request.getTableSpec();
    List<Player> players = getPlayers(teamId, tableSpec);

    response.setPlayers(players);

    stopwatch.stop();
    logger.info("Player table request took " + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms");
    return response;
  }

  public List<Player> getPlayers(TeamId teamId, TableSpec tableSpec) throws DataSourceException {
    List<Player> players;
    PlayerDataSet playerDataSet = tableSpec.getPlayerDataSet();
    String cacheKey = getKey(teamId, playerDataSet);
    if (cache.containsKey(cacheKey)) {
      players = cache.get(cacheKey);
    } else {
      players = dataSource.getPlayers(teamId, playerDataSet);
      cache.put(cacheKey, players);
    }
    synchronized (players) {
      Comparator<Player> comparator = tableSpec.getSortCol() == PlayerColumn.WIZARD
          ? PlayerColumn.getWizardComparator(tableSpec.isAscending(), EnumSet.allOf(Position.class))
          : tableSpec.getSortCol().getComparator(tableSpec.isAscending());
      players = Ordering.from(comparator).sortedCopy(players);
    }
    return players;
  }

  public ListMultimap<TeamDraftOrder, Integer> getAllKeepers() throws DataSourceException {
    return dataSource.getAllKeepers();
  }

  public long getBestPlayerId(PlayerDataSet wizardTable, TeamDraftOrder teamDraftOrder, List<DraftPick> picks, final EnumSet<Position> openPositions) throws DataSourceException {
    TeamId teamId = teamDataSource.getTeamIdByDraftOrder(teamDraftOrder);

    final Set<Long> selectedPlayerIds = new HashSet<>();
    for (DraftPick pick : picks) {
      selectedPlayerIds.add(pick.getPlayerId());
    }

    TableSpec tableSpec = beanFactory.createTableSpec().as();
    tableSpec.setPlayerDataSet(wizardTable == null ? PlayerDataSet.CBSSPORTS : wizardTable);
    tableSpec.setSortCol(wizardTable == null ? PlayerColumn.MYRANK : PlayerColumn.WIZARD);
    tableSpec.setAscending(wizardTable == null);
    List<Player> players = getPlayers(teamId, tableSpec);
    Iterable<Player> unselectedPlayers = Iterables.filter(players,
        new Predicate<Player>() {
          @Override
          public boolean apply(Player player) {
            return !selectedPlayerIds.contains(player.getPlayerId());
          }
        });
    Player player = Iterables.getFirst(Iterables.filter(unselectedPlayers,
        new Predicate<Player>() {
          @Override
          public boolean apply(Player player) {
            return openPositions.isEmpty()
                || hasAllOpenPositions(openPositions)
                || Position.apply(player, openPositions);
          }
        }), null);
    if (player == null) {
      player = Iterables.getFirst(unselectedPlayers, null);
      if (player == null) {
        throw new IllegalStateException("No players left???");
      }
    }
    return player.getPlayerId();
  }

  private boolean hasAllOpenPositions(Set<Position> openPositions) {
    return openPositions.size() == Position.REAL_POSITIONS.size()
        || (openPositions.contains(DH) && openPositions.contains(P));
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
    int lesserRank = prevRank+1;
    int greaterRank = newRank;

    if (prevRank > newRank) {
      lesserRank = newRank;
      greaterRank = prevRank-1;
    }

    dataSource.shiftInBetweenRanks(teamID, lesserRank, greaterRank, prevRank > newRank);

    // Update in caches
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

  private void updatePlayerRank(TeamId teamID, int newRank, long playerId) {
    dataSource.updatePlayerRank(teamID, newRank, playerId);

    // Update in caches
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

  public void copyTableSpecToCustom(CopyAllPlayerRanksRequest request) throws DataSourceException {
    final TeamId teamID = teamDataSource.getTeamIdByDraftOrder(teamTokens.get(request.getTeamToken()));
    TableSpec tableSpec = request.getTableSpec();
    dataSource.copyTableSpecToCustom(teamID, tableSpec);
    for (PlayerDataSet playerDataSet : PlayerDataSet.values()) {
      cache.remove(getKey(teamID, playerDataSet));
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
        + teamId.get();
  }

  @VisibleForTesting
  public void reset() throws DataSourceException {
    cache.clear();
    warmCaches();
  }
}