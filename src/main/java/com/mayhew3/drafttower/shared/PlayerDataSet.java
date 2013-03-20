package com.mayhew3.drafttower.shared;

import static com.mayhew3.drafttower.shared.PlayerColumn.*;
import static com.mayhew3.drafttower.shared.PlayerColumn.RATING;

/** Data sources for player stats. */
public enum PlayerDataSet {
  WIZARD("Wizard", "UnclaimedDisplayPlayersWithCatsByQuality", null, "Rating DESC"),
  CBS("CBSSports", "projectionsView", "CBSSports", "Rank ASC"),
  GURU("GURU", "projectionsView", "GURU", "Rank ASC"),
  LAST_YEAR("2012", "UnclaimedDisplayPlayersWithCatsByQuality", null, "Rating DESC"),
  CUSTOM("Custom", "UnclaimedDisplayPlayersWithCatsByQuality", null, "Rating DESC");

  private final String displayName;
  private final String tableName;
  private final String sourceFilter;
  private final String startingSort;

  PlayerDataSet(String displayName, String tableName, String sourceFilter, String startingSort) {
    this.displayName = displayName;
    this.tableName = tableName;
    this.sourceFilter = sourceFilter;
    this.startingSort = startingSort;
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
}
