package com.mayhew3.drafttower.client.players.unclaimed;

import com.google.gwt.cell.client.Cell;
import com.google.inject.Provider;
import com.mayhew3.drafttower.shared.Player;
import com.mayhew3.drafttower.shared.PlayerColumn;
import com.mayhew3.drafttower.shared.Position;

import java.util.EnumSet;

import static com.mayhew3.drafttower.shared.PlayerColumn.MYRANK;
import static com.mayhew3.drafttower.shared.PlayerColumn.WIZARD;

/**
 * Player table column not representing a scoring stat.
 */
public class NonStatPlayerTableColumn extends PlayerTableColumn<String> {

  private final Provider<EnumSet<Position>> positionFilterProvider;

  public NonStatPlayerTableColumn(Cell<String> cell,
      PlayerColumn column,
      final UnclaimedPlayerDataProvider presenter,
      Provider<EnumSet<Position>> positionFilterProvider) {
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
      return PlayerColumn.getWizard(player, positionFilterProvider.get());
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