package com.mayhew3.drafttower.client.players.unclaimed;

import com.mayhew3.drafttower.shared.PlayerColumn;
import com.mayhew3.drafttower.shared.Position;

import java.util.EnumSet;

/**
 * Represents a specific instance of sort and position filter settings.
 */
public class SortSpec {
  private final PlayerColumn column;
  private final EnumSet<Position> wizardPosition;
  private final boolean ascending;

  SortSpec(PlayerColumn column, EnumSet<Position> positions, boolean ascending) {
    this.column = column;
    wizardPosition = column == PlayerColumn.WIZARD ? positions : null;
    this.ascending = ascending;
  }

  public PlayerColumn getColumn() {
    return column;
  }

  public boolean isAscending() {
    return ascending;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SortSpec sortSpec = (SortSpec) o;

    if (ascending != sortSpec.ascending) return false;
    if (column != sortSpec.column) return false;
    if (wizardPosition != sortSpec.wizardPosition) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = column.hashCode();
    result = 31 * result + (wizardPosition != null ? wizardPosition.hashCode() : 0);
    result = 31 * result + (ascending ? 1 : 0);
    return result;
  }
}