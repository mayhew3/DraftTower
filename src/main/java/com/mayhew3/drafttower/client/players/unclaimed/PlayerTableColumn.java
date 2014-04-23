package com.mayhew3.drafttower.client.players.unclaimed;

import com.google.gwt.cell.client.Cell;
import com.mayhew3.drafttower.shared.Player;
import com.mayhew3.drafttower.shared.PlayerColumn;
import gwtquery.plugins.droppable.client.gwt.DragAndDropColumn;

import static com.mayhew3.drafttower.shared.PlayerColumn.*;

/**
 * Base class for columns in player table.
 */
public abstract class PlayerTableColumn<C> extends DragAndDropColumn<Player, C> {

  protected final PlayerColumn column;

  public PlayerTableColumn(Cell<C> cell, PlayerColumn column) {
    super(cell);
    this.column = column;
    setSortable(true);

    if (column != NAME && column != MLB && column != ELIG) {
      setHorizontalAlignment(ALIGN_RIGHT);
    }
  }

  public PlayerColumn getColumn() {
    return column;
  }

  public abstract PlayerColumn getSortedColumn();

  public abstract ColumnSort getSortedColumn(boolean isAscending);

  protected abstract void updateDefaultSort();
}