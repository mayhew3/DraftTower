package com.mayhew3.drafttower.client;

import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.inject.Inject;
import com.mayhew3.drafttower.client.DraftSocketHandler.DraftStatusListener;
import com.mayhew3.drafttower.shared.DraftStatus;
import com.mayhew3.drafttower.shared.Player;
import com.mayhew3.drafttower.shared.PlayerColumn;

import static com.mayhew3.drafttower.shared.PlayerColumn.*;

/**
 * Table widget for displaying player stats.
 */
public class PlayerTable extends CellTable<Player> {

  public class PlayerTableColumn extends TextColumn<Player> {
    private final PlayerColumn column;

    public PlayerTableColumn(PlayerColumn column) {
      this.column = column;
      setSortable(true);
    }

    @Override
    public String getValue(Player player) {
      return player.getColumnValues().get(column);
    }

    public PlayerColumn getColumn() {
      return column;
    }
  }

  public static final PlayerColumn PITCHER_COLUMNS[] = {
      NAME, POS, INN, K, ERA, WHIP, WL, S
  };
  public static final PlayerColumn BATTER_COLUMNS[] = {
      NAME, POS, HR, RBI, OBP, SLG, RHR, SBCS
  };

  @Inject
  public PlayerTable(UnclaimedPlayerDataProvider dataProvider,
      DraftSocketHandler socketHandler) {
    // TODO
    PlayerColumn[] columns = PITCHER_COLUMNS;

    for (PlayerColumn column : columns) {
      addColumn(new PlayerTableColumn(column), column.getShortName());
    }

    dataProvider.addDataDisplay(this);
    addColumnSortHandler(new AsyncHandler(this));

    socketHandler.addListener(new DraftStatusListener() {
      public void onConnect() {
        // No-op.
      }

      public void onMessage(DraftStatus status) {
        // TODO: limit to status updates that change player list?
        setVisibleRangeAndClearData(getVisibleRange(), true);
      }

      public void onDisconnect() {
        // No-op.
      }
    });
  }

}