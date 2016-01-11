package com.mayhew3.drafttower.client.players.unclaimed;

import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.Import;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.*;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.client.Window;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.mayhew3.drafttower.client.players.PlayerDragController;
import com.mayhew3.drafttower.client.players.PlayerTable;
import com.mayhew3.drafttower.client.players.PositionFilter;
import com.mayhew3.drafttower.shared.*;

import java.util.EnumMap;
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
      String favoriteColumn();
      String favorite();
      String newsCell();
      String rightAlign();
      String batterStat();
      String pitcherStat();
      String splitHeader();
    }

    @Source("UnclaimedPlayerTable.css")
    @Import(CellTable.Style.class)
    Css css();
  }

  protected static final Resources.Css CSS = ((Resources) GWT.create(Resources.class)).css();
  static {
    CSS.ensureInjected();
    BASE_CSS.ensureInjected();
  }

  @SuppressWarnings("ConstantConditions")
  private static final PlayerColumn COLUMNS[] = Scoring.CATEGORIES ?
  new PlayerColumn[] {
      NAME, MLB, ELIG, AB, HR, RBI, OBP, SLG, RHR, SBCS, RANK, WIZARD, DRAFT, MYRANK
  } : new PlayerColumn[] {
      NAME, MLB, ELIG, G, AB, BA, OBP, SLG, H, HR, RBI, R, BB, KO, SB, PTS, RANK, WIZARD, DRAFT, MYRANK
  };
  @SuppressWarnings("ConstantConditions")
  private static final PlayerColumn PITCHER_COLUMNS[] = Scoring.CATEGORIES ?
  new PlayerColumn[] {
      null, null, null, G, INN, K, ERA, WHIP, WL, S, null, null, null, null, null, null
  } : new PlayerColumn[] {
      null, null, null, null, GS, INN, WHIP, ERA, HA, HRA, W, L, BBI, K, S, null, null, null, null, null
  };

  private final UnclaimedPlayerDataProvider presenter;

  private Provider<Integer> queueAreaTopProvider;
  private Integer cachedRowHeight;
  private Integer cachedTop;

  private final Map<PlayerColumn, PlayerTableColumn<?>> playerColumns = new EnumMap<>(PlayerColumn.class);

  @Inject
  public UnclaimedPlayerTable(final UnclaimedPlayerDataProvider presenter,
      PlayerDragController playerDragController) {
    super(presenter, playerDragController);
    this.presenter = presenter;

    addStyleName(BASE_CSS.table());
    setPageSize(40);
    ((AbstractHeaderOrFooterBuilder<?>) getHeaderBuilder()).setSortIconStartOfLine(false);

    addColumn(new InjuryColumn());

    FavoriteColumn favoriteColumn = new FavoriteColumn();
    favoriteColumn.setFieldUpdater(new FieldUpdater<Player, Boolean>() {
      @Override
      public void update(int index, Player player, Boolean value) {
        presenter.toggleFavoritePlayer(player);
        redrawRow(index);
      }
    });
    addColumn(favoriteColumn);

    for (int i = 0; i < COLUMNS.length; i++) {
      PlayerColumn column = COLUMNS[i];
      PlayerColumn pitcherColumn = PITCHER_COLUMNS[i];
      Provider<PositionFilter> positionFilterProvider = presenter.getPositionFilterProvider();

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
      }
    });

    final SingleSelectionModel<Player> selectionModel = new SingleSelectionModel<>();
    setSelectionModel(selectionModel,
        DefaultSelectionEventManager.<Player>createBlacklistManager(getColumnIndex(favoriteColumn)));
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

  @Override
  public void onDrop(DraggableItem item, MouseUpEvent event) {
    if (item instanceof Player) {
      Player draggedPlayer = (Player) item;
      int relativeY = event.getRelativeY(getElement());
      int rowIndex = getRowIndex(relativeY);
      Player droppedPlayer = getVisibleItem(rowIndex);
      if (draggedPlayer.getPlayerId() != droppedPlayer.getPlayerId()) {
        int prevRank = Integer.parseInt(MYRANK.get(draggedPlayer));
        int targetRank = Integer.parseInt(MYRANK.get(droppedPlayer));
        if (prevRank > targetRank && !isTopDrop(relativeY)) {
          targetRank++;
        }
        if (prevRank < targetRank && isTopDrop(relativeY)) {
          targetRank--;
        }
        presenter.changePlayerRank(draggedPlayer, targetRank, prevRank);
      }
    }
  }

  @Override
  public void initColumnSort(TableSpec tableSpec) {
    ColumnSortList columnSortList = getColumnSortList();
    columnSortList.clear();
    columnSortList.push(new ColumnSortInfo(
        playerColumns.get(tableSpec.getSortCol()), tableSpec.isAscending()));
    refresh();
  }

  @Override
  public void computePageSize() {
    if (cachedRowHeight == null) {
      TableRowElement rowElement = getRowElement(0);
      if (rowElement != null && queueAreaTopProvider != null) {
        cachedRowHeight = rowElement.getOffsetHeight();
        cachedTop = rowElement.getAbsoluteTop();
      } else {
        return;
      }
    }
    int pageSize;
    if (cachedRowHeight == 0) {
      // e.g. tests
      pageSize = 40;
    } else {
      int availableHeight = queueAreaTopProvider.get() - cachedTop;
      pageSize = availableHeight / cachedRowHeight;
    }
    if (pageSize != getPageSize()) {
      setPageSize(pageSize);
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
    refresh();
  }

  @Override
  protected SafeHtml getDragHelperContents(Player draggedItem) {
    return new SafeHtmlBuilder().appendEscaped(NAME.get(draggedItem)).toSafeHtml();
  }

  @Override
  public boolean isDropEnabled() {
    return getSortedPlayerColumn() == MYRANK;
  }

  public void setQueueAreaTopProvider(Provider<Integer> queueAreaTopProvider) {
    this.queueAreaTopProvider = queueAreaTopProvider;
  }
}