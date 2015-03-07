package com.mayhew3.drafttower.client.players.unclaimed;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import com.mayhew3.drafttower.client.players.PositionFilter;
import com.mayhew3.drafttower.shared.*;

import java.util.*;
import java.util.Map.Entry;

/**
 * Encapsulates the list of players for a given data set; handles sorting, filtering, etc.
 */
public class PlayerList {
  @VisibleForTesting final Map<SortSpec, List<Player>> playersBySort = new HashMap<>();
  private final Set<Long> pickedPlayers = new HashSet<>();

  private PositionFilter cachedWizardSortPositionFilter;

  public PlayerList(List<Player> players,
      PlayerColumn defaultSortCol,
      boolean defaultSortAscending) {
    playersBySort.put(new SortSpec(defaultSortCol, defaultSortAscending), players);
  }

  public Iterable<Player> getPlayers(TableSpec tableSpec,
      int rowStart, int rowCount,
      final PositionFilter positionFilter,
      final EnumSet<Position> excludedPositions,
      final boolean hideInjuries,
      final String nameFilter) {
    if (cachedWizardSortPositionFilter != null &&
        tableSpec.getSortCol() == PlayerColumn.WIZARD &&
        !positionFilter.equals(cachedWizardSortPositionFilter)) {
      clearCachedSort(PlayerColumn.WIZARD, playersBySort.values().iterator().next());
    }
    if (tableSpec.getSortCol() == PlayerColumn.WIZARD) {
      cachedWizardSortPositionFilter = positionFilter;
    }
    SortSpec sortSpec = new SortSpec(tableSpec.getSortCol(), tableSpec.isAscending());
    if (!playersBySort.containsKey(sortSpec)) {
      List<Player> players = playersBySort.values().iterator().next();
      Comparator<Player> comparator = sortSpec.getColumn() == PlayerColumn.WIZARD
          ? positionFilter.getWizardComparator(sortSpec.isAscending())
          : sortSpec.getColumn().getComparator(sortSpec.isAscending());
      playersBySort.put(sortSpec,
          Ordering.from(comparator).sortedCopy(players));
    }
    return Iterables.limit(Iterables.skip(Iterables.filter(
        playersBySort.get(sortSpec),
        new Predicate<Player>() {
          @Override
          public boolean apply(Player player) {
            return (nameFilter == null
                || PlayerColumn.NAME.get(player).toLowerCase()
                .contains(nameFilter.toLowerCase()))
                && (!hideInjuries || player.getInjury() == null)
                && positionFilter.apply(player, excludedPositions)
                && !pickedPlayers.contains(player.getPlayerId());
          }
        }), rowStart), rowCount);
  }

  public int getTotalPlayers() {
    return playersBySort.values().iterator().next().size() - pickedPlayers.size();
  }

  public void ensurePlayersRemoved(List<DraftPick> picks) {
    pickedPlayers.clear();
    for (DraftPick pick : picks) {
      pickedPlayers.add(pick.getPlayerId());
    }
  }

  public void updatePlayerRank(long playerId, int prevRank, int newRank) {
    List<Player> players = playersBySort.values().iterator().next();
    int lesserRank = prevRank + 1;
    int greaterRank = newRank;
    if (prevRank > newRank) {
      lesserRank = newRank;
      greaterRank = prevRank - 1;
    }
    // Update all players.
    for (Player player : players) {
      if (player.getPlayerId() == playerId) {
        player.setMyRank(Integer.toString(newRank));
      } else {
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
    clearCachedSort(PlayerColumn.MYRANK, players);
  }

  public void updateWizardValues(Map<Long, Float> pickProbabilityPredictions) {
    List<Player> players = playersBySort.values().iterator().next();
    PlayerColumn.calculateWizardScores(Iterables.filter(players, new Predicate<Player>() {
      @Override
      public boolean apply(Player player) {
        return !pickedPlayers.contains(player.getPlayerId());
      }
    }), pickProbabilityPredictions);
    clearCachedSort(PlayerColumn.WIZARD, players);
  }

  private void clearCachedSort(PlayerColumn column, List<Player> players) {
    Set<SortSpec> keysToRemove = new HashSet<>();
    for (Entry<SortSpec, List<Player>> entry : playersBySort.entrySet()) {
      SortSpec sortSpec = entry.getKey();
      if (sortSpec.getColumn() == column) {
        keysToRemove.add(sortSpec);
      }
    }
    for (SortSpec sortSpec : keysToRemove) {
      playersBySort.remove(sortSpec);
    }
    // Ensure we don't hit the server again if the only sort order we had was by rank.
    if (playersBySort.isEmpty()) {
      Comparator<Player> comparator = column == PlayerColumn.WIZARD
          ? cachedWizardSortPositionFilter == null
              ? PlayerColumn.getWizardComparator(false, EnumSet.allOf(Position.class))
              : cachedWizardSortPositionFilter.getWizardComparator(false)
          : column.getComparator(true);
      playersBySort.put(new SortSpec(column, true),
          Ordering.from(comparator).sortedCopy(players));
    }
  }
}