package com.mayhew3.drafttower.client.players.unclaimed;

import com.google.gwt.cell.client.Cell;
import com.google.inject.Provider;
import com.mayhew3.drafttower.client.players.PositionFilter;
import com.mayhew3.drafttower.shared.Player;
import com.mayhew3.drafttower.shared.PlayerColumn;

/**
 * Player table column for scoring stat.
 */
public class StatPlayerTableColumn extends PlayerTableColumn<PlayerValue> {

  private final PlayerColumn pitcherColumn;
  private final UnclaimedPlayerDataProvider presenter;
  private final Provider<PositionFilter> positionFilterProvider;

  public StatPlayerTableColumn(Cell<PlayerValue> cell,
      PlayerColumn column,
      PlayerColumn pitcherColumn,
      UnclaimedPlayerDataProvider presenter,
      Provider<PositionFilter> positionFilterProvider) {
    super(cell, column);
    this.pitcherColumn = pitcherColumn;
    this.presenter = presenter;
    this.positionFilterProvider = positionFilterProvider;
    updateDefaultSort();
  }

  @Override
  protected void updateDefaultSort() {
    setDefaultSortAscending(pitcherColumn.isDefaultSortAscending()
        && positionFilterProvider.get().isPitcherFilter());
  }

  @Override
  public PlayerValue getValue(Player player) {
    if (presenter.getTableSpec().getSortCol() == column) {
      return getColumnValue(player, column);
    }
    if (presenter.getTableSpec().getSortCol() == pitcherColumn) {
      return getColumnValue(player, pitcherColumn);
    }
    if (pitcherColumn != null && pitcherColumn.get(player) != null) {
      return getColumnValue(player, pitcherColumn);
    }
    return getColumnValue(player, column);
  }

  private PlayerValue getColumnValue(Player player, PlayerColumn column) {
    if (column == PlayerColumn.WIZARD) {
      return new PlayerValue(player, positionFilterProvider.get().getWizard(player));
    } else {
      return new PlayerValue(player, column.get(player));
    }
  }

  @Override
  public PlayerColumn getSortedColumn() {
    return positionFilterProvider.get().isPitcherFilter() ? pitcherColumn : column;
  }

  @Override
  public ColumnSort getSortedColumn(boolean isAscending) {
    if (positionFilterProvider.get().isPitcherFilter()) {
      return new ColumnSort(pitcherColumn, isAscending);
    } else if (positionFilterProvider.get().isPitchersAndBattersFilter()) {
      PlayerColumn sortColumn = isAscending ? pitcherColumn : column;
      return new ColumnSort(sortColumn, sortColumn.isDefaultSortAscending());
    } else {
      return new ColumnSort(column, isAscending);
    }
  }
}