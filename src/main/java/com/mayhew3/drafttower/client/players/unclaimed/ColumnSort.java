package com.mayhew3.drafttower.client.players.unclaimed;

import com.mayhew3.drafttower.shared.PlayerColumn;

/**
 * Represents a specific instance of sort settings.
 */
class ColumnSort {
  private final PlayerColumn column;
  private final boolean isAscending;

  ColumnSort(PlayerColumn column, boolean ascending) {
    this.column = column;
    this.isAscending = ascending;
  }

  PlayerColumn getColumn() {
    return column;
  }

  boolean isAscending() {
    return isAscending;
  }
}