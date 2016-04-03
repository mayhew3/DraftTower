package com.mayhew3.drafttower.client.players.unclaimed;

import com.google.gwt.cell.client.Cell;
import com.mayhew3.drafttower.client.players.PositionFilter;
import com.mayhew3.drafttower.shared.Player;
import com.mayhew3.drafttower.shared.PlayerColumn;

import javax.inject.Provider;

import static com.mayhew3.drafttower.shared.PlayerColumn.MYRANK;
import static com.mayhew3.drafttower.shared.PlayerColumn.WIZARD;

/**
 * Player table column not representing a scoring stat.
 */
public class NonStatPlayerTableColumn extends PlayerTableColumn<String> {

  private final Provider<PositionFilter> positionFilterProvider;

  public NonStatPlayerTableColumn(Cell<String> cell,
      PlayerColumn column,
      final UnclaimedPlayerDataProvider presenter,
      Provider<PositionFilter> positionFilterProvider) {
    super(cell, column);
    this.positionFilterProvider = positionFilterProvider;

    setDefaultSortAscending(column.isDefaultSortAscending());

    if (column == MYRANK) {
      setFieldUpdater(new MyRankFieldUpdater(presenter));
    }
  }

  @Override
  public String getValue(Player player) {
    if (column == WIZARD) {
      return positionFilterProvider.get().getWizard(player);
    } else {
      return column.get(player);
    }
  }

  @Override
  public PlayerColumn getSortedColumn() {
    return column;
  }

  @Override
  public ColumnSort getSortedColumn(boolean isAscending) {
    return new ColumnSort(column, isAscending);
  }

  @Override
  protected void updateDefaultSort() {
    // No-op.
  }
}