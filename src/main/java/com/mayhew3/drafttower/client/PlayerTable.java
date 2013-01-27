package com.mayhew3.drafttower.client;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.inject.Inject;
import com.mayhew3.drafttower.client.DraftSocketHandler.DraftStatusListener;
import com.mayhew3.drafttower.shared.DraftStatus;
import com.mayhew3.drafttower.shared.Player;
import com.mayhew3.drafttower.shared.PlayerColumn;
import com.mayhew3.drafttower.shared.Position;

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

  public static final PlayerColumn COLUMNS[] = {
      NAME, POS, ELIG, HR, RBI, OBP, SLG, RHR, SBCS, INN, K, ERA, WHIP, WL, S, RANK, RATING
  };

  private Position positionFilter;

  @Inject
  public PlayerTable(UnclaimedPlayerDataProvider dataProvider,
      DraftSocketHandler socketHandler) {
    setPageSize(20);

    for (PlayerColumn column : COLUMNS) {
      addColumn(new PlayerTableColumn(column),
          new SafeHtmlBuilder()
              .appendHtmlConstant("<span title=\"")
              .appendEscaped(column.getLongName())
              .appendHtmlConstant("\">")
              .appendEscaped(column.getShortName())
              .appendHtmlConstant("</span>")
              .toSafeHtml());
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

  public Position getPositionFilter() {
    return positionFilter;
  }

  public void setPositionFilter(Position positionFilter) {
    this.positionFilter = positionFilter;
    setVisibleRangeAndClearData(getVisibleRange(), true);
  }
}