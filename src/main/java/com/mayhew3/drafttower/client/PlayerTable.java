package com.mayhew3.drafttower.client;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent;
import com.mayhew3.drafttower.client.events.PlayerSelectedEvent;
import com.mayhew3.drafttower.shared.Player;
import com.mayhew3.drafttower.shared.PlayerColumn;
import com.mayhew3.drafttower.shared.Position;
import com.mayhew3.drafttower.shared.ProjectionSystem;

import static com.mayhew3.drafttower.shared.PlayerColumn.*;

/**
 * Table widget for displaying player stats.
 */
public class PlayerTable extends CellTable<Player> implements
    DraftStatusChangedEvent.Handler {

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

  interface Resources extends ClientBundle {
    interface Css extends CssResource {
      String table();
      String injury();
    }

    @Source("PlayerTable.css")
    Css css();
  }

  private static final Resources.Css CSS = ((Resources) GWT.create(Resources.class)).css();
  static {
    CSS.ensureInjected();
  }

  public static final PlayerColumn COLUMNS[] = {
      NAME, POS, ELIG, HR, RBI, OBP, SLG, RHR, SBCS, INN, K, ERA, WHIP, WL, S, RANK, RATING
  };

  private Position positionFilter;
  private ProjectionSystem projectionSystem = ProjectionSystem.CBS;

  @Inject
  public PlayerTable(UnclaimedPlayerDataProvider dataProvider,
      final EventBus eventBus) {
    addStyleName(CSS.table());
    setPageSize(40);

    addColumn(new Column<Player, SafeHtml>(new SafeHtmlCell()) {
      @Override
      public SafeHtml getValue(Player player) {
        if (player.getInjury() != null) {
          return new SafeHtmlBuilder()
              .appendHtmlConstant("<span ")
              .appendHtmlConstant("class=\"")
              .appendEscaped(CSS.injury())
              .appendHtmlConstant("\" title=\"")
              .appendEscaped(player.getInjury())
              .appendHtmlConstant("\">✚</span>")
              .toSafeHtml();
        }
        return null;
      }
    });

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

    final SingleSelectionModel<Player> selectionModel = new SingleSelectionModel<Player>();
    setSelectionModel(selectionModel);
    getSelectionModel().addSelectionChangeHandler(new Handler() {
      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        eventBus.fireEvent(new PlayerSelectedEvent(selectionModel.getSelectedObject()));
      }
    });

    eventBus.addHandler(DraftStatusChangedEvent.TYPE, this);
  }

  public Position getPositionFilter() {
    return positionFilter;
  }

  public ProjectionSystem getProjectionSystem() {
    return projectionSystem;
  }

  public void setPositionFilter(Position positionFilter) {
    this.positionFilter = positionFilter;
    setVisibleRangeAndClearData(getVisibleRange(), true);
  }

  public void setProjectionSystem(ProjectionSystem projectionSystem) {
    this.projectionSystem = projectionSystem;
    setVisibleRangeAndClearData(getVisibleRange(), true);
  }

  @Override
  public void onDraftStatusChanged(DraftStatusChangedEvent event) {
    // TODO: limit to status updates that change player list?
    setVisibleRangeAndClearData(getVisibleRange(), true);
  }
}