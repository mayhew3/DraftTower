package com.mayhew3.drafttower.client;

import com.google.gwt.cell.client.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.mayhew3.drafttower.client.events.ChangePlayerRankEvent;
import com.mayhew3.drafttower.client.events.PlayerSelectedEvent;
import com.mayhew3.drafttower.shared.Player;
import com.mayhew3.drafttower.shared.PlayerColumn;
import com.mayhew3.drafttower.shared.PlayerDataSet;
import com.mayhew3.drafttower.shared.Position;
import gwtquery.plugins.draggable.client.events.DragStartEvent;
import gwtquery.plugins.draggable.client.events.DragStartEvent.DragStartEventHandler;
import gwtquery.plugins.droppable.client.DroppableOptions.DroppableFunction;
import gwtquery.plugins.droppable.client.events.DragAndDropContext;
import gwtquery.plugins.droppable.client.gwt.DragAndDropColumn;

import static com.mayhew3.drafttower.shared.PlayerColumn.*;

/**
 * Table widget for displaying player stats.
 */
public class UnclaimedPlayerTable extends PlayerTable<Player> {

  interface Resources extends ClientBundle {
    interface Css extends CssResource {
      String injury();
    }

    @Source("UnclaimedPlayerTable.css")
    Css css();
  }

  protected static final Resources.Css CSS = ((Resources) GWT.create(Resources.class)).css();
  static {
    CSS.ensureInjected();
  }

  private class RankCell extends EditTextCell {
    @Override
    public void onBrowserEvent(final Context context, final com.google.gwt.dom.client.Element parent, final String value, final NativeEvent event, final ValueUpdater<String> valueUpdater) {
      if (playerDataSet == PlayerDataSet.CUSTOM) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);
      }
    }
  }

  public class PlayerTableColumn extends DragAndDropColumn<Player, String> {

    private final PlayerColumn column;

    public PlayerTableColumn(PlayerColumn column) {
      super(column == RANK ? new RankCell() : new TextCell());
      this.column = column;
      setSortable(true);

      if (column == RANK) {
        setFieldUpdater(new FieldUpdater<Player, String>() {
          @Override
          public void update(int index, Player player, String newRank) {
            if (!newRank.equals(player.getColumnValues().get(RANK))) {
              try {
                eventBus.fireEvent(new ChangePlayerRankEvent(player.getPlayerId(),
                    Integer.parseInt(newRank)));
              } catch (NumberFormatException e) {
                // whatevs
              }
            }
          }
        });
      }

      DroppableFunction onDrop = new DroppableFunction() {
        @Override
        public void f(DragAndDropContext dragAndDropContext) {
          Player draggedPlayer = dragAndDropContext.getDraggableData();
          Player droppedPlayer = dragAndDropContext.getDroppableData();
          eventBus.fireEvent(new ChangePlayerRankEvent(
              draggedPlayer.getPlayerId(),
              Integer.parseInt(droppedPlayer.getColumnValues().get(RANK)) + 1));
        }
      };
      initDragging(this, onDrop);
    }

    @Override
    public String getValue(Player player) {
      return player.getColumnValues().get(column);
    }

    public PlayerColumn getColumn() {
      return column;
    }
  }

  static {
    BASE_CSS.ensureInjected();
  }

  public static final PlayerColumn COLUMNS[] = {
      NAME, POS, ELIG, HR, RBI, OBP, SLG, RHR, SBCS, INN, K, ERA, WHIP, WL, S, RANK, RATING
  };

  private Position positionFilter;
  private PlayerDataSet playerDataSet = PlayerDataSet.WIZARD;
  private boolean hideInjuries;

  @Inject
  public UnclaimedPlayerTable(UnclaimedPlayerDataProvider dataProvider,
      final EventBus eventBus) {
    super(eventBus);

    addStyleName(BASE_CSS.table());
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
    addColumnSortHandler(new AsyncHandler(this) {
      @Override
      public void onColumnSort(ColumnSortEvent event) {
        super.onColumnSort(event);
        updateDropEnabled();
      }
    });

    addDragStartHandler(new DragStartEventHandler() {
      @Override
      public void onDragStart(DragStartEvent dragStartEvent) {
        Player player = dragStartEvent.getDraggableData();
        dragStartEvent.getHelper().setInnerSafeHtml(
            new SafeHtmlBuilder().appendEscaped(
                player.getColumnValues().get(NAME)).toSafeHtml());
      }
    });
    updateDropEnabled();

    final SingleSelectionModel<Player> selectionModel = new SingleSelectionModel<Player>();
    setSelectionModel(selectionModel);
    getSelectionModel().addSelectionChangeHandler(new Handler() {
      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        eventBus.fireEvent(new PlayerSelectedEvent(selectionModel.getSelectedObject()));
      }
    });
    setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
  }

  public PlayerColumn getSortedColumn() {
    ColumnSortList columnSortList = getColumnSortList();
    if (columnSortList.size() > 0) {
      return ((PlayerTableColumn) columnSortList.get(0).getColumn()).getColumn();
    }
    return null;
  }

  public Position getPositionFilter() {
    return positionFilter;
  }

  public PlayerDataSet getPlayerDataSet() {
    return playerDataSet;
  }

  public boolean getHideInjuries() {
    return hideInjuries;
  }

  public void setPositionFilter(Position positionFilter) {
    this.positionFilter = positionFilter;
    setVisibleRangeAndClearData(getVisibleRange(), true);
  }

  public void setPlayerDataSet(PlayerDataSet playerDataSet) {
    this.playerDataSet = playerDataSet;
    updateDropEnabled();
    setVisibleRangeAndClearData(getVisibleRange(), true);
  }

  private void updateDropEnabled() {
    boolean dropEnabled = playerDataSet == PlayerDataSet.CUSTOM
        && getSortedColumn() == RANK;
    for (int i = 0; i < getColumnCount(); i++) {
      Column<Player, ?> column = getColumn(i);
      if (column instanceof DragAndDropColumn) {
        ((DragAndDropColumn) column).getDroppableOptions().setDisabled(!dropEnabled);
      }
    }
  }

  public void setHideInjuries(boolean hideInjuries) {
    this.hideInjuries = hideInjuries;
    setVisibleRangeAndClearData(getVisibleRange(), true);
  }

}