package com.mayhew3.drafttower.client.players.unclaimed;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.mayhew3.drafttower.client.OpenPositions;
import com.mayhew3.drafttower.client.TeamsInfo;
import com.mayhew3.drafttower.client.events.*;
import com.mayhew3.drafttower.client.players.PlayerDataProvider;
import com.mayhew3.drafttower.client.players.PlayerTableView;
import com.mayhew3.drafttower.client.serverrpc.ServerRpc;
import com.mayhew3.drafttower.shared.*;

import java.util.*;
import java.util.Map.Entry;

import static com.mayhew3.drafttower.shared.PlayerColumn.NAME;

/**
 * Data provider for player table.
 */
@Singleton
public class UnclaimedPlayerDataProvider extends PlayerDataProvider<Player> implements
    DraftStatusChangedEvent.Handler,
    LoginEvent.Handler,
    ChangePlayerRankEvent.Handler,
    SetAutoPickWizardEvent.Handler,
    CopyAllPlayerRanksEvent.Handler {

  private final class PlayerList {
    private final Map<SortSpec, List<Player>> playersBySortCol = new HashMap<>();
    private final Set<Long> pickedPlayers = new HashSet<>();

    private PlayerList(List<Player> players,
        PlayerColumn defaultSortCol,
        EnumSet<Position> defaultPositionFilter,
        boolean defaultSortAscending) {
      playersBySortCol.put(
          new SortSpec(defaultSortCol, defaultPositionFilter, defaultSortAscending), players);
    }

    private Iterable<Player> getPlayers(TableSpec tableSpec,
        int rowStart, int rowCount,
        final EnumSet<Position> positionFilter,
        final boolean hideInjuries,
        final String nameFilter) {
      // TODO(kprevas): refetch when nameFilter is set to get 0 AB/0 INN players?
      SortSpec sortSpec = new SortSpec(tableSpec.getSortCol(), positionFilter, tableSpec.isAscending());
      if (!playersBySortCol.containsKey(sortSpec)) {
        List<Player> players = playersBySortCol.values().iterator().next();
        Comparator<Player> comparator = sortSpec.getColumn() == PlayerColumn.WIZARD
            ? PlayerColumn.getWizardComparator(sortSpec.isAscending(), positionFilter)
            : sortSpec.getColumn().getComparator(sortSpec.isAscending());
        playersBySortCol.put(sortSpec,
            Ordering.from(comparator).sortedCopy(players));
      }
      return Iterables.limit(Iterables.skip(Iterables.filter(
          playersBySortCol.get(sortSpec),
          new Predicate<Player>() {
            @Override
            public boolean apply(Player player) {
              return (nameFilter == null
                      || PlayerColumn.NAME.get(player).toLowerCase()
                          .contains(nameFilter.toLowerCase()))
                  && (!hideInjuries || player.getInjury() == null)
                  && (positionFilter == null || Position.apply(player, positionFilter))
                  && !pickedPlayers.contains(player.getPlayerId());
            }
          }), rowStart), rowCount);
    }

    public int getTotalPlayers() {
      return playersBySortCol.values().iterator().next().size();
    }

    public void ensurePlayersRemoved(List<DraftPick> picks) {
      pickedPlayers.clear();
      for (DraftPick pick : picks) {
        pickedPlayers.add(pick.getPlayerId());
      }
    }

    public void updatePlayerRank(long playerId, int prevRank, int newRank) {
      List<Player> players = playersBySortCol.values().iterator().next();
      int lesserRank = prevRank + 1;
      int greaterRank = newRank;
      if (prevRank > newRank) {
        lesserRank = newRank;
        greaterRank = prevRank - 1;
      }
      // Update all players.
      for (Player player : players) {
        if (player.getPlayerId() == playerId) {
          player.setMyRank(Integer.toString(newRank));
        } else {
          int rank = Integer.parseInt(player.getMyRank());
          if (rank >= lesserRank && rank <= greaterRank) {
            if (prevRank > newRank) {
              player.setMyRank(Integer.toString(rank + 1));
            } else {
              player.setMyRank(Integer.toString(rank - 1));
            }
          }
        }
      }
      // Clear any cached sort orders sorted by rank.
      Set<SortSpec> keysToRemove = new HashSet<>();
      for (Entry<SortSpec, List<Player>> entry : playersBySortCol.entrySet()) {
        SortSpec sortSpec = entry.getKey();
        if (sortSpec.getColumn() == PlayerColumn.MYRANK) {
          keysToRemove.add(sortSpec);
        }
      }
      for (SortSpec sortSpec : keysToRemove) {
        playersBySortCol.remove(sortSpec);
      }
      // Ensure we don't hit the server again if the only sort order we had was by rank.
      if (playersBySortCol.isEmpty()) {
        playersBySortCol.put(new SortSpec(PlayerColumn.MYRANK, EnumSet.allOf(Position.class), true),
            Ordering.from(PlayerColumn.MYRANK.getComparator(true)).sortedCopy(players));
      }
    }
  }

  public static final PlayerDataSet DEFAULT_DATA_SET = PlayerDataSet.CBSSPORTS;
  public static final PlayerColumn DEFAULT_SORT_COL = PlayerColumn.MYRANK;
  public static final boolean DEFAULT_SORT_ASCENDING = true;

  private final BeanFactory beanFactory;
  private final ServerRpc serverRpc;
  private final TeamsInfo teamsInfo;
  private final OpenPositions openPositions;
  private final EventBus eventBus;

  private final TableSpec tableSpec;

  private final Map<PlayerDataSet, PlayerList> playersByDataSet = new HashMap<>();
  private final Map<PlayerDataSet, Runnable> requestCallbackByDataSet = new HashMap<>();
  private List<DraftPick> picks = new ArrayList<>();

  @Inject
  public UnclaimedPlayerDataProvider(BeanFactory beanFactory,
      ServerRpc serverRpc,
      TeamsInfo teamsInfo,
      EventBus eventBus,
      OpenPositions openPositions) {
    super(eventBus);

    this.beanFactory = beanFactory;
    this.serverRpc = serverRpc;
    this.teamsInfo = teamsInfo;
    this.openPositions = openPositions;
    this.eventBus = eventBus;

    tableSpec = beanFactory.createTableSpec().as();
    tableSpec.setPlayerDataSet(DEFAULT_DATA_SET);
    tableSpec.setSortCol(DEFAULT_SORT_COL);
    tableSpec.setAscending(DEFAULT_SORT_ASCENDING);

    eventBus.addHandler(ChangePlayerRankEvent.TYPE, this);
    eventBus.addHandler(SetAutoPickWizardEvent.TYPE, this);
    eventBus.addHandler(CopyAllPlayerRanksEvent.TYPE, this);
    eventBus.addHandler(DraftStatusChangedEvent.TYPE, this);
    eventBus.addHandler(LoginEvent.TYPE, this);
  }

  @Override
  public void setView(PlayerTableView<Player> view) {
    assert view instanceof UnclaimedPlayerTableView;
    super.setView(view);
  }

  @Override
  protected UnclaimedPlayerTableView getView() {
    return (UnclaimedPlayerTableView) super.getView();
  }

  @Override
  public void onLogin(LoginEvent event) {
    requestData(DEFAULT_DATA_SET,
        DEFAULT_SORT_COL,
        openPositions.get(),
        DEFAULT_SORT_ASCENDING,
        new Runnable() {
          @Override
          public void run() {
            // No-op.
          }
        });
    PlayerDataSet initialWizardTable = event.getLoginResponse().getInitialWizardTable();
    if (initialWizardTable != null) {
      tableSpec.setPlayerDataSet(initialWizardTable);
      tableSpec.setSortCol(PlayerColumn.WIZARD);
      tableSpec.setAscending(false);
    }

    getView().initColumnSort(tableSpec);
  }

  private void requestData(
      final PlayerDataSet dataSet,
      final PlayerColumn defaultSortCol,
      final EnumSet<Position> defaultPositionFilter,
      final boolean defaultSortAscending,
      Runnable callback) {
    if (!teamsInfo.isLoggedIn()) {
      return;
    }
    boolean requestInFlight = requestCallbackByDataSet.containsKey(dataSet);
    requestCallbackByDataSet.put(dataSet, callback);
    if (requestInFlight) {
      return;
    }
    AutoBean<UnclaimedPlayerListRequest> requestBean =
        beanFactory.createUnclaimedPlayerListRequest();
    UnclaimedPlayerListRequest request = requestBean.as();
    request.setTeamToken(teamsInfo.getTeamToken());

    TableSpec tableSpec = beanFactory.createTableSpec().as();
    tableSpec.setPlayerDataSet(dataSet);
    tableSpec.setSortCol(defaultSortCol);
    tableSpec.setAscending(defaultSortAscending);
    request.setTableSpec(tableSpec);

    serverRpc.sendPlayerListRequest(requestBean, new Function<UnclaimedPlayerListResponse, Void>() {
      @Override
      public Void apply(UnclaimedPlayerListResponse playerListResponse) {
        PlayerList playerList = new PlayerList(
            playerListResponse.getPlayers(),
            defaultSortCol,
            defaultPositionFilter,
            defaultSortAscending);
        playerList.ensurePlayersRemoved(picks);
        playersByDataSet.put(dataSet, playerList);
        Runnable callback = requestCallbackByDataSet.get(dataSet);
        if (callback != null) {
          callback.run();
        }
        requestCallbackByDataSet.remove(dataSet);
        return null;
      }
    });
  }

  @Override
  protected void rangeChanged(final HasData<Player> display) {
    final int rowStart = display.getVisibleRange().getStart();
    int rowCount = display.getVisibleRange().getLength();
    if (display instanceof UnclaimedPlayerTable) {
      UnclaimedPlayerTable table = (UnclaimedPlayerTable) display;
      EnumSet<Position> positionFilter = table.getPositionFilter();
      if (positionFilter.isEmpty()) {
        positionFilter = Position.REAL_POSITIONS;
      }
      boolean hideInjuries = table.getHideInjuries();
      String nameFilter = table.getNameFilter();
      if (!playersByDataSet.containsKey(tableSpec.getPlayerDataSet())) {
        requestData(tableSpec.getPlayerDataSet(),
            tableSpec.getSortCol(),
            positionFilter,
            tableSpec.isAscending(),
            new Runnable() {
              @Override
              public void run() {
                rangeChanged(display);
              }
            });
      } else {
        PlayerList playerList = playersByDataSet.get(tableSpec.getPlayerDataSet());
        Iterable<Player> players = playerList.getPlayers(
            tableSpec, rowStart, rowCount, positionFilter, hideInjuries, nameFilter);
        table.setRowData(rowStart, Lists.newArrayList(players));
        table.setRowCount(playerList.getTotalPlayers(), true);
        table.computePageSize();
      }
    }
  }

  @Override
  public void onDraftStatusChanged(DraftStatusChangedEvent event) {
    picks = event.getStatus().getPicks();
    for (PlayerList playerList : playersByDataSet.values()) {
      playerList.ensurePlayersRemoved(picks);
    }
  }

  @Override
  public void onChangePlayerRank(ChangePlayerRankEvent event) {
    if (!teamsInfo.isLoggedIn()) {
      return;
    }
    for (PlayerList playerList : playersByDataSet.values()) {
      playerList.updatePlayerRank(event.getPlayerId(), event.getPrevRank(), event.getNewRank());
    }
    AutoBean<ChangePlayerRankRequest> requestBean =
        beanFactory.createChangePlayerRankRequest();
    ChangePlayerRankRequest request = requestBean.as();
    request.setTeamToken(teamsInfo.getTeamToken());

    request.setPlayerId(event.getPlayerId());
    request.setNewRank(event.getNewRank());
    request.setPrevRank(event.getPrevRank());

    serverRpc.sendChangePlayerRankRequest(requestBean, new Runnable() {
      @Override
      public void run() {
        getView().refresh();
      }
    });
  }

  @Override
  public void onCopyAllPlayerRanks(CopyAllPlayerRanksEvent event) {
    if (!teamsInfo.isLoggedIn()) {
      return;
    }
    playersByDataSet.clear();
    AutoBean<CopyAllPlayerRanksRequest> requestBean =
        beanFactory.createCopyAllPlayerRanksRequest();
    CopyAllPlayerRanksRequest request = requestBean.as();
    request.setTeamToken(teamsInfo.getTeamToken());

    request.setTableSpec(event.getTableSpec());

    serverRpc.sendCopyRanksRequest(requestBean, new Runnable() {
      @Override
      public void run() {
        getView().refresh();
      }
    });
  }

  @Override
  public void onSetAutoPickWizard(SetAutoPickWizardEvent event) {
    if (!teamsInfo.isLoggedIn()) {
      return;
    }
    AutoBean<SetWizardTableRequest> requestBean =
        beanFactory.createSetAutoPickWizardRequest();
    SetWizardTableRequest request = requestBean.as();
    request.setTeamToken(teamsInfo.getTeamToken());

    request.setPlayerDataSet(event.getWizardTable());

    serverRpc.sendSetWizardTableRequest(requestBean, new Runnable() {
      @Override
      public void run() {
        getView().refresh();
      }
    });
  }

  @Override
  protected Predicate<Player> createPredicate(long playerId) {
    return new PlayerPredicate(playerId);
  }

  public void showPlayerPopup(Player player) {
    eventBus.fireEvent(new ShowPlayerPopupEvent(player));
  }

  public void select(Player player) {
    eventBus.fireEvent(new PlayerSelectedEvent(player.getPlayerId(), NAME.get(player)));
  }

  public void changePlayerRank(Player player, int targetRank, int prevRank) {
    eventBus.fireEvent(new ChangePlayerRankEvent(
        player.getPlayerId(),
        targetRank,
        prevRank));
  }

  public void setSort(ColumnSort sort) {
    tableSpec.setSortCol(sort.getColumn());
    tableSpec.setAscending(sort.isAscending());
  }

  public void setPlayerDataSet(PlayerDataSet playerDataSet) {
    tableSpec.setPlayerDataSet(playerDataSet);
  }

  public TableSpec getTableSpec() {
    return tableSpec;
  }
}