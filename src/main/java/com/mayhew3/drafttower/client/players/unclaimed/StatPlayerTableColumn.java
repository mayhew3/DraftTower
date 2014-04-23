package com.mayhew3.drafttower.client.players.unclaimed;

import com.google.gwt.cell.client.Cell;
import com.google.inject.Provider;
import com.mayhew3.drafttower.shared.Player;
import com.mayhew3.drafttower.shared.PlayerColumn;
import com.mayhew3.drafttower.shared.Position;

import java.util.EnumSet;

/**
 * Player table column for scoring stat.
 */
public class StatPlayerTableColumn extends PlayerTableColumn<PlayerValue> {

  private final PlayerColumn pitcherColumn;
  private final UnclaimedPlayerDataProvider presenter;
  private final Provider<EnumSet<Position>> positionFilterProvider;

  public StatPlayerTableColumn(Cell<PlayerValue> cell,
      PlayerColumn column,
      PlayerColumn pitcherColumn,
      UnclaimedPlayerDataProvider presenter,
      Provider<EnumSet<Position>> positionFilterProvider) {
    super(cell, column);
    this.pitcherColumn = pitcherColumn;
    this.presenter = presenter;
    this.positionFilterProvider = positionFilterProvider;
    updateDefaultSort();
  }

  @Override
  protected void updateDefaultSort() {
    setDefaultSortAscending(pitcherColumn.isDefaultSortAscending()
        && Position.isPitcherFilter(positionFilterProvider.get()));
  }

  @Override
  public PlayerValue getValue(Player player) {
    if (presenter.getTableSpec().getSortCol() == column) {
      return new PlayerValue(player, column.get(player));
    }
    if (presenter.getTableSpec().getSortCol() == pitcherColumn) {
      return new PlayerValue(player, pitcherColumn.get(player));
    }
    if (pitcherColumn != null && pitcherColumn.get(player) != null) {
      return new PlayerValue(player, pitcherColumn.get(player));
    }
    return new PlayerValue(player, column.get(player));
  }

  @Override
  public PlayerColumn getSortedColumn() {
    return Position.isPitcherFilter(positionFilterProvider.get()) ? pitcherColumn : column;
  }

  @Override
  public ColumnSort getSortedColumn(boolean isAscending) {
    if (Position.isPitcherFilter(positionFilterProvider.get())) {
      return new ColumnSort(pitcherColumn, isAscending);
    } else if (Position.isPitchersAndBattersFilter(positionFilterProvider.get())) {
      PlayerColumn sortColumn = isAscending ? pitcherColumn : column;
      return new ColumnSort(sortColumn, sortColumn.isDefaultSortAscending());
    } else {
      return new ColumnSort(column, isAscending);
    }
  }
}