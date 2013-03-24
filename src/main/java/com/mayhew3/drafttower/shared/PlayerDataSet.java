package com.mayhew3.drafttower.shared;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.ArrayList;

/** Data sources for player stats. */
public enum PlayerDataSet {
  WIZARD("Wizard", "UnclaimedDisplayPlayersWithCatsByQuality", null),
  CBS("CBSSports", "projectionsView", "CBSSports"),
  GURU("GURU", "projectionsView", "GURU"),
  CUSTOM("Custom", "rankingsView", null);

  private final String displayName;
  private final String tableName;
  private final String sourceFilter;

  PlayerDataSet(String displayName,
                String tableName,
                String sourceFilter) {
    this.displayName = displayName;
    this.tableName = tableName;
    this.sourceFilter = sourceFilter;
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
