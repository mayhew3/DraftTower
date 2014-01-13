package com.mayhew3.drafttower.client;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.gwt.cell.client.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.client.Window;
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
    }

    @Source("UnclaimedPlayerTable.css")
    Css css();
  }

  protected static final Resources.Css CSS = ((Resources) GWT.create(Resources.class)).css();
  static {
    CSS.ensureInjected();
  }

  public class PlayerTableColumn extends DragAndDropColumn<Player, String> {

    private final PlayerColumn column;

    public PlayerTableColumn(PlayerColumn column) {
      super(createCell(column));
      this.column = column;
      setSortable(true);
      setDefaultSortAscending(column == ERA || column == WHIP || column == NAME || column == RANK || column == MYRANK || column == DRAFT);

      if (column != NAME && column != MLB && column != ELIG) {
        setHorizontalAlignment(ALIGN_RIGHT);
      }

      if (column == MYRANK) {
        setFieldUpdater(new FieldUpdater<Player, String>() {
          @Override
          public void update(int index, Player player, String newRank) {
            String currentRank = player.getColumnValues().get(MYRANK);
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

      DroppableFunction onDrop = new DroppableFunction() {
        @Override
        public void f(DragAndDropContext dragAndDropContext) {
          Player draggedPlayer = dragAndDropContext.getDraggableData();
          Player droppedPlayer = dragAndDropContext.getDroppableData();
          if (draggedPlayer.getPlayerId() != droppedPlayer.getPlayerId()) {
            int prevRank = Integer.parseInt(draggedPlayer.getColumnValues().get(MYRANK));
            int targetRank = Integer.parseInt(droppedPlayer.getColumnValues().get(MYRANK));
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

  private static final PlayerColumn COLUMNS[] = {
      NAME, MLB, ELIG, G, AB, HR, RBI, OBP, SLG, RHR, SBCS, INN, K, ERA, WHIP, WL, S, RANK, WIZARD, DRAFT, MYRANK
  };

  private final Provider<Integer> queueAreaTopProvider;

  private Position positionFilter;
  private final TableSpec tableSpec;
  private boolean hideInjuries;
  private String nameFilter;
  private final Map<PlayerColumn, PlayerTableColumn> playerColumns = new EnumMap<>(PlayerColumn.class);

  @Inject
  public UnclaimedPlayerTable(UnclaimedPlayerDataProvider dataProvider,
      BeanFactory beanFactory,
      final EventBus eventBus,
      @QueueAreaTop Provider<Integer> queueAreaTopProvider) {
    super(eventBus);
    this.queueAreaTopProvider = queueAreaTopProvider;

    tableSpec = beanFactory.createTableSpec().as();
    tableSpec.setPlayerDataSet(PlayerDataSet.CBSSPORTS);
    tableSpec.setSortCol(PlayerColumn.MYRANK);
    tableSpec.setAscending(true);

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
      PlayerTableColumn playerTableColumn = new PlayerTableColumn(column);
      addColumn(playerTableColumn,
          new SafeHtmlBuilder()
              .appendHtmlConstant("<span title=\"")
              .appendEscaped(column.getLongName())
              .appendHtmlConstant("\">")
              .appendEscaped(column.getShortName())
              .appendHtmlConstant("</span>")
              .toSafeHtml());
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
                player.getColumnValues().get(NAME)).toSafeHtml());
      }
    });
    updateDropEnabled();

    final SingleSelectionModel<Player> selectionModel = new SingleSelectionModel<>();
    setSelectionModel(selectionModel);
    getSelectionModel().addSelectionChangeHandler(new Handler() {
      @Override
      public void onSelectionChange(SelectionChangeEvent event) {
        Player player = selectionModel.getSelectedObject();
        eventBus.fireEvent(new PlayerSelectedEvent(player.getPlayerId(), player.getColumnValues().get(NAME)));
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

  void computePageSize() {
    int availableHeight = queueAreaTopProvider.get() - getRowElement(0).getAbsoluteTop();
    int pageSize = availableHeight / getRowElement(0).getOffsetHeight();
    if (pageSize != getPageSize()) {
      setPageSize(pageSize);
    }
  }

  public PlayerColumn getSortedColumn() {
    ColumnSortList columnSortList = getColumnSortList();
    if (columnSortList.size() > 0) {
      return ((PlayerTableColumn) columnSortList.get(0).getColumn()).getColumn();
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
    this.positionFilter = positionFilter;
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