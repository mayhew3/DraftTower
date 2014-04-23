package com.mayhew3.drafttower.client.players.unclaimed;

import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.AbstractHeaderOrFooterBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.client.Window;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.mayhew3.drafttower.client.players.PlayerTable;
import com.mayhew3.drafttower.shared.Player;
import com.mayhew3.drafttower.shared.PlayerColumn;
import com.mayhew3.drafttower.shared.Position;
import com.mayhew3.drafttower.shared.TableSpec;
import gwtquery.plugins.draggable.client.events.DragStartEvent;
import gwtquery.plugins.draggable.client.events.DragStartEvent.DragStartEventHandler;
import gwtquery.plugins.droppable.client.DroppableOptions.DroppableFunction;
import gwtquery.plugins.droppable.client.events.DragAndDropContext;
import gwtquery.plugins.droppable.client.gwt.DragAndDropColumn;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import static com.google.gwt.user.client.ui.HasHorizontalAlignment.ALIGN_RIGHT;
import static com.mayhew3.drafttower.shared.PlayerColumn.*;

/**
 * Table widget for displaying player stats.
 */
public class UnclaimedPlayerTable extends PlayerTable<Player>
    implements UnclaimedPlayerTableView {

  interface Resources extends ClientBundle {
    interface Css extends CssResource {
      String injury();
      String newsCell();
      String rightAlign();
      String batterStat();
      String pitcherStat();
      String splitHeader();
    }

    @Source("UnclaimedPlayerTable.css")
    Css css();
  }

  protected static final Resources.Css CSS = ((Resources) GWT.create(Resources.class)).css();
  static {
    CSS.ensureInjected();
    BASE_CSS.ensureInjected();
  }

  private static final PlayerColumn COLUMNS[] = {
      NAME, MLB, ELIG, G, AB, HR, RBI, OBP, SLG, RHR, SBCS, RANK, WIZARD, DRAFT, MYRANK
  };
  private static final PlayerColumn PITCHER_COLUMNS[] = {
      null, null, null, null, null, INN, K, ERA, WHIP, WL, S, null, null, null, null, null
  };

  private final UnclaimedPlayerDataProvider presenter;

  private Provider<Integer> queueAreaTopProvider;

  private final Map<PlayerColumn, PlayerTableColumn<?>> playerColumns = new EnumMap<>(PlayerColumn.class);

  @Inject
  public UnclaimedPlayerTable(final UnclaimedPlayerDataProvider presenter) {
    super(presenter);
    this.presenter = presenter;

    addStyleName(BASE_CSS.table());
    setPageSize(40);
    ((AbstractHeaderOrFooterBuilder<?>) getHeaderBuilder()).setSortIconStartOfLine(false);

    addColumn(new InjuryColumn());

    DroppableFunction onDrop = new DroppableFunction() {
      @Override
      public void f(DragAndDropContext dragAndDropContext) {
        handleDrop(dragAndDropContext);
      }
    };

    for (int i = 0; i < COLUMNS.length; i++) {
      PlayerColumn column = COLUMNS[i];
      PlayerColumn pitcherColumn = PITCHER_COLUMNS[i];
      Provider<EnumSet<Position>> positionFilterProvider = presenter.getPositionFilterProvider();

      PlayerTableColumn<?> playerTableColumn;
      if (pitcherColumn == null) {
        playerTableColumn = new NonStatPlayerTableColumn(
            column == MYRANK ? new EditTextCell() : new TextCell(),
            column,
            presenter,
            positionFilterProvider);
      } else {
        playerTableColumn = new StatPlayerTableColumn(
            new StatPlayerCell(positionFilterProvider),
            column,
            pitcherColumn,
            presenter,
            positionFilterProvider);
      }
      initDragging(playerTableColumn, onDrop);
      addColumn(playerTableColumn,
          new PlayerColumnHeader(column, pitcherColumn, positionFilterProvider));
      if (playerTableColumn.getHorizontalAlignment() == ALIGN_RIGHT) {
        getHeader(getColumnIndex(playerTableColumn)).setHeaderStyleNames(CSS.rightAlign());
      }
      playerColumns.put(column, playerTableColumn);

      if (column == NAME) {
        Column<Player, String> newsColumn = new NewsColumn();
        newsColumn.setFieldUpdater(new FieldUpdater<Player, String>() {
          @Override
          public void update(int index, Player player, String value) {
            presenter.showPlayerPopup(player);
          }
        });
        newsColumn.setCellStyleNames(CSS.newsCell());
        addColumn(newsColumn);
      }
    }

    addColumnSortHandler(new AsyncHandler(this) {
      @Override
      public void onColumnSort(ColumnSortEvent event) {
        ColumnSort sortedColumn = getSortedColumn();
        presenter.setSort(sortedColumn);
        super.onColumnSort(event);
        updateDropEnabled();
      }
    });

    addDragStartHandler(new DragStartEventHandler() {
      @Override
      public void onDragStart(DragStartEvent dragStartEvent) {
        Player player = dragStartEvent.getDraggableData();
        dragStartEvent.getHelper().setInnerSafeHtml(
            new SafeHtmlBuilder().appendEscaped(NAME.get(player))
                .toSafeHtml());
      }
    });
    updateDropEnabled();

    final SingleSelectionModel<Player> selectionModel = new SingleSelectionModel<>();
    setSelectionModel(selectionModel);
    getSelectionModel().addSelectionChangeHandler(new Handler() {
      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        Player player = selectionModel.getSelectedObject();
        presenter.select(player);
      }
    });
    setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);

    Window.addResizeHandler(new ResizeHandler() {
      @Override
      public void onResize(ResizeEvent event) {
        computePageSize();
      }
    });
  }

  private void handleDrop(DragAndDropContext dragAndDropContext) {
    Player draggedPlayer = dragAndDropContext.getDraggableData();
    Player droppedPlayer = dragAndDropContext.getDroppableData();
    if (draggedPlayer.getPlayerId() != droppedPlayer.getPlayerId()) {
      int prevRank = Integer.parseInt(MYRANK.get(draggedPlayer));
      int targetRank = Integer.parseInt(MYRANK.get(droppedPlayer));
      if (prevRank > targetRank && !isTopDrop(dragAndDropContext, false)) {
        targetRank++;
      }
      if (prevRank < targetRank && isTopDrop(dragAndDropContext, false)) {
        targetRank--;
      }
      presenter.changePlayerRank(draggedPlayer, targetRank, prevRank);
    }
  }

  @Override
  public void initColumnSort(TableSpec tableSpec) {
    ColumnSortList columnSortList = getColumnSortList();
    columnSortList.clear();
    columnSortList.push(new ColumnSortInfo(
        playerColumns.get(tableSpec.getSortCol()), tableSpec.isAscending()));
    refresh();
    updateDropEnabled();
  }

  void computePageSize() {
    TableRowElement rowElement = getRowElement(0);
    if (rowElement != null && queueAreaTopProvider != null) {
      int availableHeight = queueAreaTopProvider.get() - rowElement.getAbsoluteTop();
      int pageSize = availableHeight / rowElement.getOffsetHeight();
      if (pageSize != getPageSize()) {
        setPageSize(pageSize);
      }
    }
  }

  @Override
  public PlayerColumn getSortedPlayerColumn() {
    ColumnSortList columnSortList = getColumnSortList();
    if (columnSortList.size() > 0) {
      PlayerTableColumn<?> column = (PlayerTableColumn<?>) columnSortList.get(0).getColumn();
      return column.getSortedColumn();
    }
    return null;
  }

  public ColumnSort getSortedColumn() {
    ColumnSortList columnSortList = getColumnSortList();
    if (columnSortList.size() > 0) {
      ColumnSortInfo columnSortInfo = columnSortList.get(0);
      PlayerTableColumn<?> column = (PlayerTableColumn<?>) columnSortInfo.getColumn();
      return column.getSortedColumn(columnSortInfo.isAscending());
    }
    return null;
  }

  @Override
  public void positionFilterUpdated(boolean reSort) {
    for (PlayerTableColumn<?> playerTableColumn : playerColumns.values()) {
      playerTableColumn.updateDefaultSort();
    }
    if (reSort) {
      ColumnSortEvent.fire(this, getColumnSortList());
    }
    refresh();
  }

  @Override
  public void playerDataSetUpdated() {
    updateDropEnabled();
    refresh();
  }

  @SuppressWarnings("unchecked")
  private void updateDropEnabled() {
    boolean dropEnabled = getSortedPlayerColumn() == MYRANK;
    for (int i = 0; i < getColumnCount(); i++) {
      Column<Player, ?> column = getColumn(i);
      if (column instanceof DragAndDropColumn) {
        ((DragAndDropColumn<Player, ?>) column).getDroppableOptions().setDisabled(!dropEnabled);
      }
    }
  }

  public void setQueueAreaTopProvider(Provider<Integer> queueAreaTopProvider) {
    this.queueAreaTopProvider = queueAreaTopProvider;
  }
}