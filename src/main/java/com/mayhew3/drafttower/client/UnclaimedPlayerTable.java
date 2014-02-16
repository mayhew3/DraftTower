package com.mayhew3.drafttower.client;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.gwt.cell.client.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.google.gwt.user.cellview.client.*;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.client.Window;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.Range;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.mayhew3.drafttower.client.DraftTowerGinModule.QueueAreaTop;
import com.mayhew3.drafttower.client.events.ChangePlayerRankEvent;
import com.mayhew3.drafttower.client.events.LoginEvent;
import com.mayhew3.drafttower.client.events.PlayerSelectedEvent;
import com.mayhew3.drafttower.client.events.ShowPlayerPopupEvent;
import com.mayhew3.drafttower.shared.*;
import gwtquery.plugins.draggable.client.events.DragStartEvent;
import gwtquery.plugins.draggable.client.events.DragStartEvent.DragStartEventHandler;
import gwtquery.plugins.droppable.client.DroppableOptions.DroppableFunction;
import gwtquery.plugins.droppable.client.events.DragAndDropContext;
import gwtquery.plugins.droppable.client.gwt.DragAndDropColumn;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.google.gwt.user.client.ui.HasHorizontalAlignment.ALIGN_RIGHT;
import static com.mayhew3.drafttower.shared.PlayerColumn.*;

/**
 * Table widget for displaying player stats.
 */
public class UnclaimedPlayerTable extends PlayerTable<Player> implements
    LoginEvent.Handler {

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
  }

  private class PlayerColumnHeader extends Header<SafeHtml> {
    private final PlayerColumn column;
    private final PlayerColumn pitcherColumn;

    public PlayerColumnHeader(PlayerColumn column, PlayerColumn pitcherColumn) {
      super(new SafeHtmlCell());
      this.column = column;
      this.pitcherColumn = pitcherColumn;
    }

    @Override
    public SafeHtml getValue() {
      return new SafeHtmlBuilder()
                    .appendHtmlConstant("<span title=\"")
                    .appendEscaped(getLongName())
                    .appendHtmlConstant("\">")
                    .append(getShortName())
                    .appendHtmlConstant("</span>")
                    .toSafeHtml();
    }

    private SafeHtml getShortName() {
      if (pitcherColumn != null) {
        if (positionFilter == Position.P) {
          return new SafeHtmlBuilder()
              .appendEscaped(pitcherColumn.getShortName())
              .toSafeHtml();
        }
        if (positionFilter == Position.UNF || positionFilter == null) {
          return new SafeHtmlBuilder()
              .appendHtmlConstant("<span class=\"")
              .appendEscaped(CSS.splitHeader())
              .appendHtmlConstant("\">")
              .appendHtmlConstant("<span class=\"")
              .appendEscaped(CSS.batterStat())
              .appendHtmlConstant("\">")
              .appendEscaped(column.getShortName())
              .appendHtmlConstant("</span>/<span class=\"")
              .appendEscaped(CSS.pitcherStat())
              .appendHtmlConstant("\">")
              .appendEscaped(pitcherColumn.getShortName())
              .appendHtmlConstant("</span>")
              .appendHtmlConstant("</span>")
              .toSafeHtml();
        }
      }
      return new SafeHtmlBuilder()
          .appendEscaped(column.getShortName())
          .toSafeHtml();
    }

    private String getLongName() {
      if (pitcherColumn != null) {
        if (positionFilter == Position.P) {
          return pitcherColumn.getLongName();
        }
        if (positionFilter == Position.UNF || positionFilter == null) {
          return column.getLongName() + "/" + pitcherColumn.getLongName();
        }
      }
      return column.getLongName();
    }
  }

  private static class PlayerValue {
    private Player player;
    private String value;

    private PlayerValue(Player player, String value) {
      this.player = player;
      this.value = value;
    }
  }

  private abstract class PlayerTableColumn<C> extends DragAndDropColumn<Player, C> {
    protected final PlayerColumn column;

    public PlayerTableColumn(Cell<C> cell, PlayerColumn column) {
      super(cell);
      this.column = column;
      setSortable(true);

      if (column != NAME && column != MLB && column != ELIG) {
        setHorizontalAlignment(ALIGN_RIGHT);
      }

      DroppableFunction onDrop = new DroppableFunction() {
        @Override
        public void f(DragAndDropContext dragAndDropContext) {
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
            eventBus.fireEvent(new ChangePlayerRankEvent(
                draggedPlayer.getPlayerId(),
                targetRank,
                prevRank));
          }
        }
      };
      initDragging(this, onDrop);
    }

    public PlayerColumn getColumn() {
      return column;
    }

    public abstract PlayerColumn getSortedColumn();

    protected abstract void updateDefaultSort();
  }

  public class NonStatPlayerTableColumn extends PlayerTableColumn<String> {

    public NonStatPlayerTableColumn(PlayerColumn column) {
      super(createCell(column), column);

      setDefaultSortAscending(column.isDefaultSortAscending());

      if (column == MYRANK) {
        setFieldUpdater(new FieldUpdater<Player, String>() {
          @Override
          public void update(int index, Player player, String newRank) {
            String currentRank = MYRANK.get(player);
            if (!newRank.equals(currentRank)) {
              try {
                eventBus.fireEvent(new ChangePlayerRankEvent(player.getPlayerId(),
                    Integer.parseInt(newRank), Integer.parseInt(currentRank)));
              } catch (NumberFormatException e) {
                // whatevs
              }
            }
          }
        });
      }
    }

    @Override
    public String getValue(Player player) {
      if (column == WIZARD) {
        return PlayerColumn.getWizard(player, positionFilter, openPositions.get());
      } else {
        return column.get(player);
      }
    }

    @Override
    public PlayerColumn getSortedColumn() {
      return column;
    }

    @Override
    protected void updateDefaultSort() {
      // No-op.
    }
  }

  public class StatPlayerTableColumn extends PlayerTableColumn<PlayerValue> {

    private final PlayerColumn pitcherColumn;

    public StatPlayerTableColumn(PlayerColumn column, PlayerColumn pitcherColumn) {
      super(createStatCell(), column);
      this.pitcherColumn = pitcherColumn;
      updateDefaultSort();
    }

    @Override
    protected void updateDefaultSort() {
      setDefaultSortAscending(pitcherColumn.isDefaultSortAscending() && positionFilter == Position.P);
    }

    @Override
    public PlayerValue getValue(Player player) {
      if (pitcherColumn != null && pitcherColumn.get(player) != null) {
        return new PlayerValue(player, pitcherColumn.get(player));
      }
      return new PlayerValue(player, column.get(player));
    }

    @Override
    public PlayerColumn getSortedColumn() {
      return positionFilter == Position.P ? pitcherColumn : column;
    }
  }

  static {
    BASE_CSS.ensureInjected();
  }

  private static final PlayerColumn COLUMNS[] = {
      NAME, MLB, ELIG, G, AB, HR, RBI, OBP, SLG, RHR, SBCS, RANK, WIZARD, DRAFT, MYRANK
  };
  private static final PlayerColumn PITCHER_COLUMNS[] = {
      null, null, null, null, null, INN, K, ERA, WHIP, WL, S, null, null, null, null, null
  };

  public static final PlayerDataSet DEFAULT_DATA_SET = PlayerDataSet.CBSSPORTS;
  public static final PlayerColumn DEFAULT_SORT_COL = PlayerColumn.MYRANK;
  public static final boolean DEFAULT_SORT_ASCENDING = true;
  public static final Position DEFAULT_POSITION_FILTER = Position.UNF;

  private final Provider<Integer> queueAreaTopProvider;
  private final OpenPositions openPositions;

  private Position positionFilter;
  private final TableSpec tableSpec;
  private boolean hideInjuries;
  private String nameFilter;
  private final Map<PlayerColumn, PlayerTableColumn<?>> playerColumns = new EnumMap<>(PlayerColumn.class);

  @Inject
  public UnclaimedPlayerTable(AsyncDataProvider<Player> dataProvider,
      BeanFactory beanFactory,
      final EventBus eventBus,
      @QueueAreaTop Provider<Integer> queueAreaTopProvider,
      OpenPositions openPositions) {
    super(eventBus);
    this.queueAreaTopProvider = queueAreaTopProvider;
    this.openPositions = openPositions;

    tableSpec = beanFactory.createTableSpec().as();
    tableSpec.setPlayerDataSet(DEFAULT_DATA_SET);
    tableSpec.setSortCol(DEFAULT_SORT_COL);
    tableSpec.setAscending(DEFAULT_SORT_ASCENDING);

    addStyleName(BASE_CSS.table());
    setPageSize(40);
    ((AbstractHeaderOrFooterBuilder<?>) getHeaderBuilder()).setSortIconStartOfLine(false);

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

    for (int i = 0; i < COLUMNS.length; i++) {
      PlayerColumn column = COLUMNS[i];
      PlayerColumn pitcherColumn = PITCHER_COLUMNS[i];
      PlayerTableColumn<?> playerTableColumn = pitcherColumn == null
          ? new NonStatPlayerTableColumn(column)
          : new StatPlayerTableColumn(column, pitcherColumn);
      addColumn(playerTableColumn, new PlayerColumnHeader(column, pitcherColumn));
      if (playerTableColumn.getHorizontalAlignment() == ALIGN_RIGHT) {
        getHeader(getColumnIndex(playerTableColumn)).setHeaderStyleNames(CSS.rightAlign());
      }
      playerColumns.put(column, playerTableColumn);

      if (column == NAME) {
        Column<Player, String> newsColumn = new Column<Player, String>(new ClickableTextCell()) {
          @Override
          public String getValue(Player object) {
            return "?";
          }
        };
        newsColumn.setFieldUpdater(new FieldUpdater<Player, String>() {
          @Override
          public void update(int index, Player player, String value) {
            eventBus.fireEvent(new ShowPlayerPopupEvent(player));
          }
        });
        newsColumn.setCellStyleNames(CSS.newsCell());
        addColumn(newsColumn);
      }
    }

    dataProvider.addDataDisplay(this);
    addColumnSortHandler(new AsyncHandler(this) {
      @Override
      public void onColumnSort(ColumnSortEvent event) {
        tableSpec.setSortCol(getSortedColumn());
        tableSpec.setAscending(isSortedAscending());
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
                NAME.get(player)).toSafeHtml());
      }
    });
    updateDropEnabled();

    final SingleSelectionModel<Player> selectionModel = new SingleSelectionModel<>();
    setSelectionModel(selectionModel);
    getSelectionModel().addSelectionChangeHandler(new Handler() {
      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        Player player = selectionModel.getSelectedObject();
        eventBus.fireEvent(new PlayerSelectedEvent(player.getPlayerId(), NAME.get(player)));
      }
    });
    setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);

    eventBus.addHandler(LoginEvent.TYPE, this);

    Window.addResizeHandler(new ResizeHandler() {
      @Override
      public void onResize(ResizeEvent event) {
        computePageSize();
      }
    });
  }

  private AbstractCell<String> createCell(PlayerColumn column) {
    if (column == MYRANK) {
      return new EditTextCell();
    } else {
      return new TextCell();
    }
  }

  private AbstractCell<PlayerValue> createStatCell() {
    return new AbstractSafeHtmlCell<PlayerValue>(new AbstractSafeHtmlRenderer<PlayerValue>() {
      @Override
      public SafeHtml render(PlayerValue value) {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        if (value.value != null) {
          if (positionFilter == Position.UNF || positionFilter == null) {
            String style;
            if (ELIG.get(value.player).contains(Position.P.getShortName())) {
              style = CSS.pitcherStat();
            } else {
              style = CSS.batterStat();
            }
            builder.appendHtmlConstant("<span class=\"")
                .appendEscaped(style)
                .appendHtmlConstant("\">")
                .appendEscaped(value.value)
                .appendHtmlConstant("</span>");
          } else {
            builder.appendEscaped(value.value);
          }
        }
        return builder.toSafeHtml();
      }
    }) {
      @Override
      protected void render(Context context, SafeHtml value, SafeHtmlBuilder sb) {
        if (value != null) {
          sb.append(value);
        }
      }
    };
  }

  void computePageSize() {
    TableRowElement rowElement = getRowElement(0);
    if (rowElement != null) {
      int availableHeight = queueAreaTopProvider.get() - rowElement.getAbsoluteTop();
      int pageSize = availableHeight / rowElement.getOffsetHeight();
      if (pageSize != getPageSize()) {
        setPageSize(pageSize);
      }
    }
  }

  public PlayerColumn getSortedColumn() {
    ColumnSortList columnSortList = getColumnSortList();
    if (columnSortList.size() > 0) {
      PlayerTableColumn<?> column = (PlayerTableColumn<?>) columnSortList.get(0).getColumn();
      return column.getSortedColumn();
    }
    return null;
  }

  private boolean isSortedAscending() {
    ColumnSortList columnSortList = getColumnSortList();
    return columnSortList.size() > 0 && columnSortList.get(0).isAscending();
  }

  public Position getPositionFilter() {
    return positionFilter;
  }

  public boolean getHideInjuries() {
    return hideInjuries;
  }

  public void setPositionFilter(Position positionFilter) {
    boolean reSort = (this.positionFilter == Position.P) != (positionFilter == Position.P);
    this.positionFilter = positionFilter;
    for (PlayerTableColumn<?> playerTableColumn : playerColumns.values()) {
      playerTableColumn.updateDefaultSort();
    }
    if (reSort) {
      ColumnSortEvent.fire(this, getColumnSortList());
    }
    setVisibleRangeAndClearData(new Range(0, getPageSize()), true);
  }

  public void setPlayerDataSet(PlayerDataSet playerDataSet) {
    tableSpec.setPlayerDataSet(playerDataSet);
    updateDropEnabled();
    setVisibleRangeAndClearData(getVisibleRange(), true);
  }

  public void setNameFilter(String nameFilter) {
    this.nameFilter = nameFilter;
    setVisibleRangeAndClearData(new Range(0, getPageSize()), true);
  }

  @SuppressWarnings("unchecked")
  private void updateDropEnabled() {
    boolean dropEnabled = getSortedColumn() == MYRANK;
    for (int i = 0; i < getColumnCount(); i++) {
      Column<Player, ?> column = getColumn(i);
      if (column instanceof DragAndDropColumn) {
        ((DragAndDropColumn<Player, ?>) column).getDroppableOptions().setDisabled(!dropEnabled);
      }
    }
  }

  public void setHideInjuries(boolean hideInjuries) {
    this.hideInjuries = hideInjuries;
    setVisibleRangeAndClearData(getVisibleRange(), true);
  }

  public TableSpec getTableSpec() {
    return tableSpec;
  }

  public String getNameFilter() {
    return nameFilter;
  }

  @Override
  public void onLogin(LoginEvent event) {
    PlayerDataSet initialWizardTable = event.getLoginResponse().getInitialWizardTable();
    if (initialWizardTable != null) {
      tableSpec.setPlayerDataSet(initialWizardTable);
      tableSpec.setSortCol(PlayerColumn.WIZARD);
      tableSpec.setAscending(false);
    }

    ColumnSortList columnSortList = getColumnSortList();
    columnSortList.clear();
    columnSortList.push(new ColumnSortInfo(playerColumns.get(tableSpec.getSortCol()),
        tableSpec.isAscending()));
    setVisibleRangeAndClearData(getVisibleRange(), true);
    updateDropEnabled();
  }

  @Override
  protected boolean needsRefresh(List<DraftPick> oldPicks, List<DraftPick> newPicks) {
    for (int i = oldPicks.size(); i < newPicks.size(); i++) {
      final long pickedPlayerId = newPicks.get(i).getPlayerId();
      if (Iterables.any(getVisibleItems(), new Predicate<Player>() {
        @Override
        public boolean apply(Player player) {
          return player.getPlayerId() == pickedPlayerId;
        }
      })) {
        return true;
      }
    }
    return false;
  }
}