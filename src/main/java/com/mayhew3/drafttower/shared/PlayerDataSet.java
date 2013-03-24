package com.mayhew3.drafttower.shared;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.ArrayList;

/** Data sources for player stats. */
public enum PlayerDataSet {
  WIZARD("Wizard", "UnclaimedDisplayPlayersWithCatsByQuality", null, "Rating", "DESC"),
  CBS("CBSSports", "projectionsView", "CBSSports", "Rank", "ASC"),
  GURU("GURU", "projectionsView", "GURU", "Rank", "ASC"),
  LAST_YEAR("2012", "UnclaimedDisplayPlayersWithCatsByQuality", null, "Rating", "DESC"),
  CUSTOM("Custom", "rankingsView", null, "Rank", "ASC");

  private final String displayName;
  private final String tableName;
  private final String sourceFilter;
  private final String startingSort;
  private final String startingSortDirection;

  PlayerDataSet(String displayName,
      String tableName,
      String sourceFilter,
      String startingSort,
      String startingSortDirection) {
    this.displayName = displayName;
    this.tableName = tableName;
    this.sourceFilter = sourceFilter;
    this.startingSort = startingSort;
    this.startingSortDirection = startingSortDirection;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getTableName() {
    return tableName;
  }

  public String getSourceFilter() {
    return sourceFilter;
  }

  public String getStartingSort() {
    return startingSort;
  }

  public String getStartingSortDirection() {
    return startingSortDirection;
  }

  public static Optional<PlayerDataSet> getDataSetWithName(final String displayName) {
    ArrayList<PlayerDataSet> playerColumns = Lists.newArrayList(PlayerDataSet.values());

    return Iterables.tryFind(playerColumns, new Predicate<PlayerDataSet>() {
      @Override
      public boolean apply(PlayerDataSet input) {
        return displayName.equalsIgnoreCase(input.getDisplayName());
      }
    });
  }
}
