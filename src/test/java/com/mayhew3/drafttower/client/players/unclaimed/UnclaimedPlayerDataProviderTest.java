package com.mayhew3.drafttower.client.players.unclaimed;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.view.client.Range;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.client.OpenPositions;
import com.mayhew3.drafttower.client.TeamsInfo;
import com.mayhew3.drafttower.client.events.*;
import com.mayhew3.drafttower.client.players.PositionFilter;
import com.mayhew3.drafttower.client.players.SinglePositionFilter;
import com.mayhew3.drafttower.client.players.UnfilledPositionsFilter;
import com.mayhew3.drafttower.client.serverrpc.ServerRpc;
import com.mayhew3.drafttower.shared.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import java.util.EnumSet;
import java.util.List;

/**
 * Test for {@link UnclaimedPlayerDataProvider}.
 */
public class UnclaimedPlayerDataProviderTest {

  private BeanFactory beanFactory;
  private TestPlayerGenerator playerGenerator;
  private ServerRpc serverRpc;
  private TeamsInfo teamsInfo;
  private TableSpec tableSpec;
  private UnclaimedPlayerDataProvider provider;
  private UnclaimedPlayerTableView view;

  @Captor private ArgumentCaptor<AutoBean<UnclaimedPlayerListRequest>> getPlayerListRequestCaptor;
  @Captor private ArgumentCaptor<AutoBean<ChangePlayerRankRequest>> changePlayerRankRequestCaptor;
  @Captor private ArgumentCaptor<AutoBean<AddOrRemoveFavoriteRequest>> favoriteRequestCaptor;
  @Captor private ArgumentCaptor<AutoBean<CopyAllPlayerRanksRequest>> copyAllPlayerRanksRequestCaptor;
  @Captor private ArgumentCaptor<AutoBean<SetWizardTableRequest>> setWizardTableRequestCaptor;
  @Captor private ArgumentCaptor<List<Player>> playerListCaptor;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    OpenPositions openPositions = Mockito.mock(OpenPositions.class);
    EnumSet<Position> unfilledPositions = EnumSet.of(Position.C, Position.SB, Position.SS, Position.P);
    Mockito.when(openPositions.get()).thenReturn(unfilledPositions);
    beanFactory = AutoBeanFactorySource.create(BeanFactory.class);
    playerGenerator = new TestPlayerGenerator(beanFactory);
    serverRpc = Mockito.mock(ServerRpc.class);
    teamsInfo = Mockito.mock(TeamsInfo.class);
    Mockito.when(teamsInfo.isLoggedIn()).thenReturn(true);
    Mockito.when(teamsInfo.getTeamToken()).thenReturn("1");
    EventBus eventBus = Mockito.mock(EventBus.class);
    tableSpec = beanFactory.createTableSpec().as();
    tableSpec.setPlayerDataSet(PlayerDataSet.CBSSPORTS);
    tableSpec.setSortCol(PlayerColumn.WIZARD);

    provider = new UnclaimedPlayerDataProvider(beanFactory,
        serverRpc,
        teamsInfo,
        eventBus);
    view = Mockito.mock(UnclaimedPlayerTableView.class);
    Mockito.when(view.getVisibleRange()).thenReturn(new Range(0, 40));
    provider.setView(view);
    Mockito.reset(serverRpc, view);
    Mockito.when(view.getVisibleRange()).thenReturn(new Range(0, 40));

    provider.resetInFlightRequestsForTesting();
  }

  @Test
  public void testRefreshOnDraftStatusChangeFirstStatus() {
    DraftPick draftPick =
        DraftStatusTestUtil.createDraftPick(1, "", false, "P", 1, beanFactory);
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(
        Lists.newArrayList(draftPick), beanFactory);
    provider.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(draftStatus, beanFactory)));
    Mockito.verify(view).refresh();
  }

  @Test
  public void testNoRefreshOnDraftStatusChangeNoNewPicks() {
    DraftPick draftPick =
        DraftStatusTestUtil.createDraftPick(1, "", false, "P", 1, beanFactory);
    provider.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(Lists.newArrayList(draftPick), beanFactory)));
    Mockito.reset(view);
    provider.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(Lists.newArrayList(draftPick), beanFactory)));
    Mockito.verifyZeroInteractions(view);
  }

  @Test
  public void testNoRefreshOnDraftStatusChangePickBackedOut() {
    DraftPick draftPick =
        DraftStatusTestUtil.createDraftPick(1, "", false, "P", 1, beanFactory);
    provider.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(Lists.newArrayList(draftPick), beanFactory)));
    Mockito.reset(view);
    provider.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(Lists.<DraftPick>newArrayList(), beanFactory)));
    Mockito.verify(view).refresh();
  }

  @Test
  public void testRefreshOnDraftStatusChangeVisiblePlayerPicked() {
    provider.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(Lists.<DraftPick>newArrayList(), beanFactory)));
    Mockito.reset(view);
    Player player = beanFactory.createPlayer().as();
    player.setPlayerId(1);
    Mockito.when(view.getVisibleItems()).thenReturn(Lists.newArrayList(player));
    DraftPick draftPick =
        DraftStatusTestUtil.createDraftPick(1, "", false, "P", 1, beanFactory);
    provider.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(Lists.newArrayList(draftPick), beanFactory)));
    Mockito.verify(view).refresh();
  }

  @Test
  public void testNoRefreshOnDraftStatusChangeNoVisiblePlayerPicked() {
    provider.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(Lists.<DraftPick>newArrayList(), beanFactory)));
    Mockito.reset(view);
    Player player = beanFactory.createPlayer().as();
    player.setPlayerId(2);
    Mockito.when(view.getVisibleItems()).thenReturn(Lists.newArrayList(player));
    DraftPick draftPick =
        DraftStatusTestUtil.createDraftPick(1, "", false, "P", 1, beanFactory);
    provider.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(Lists.newArrayList(draftPick), beanFactory)));
    Mockito.verify(view, Mockito.never()).refresh();
  }

  @Test
  public void testInitialWizardTableSetOnLogin() {
    LoginResponse loginResponse = beanFactory.createLoginResponse().as();
    loginResponse.setInitialWizardTable(PlayerDataSet.ROTOWIRE);
    provider.onLogin(new LoginEvent(loginResponse));
    ArgumentCaptor<TableSpec> tableSpecCaptor = ArgumentCaptor.forClass(TableSpec.class);
    Mockito.verify(view).initColumnSort(tableSpecCaptor.capture());
    TableSpec argValue = tableSpecCaptor.getValue();
    Assert.assertEquals(PlayerDataSet.ROTOWIRE, argValue.getPlayerDataSet());
    Assert.assertEquals(PlayerColumn.WIZARD, argValue.getSortCol());
    Assert.assertEquals(false, argValue.isAscending());
  }

  @Test
  public void testNoTableSpecSetOnLoginWithoutInitialWizardTable() {
    LoginResponse loginResponse = beanFactory.createLoginResponse().as();
    provider.onLogin(new LoginEvent(loginResponse));
    ArgumentCaptor<TableSpec> tableSpecCaptor = ArgumentCaptor.forClass(TableSpec.class);
    Mockito.verify(view).initColumnSort(tableSpecCaptor.capture());
    TableSpec argValue = tableSpecCaptor.getValue();
    Assert.assertEquals(UnclaimedPlayerDataProvider.DEFAULT_DATA_SET,
        argValue.getPlayerDataSet());
    Assert.assertEquals(UnclaimedPlayerDataProvider.DEFAULT_SORT_COL,
        argValue.getSortCol());
    Assert.assertEquals(UnclaimedPlayerDataProvider.DEFAULT_SORT_ASCENDING,
        argValue.isAscending());
  }

  @Test
  public void testRequestData() {
    provider.requestData(tableSpec, Mockito.mock(Runnable.class));
    Mockito.verify(serverRpc).sendPlayerListRequest(getPlayerListRequestCaptor.capture(),
        Mockito.<Function<UnclaimedPlayerListResponse, Void>>any());
    UnclaimedPlayerListRequest request = getPlayerListRequestCaptor.getValue().as();
    Assert.assertEquals("1", request.getTeamToken());
    Assert.assertEquals(tableSpec, request.getTableSpec());
  }

  @Test
  public void testRequestDataNotLoggedIn() {
    Mockito.when(teamsInfo.isLoggedIn()).thenReturn(false);
    provider.requestData(tableSpec, Mockito.mock(Runnable.class));
    Mockito.verifyZeroInteractions(serverRpc);
  }

  @Test
  public void testRequestDataRequestInFlight() {
    provider.requestData(tableSpec, Mockito.mock(Runnable.class));
    provider.requestData(tableSpec, Mockito.mock(Runnable.class));
    Mockito.verify(serverRpc, Mockito.times(1)).sendPlayerListRequest(
        Mockito.<AutoBean<UnclaimedPlayerListRequest>>any(),
        Mockito.<Function<UnclaimedPlayerListResponse, Void>>any());
  }

  @Test
  public void testHandlePlayerListResponse() {
    provider.handlePlayerListResponse(createPlayerListResponse(), tableSpec);
    Assert.assertEquals(3, provider.playersByDataSet.get(tableSpec.getPlayerDataSet()).getTotalPlayers());
  }

  @Test
  public void testHandlePlayerListResponseCallsCallback() {
    Runnable requestCallback = Mockito.mock(Runnable.class);
    provider.requestData(tableSpec, requestCallback);
    provider.handlePlayerListResponse(createPlayerListResponse(), tableSpec);
    Mockito.verify(requestCallback).run();
  }

  @Test
  public void testHandlePlayerListResponsePickedPlayers() {
    provider.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(Lists.newArrayList(
            DraftStatusTestUtil.createDraftPick(1, "", false, "C", 1, beanFactory)), beanFactory)));
    provider.handlePlayerListResponse(createPlayerListResponse(), tableSpec);
    Assert.assertEquals(2, provider.playersByDataSet.get(tableSpec.getPlayerDataSet()).getTotalPlayers());
  }

  @Test
  public void testRangeChanged() {
    UnclaimedPlayerTableView display = Mockito.mock(UnclaimedPlayerTableView.class);
    Mockito.when(display.getVisibleRange()).thenReturn(new Range(1, 40));
    provider.rangeChanged(display);
    Mockito.verify(serverRpc).sendPlayerListRequest(getPlayerListRequestCaptor.capture(),
        Mockito.<Function<UnclaimedPlayerListResponse, Void>>any());
    UnclaimedPlayerListRequest request = getPlayerListRequestCaptor.getValue().as();
    Assert.assertEquals("1", request.getTeamToken());
    Assert.assertEquals(UnclaimedPlayerDataProvider.DEFAULT_DATA_SET,
        request.getTableSpec().getPlayerDataSet());
    Assert.assertEquals(UnclaimedPlayerDataProvider.DEFAULT_SORT_COL,
        request.getTableSpec().getSortCol());
    Assert.assertEquals(UnclaimedPlayerDataProvider.DEFAULT_SORT_ASCENDING,
        request.getTableSpec().isAscending());
    provider.handlePlayerListResponse(
        createPlayerListResponse(), provider.getTableSpec());
    Mockito.verify(display).setRowData(Mockito.eq(1), playerListCaptor.capture());
    List<Player> playerList = playerListCaptor.getValue();
    Assert.assertEquals(2, playerList.size());
    Assert.assertEquals(2, playerList.get(0).getPlayerId());
    Assert.assertEquals(3, playerList.get(1).getPlayerId());
    Mockito.verify(display).setRowCount(3, true);
    Mockito.verify(display).computePageSize();
    provider.rangeChanged(display);
    Mockito.verifyNoMoreInteractions(serverRpc);
  }

  @Test
  public void testRangeChangedEmptyPositionFilter() {
    OpenPositions openPositions = Mockito.mock(OpenPositions.class);
    Mockito.when(openPositions.get()).thenReturn(EnumSet.noneOf(Position.class));
    provider.setPositionFilter(new UnfilledPositionsFilter(openPositions), EnumSet.noneOf(Position.class));
    provider.rangeChanged(provider.getView());
    PositionFilter positionFilter = provider.getPositionFilterProvider().get();
    for (Position position : Position.REAL_POSITIONS) {
      Player player = Mockito.mock(Player.class);
      Mockito.when(player.getEligibility()).thenReturn(position.getShortName());
      Assert.assertTrue(positionFilter.apply(player, EnumSet.noneOf(Position.class)));
    }
  }

  @Test
  public void testOnDraftStatusChangedPickedPlayers() {
    provider.handlePlayerListResponse(createPlayerListResponse(), tableSpec);
    provider.onDraftStatusChanged(new DraftStatusChangedEvent(
        DraftStatusTestUtil.createClientDraftStatus(Lists.newArrayList(
            DraftStatusTestUtil.createDraftPick(1, "", false, "C", 1, beanFactory)), beanFactory)));
    Assert.assertEquals(2, provider.playersByDataSet.get(tableSpec.getPlayerDataSet()).getTotalPlayers());
  }

  @Test
  public void testOnChangePlayerRank() {
    PlayerList playerList = Mockito.mock(PlayerList.class);
    provider.playersByDataSet.put(UnclaimedPlayerDataProvider.DEFAULT_DATA_SET, playerList);
    provider.onChangePlayerRank(new ChangePlayerRankEvent(1, 2, 1));
    Mockito.verify(playerList).updatePlayerRank(1, 1, 2);
    Mockito.verify(serverRpc).sendChangePlayerRankRequest(
        changePlayerRankRequestCaptor.capture(), Mockito.any(Runnable.class));
    ChangePlayerRankRequest request = changePlayerRankRequestCaptor.getValue().as();
    Assert.assertEquals("1", request.getTeamToken());
    Assert.assertEquals(1, request.getPlayerId());
    Assert.assertEquals(1, request.getPrevRank());
    Assert.assertEquals(2, request.getNewRank());
  }

  @Test
  public void testOnChangePlayerRankNotLoggedIn() {
    Mockito.when(teamsInfo.isLoggedIn()).thenReturn(false);
    provider.onChangePlayerRank(new ChangePlayerRankEvent(1, 1, 2));
    Mockito.verifyZeroInteractions(serverRpc);
  }

  @Test
  public void testOnToggleFavoritePlayer() {
    PlayerList playerList = Mockito.mock(PlayerList.class);
    provider.playersByDataSet.put(UnclaimedPlayerDataProvider.DEFAULT_DATA_SET, playerList);
    provider.onToggleFavoritePlayer(new ToggleFavoritePlayerEvent(1, true));
    Mockito.verify(playerList).updateFavoritePlayer(1, true);
    Mockito.verify(serverRpc).sendAddOrRemoveFavoriteRequest(
        favoriteRequestCaptor.capture(), Mockito.any(Runnable.class));
    AddOrRemoveFavoriteRequest request = favoriteRequestCaptor.getValue().as();
    Assert.assertEquals("1", request.getTeamToken());
    Assert.assertEquals(1, request.getPlayerId());
    Assert.assertTrue(request.isAdd());
  }

  @Test
  public void testOnToggleFavoritePlayerNotLoggedIn() {
    Mockito.when(teamsInfo.isLoggedIn()).thenReturn(false);
    provider.onToggleFavoritePlayer(new ToggleFavoritePlayerEvent(1, true));
    Mockito.verifyZeroInteractions(serverRpc);
  }

  @Test
  public void testOnCopyAllPlayerRanks() {
    PlayerList playerList = Mockito.mock(PlayerList.class);
    provider.playersByDataSet.put(UnclaimedPlayerDataProvider.DEFAULT_DATA_SET, playerList);
    provider.onCopyAllPlayerRanks(new CopyAllPlayerRanksEvent(tableSpec));
    Assert.assertTrue(provider.playersByDataSet.isEmpty());
    Mockito.verify(serverRpc).sendCopyRanksRequest(
        copyAllPlayerRanksRequestCaptor.capture(), Mockito.any(Runnable.class));
    CopyAllPlayerRanksRequest request = copyAllPlayerRanksRequestCaptor.getValue().as();
    Assert.assertEquals("1", request.getTeamToken());
    Assert.assertEquals(tableSpec, request.getTableSpec());
  }

  @Test
  public void testOnCopyAllPlayerRanksNotLoggedIn() {
    Mockito.when(teamsInfo.isLoggedIn()).thenReturn(false);
    provider.onCopyAllPlayerRanks(new CopyAllPlayerRanksEvent(tableSpec));
    Mockito.verifyZeroInteractions(serverRpc);
  }

  @Test
  public void testOnSetAutoPickWizard() {
    provider.onSetAutoPickWizard(new SetAutoPickWizardEvent(PlayerDataSet.GURU));
    Mockito.verify(serverRpc).sendSetWizardTableRequest(
        setWizardTableRequestCaptor.capture(), Mockito.any(Runnable.class));
    SetWizardTableRequest request = setWizardTableRequestCaptor.getValue().as();
    Assert.assertEquals("1", request.getTeamToken());
    Assert.assertEquals(PlayerDataSet.GURU, request.getPlayerDataSet());
  }

  @Test
  public void testOnSetAutoPickWizardNotLoggedIn() {
    Mockito.when(teamsInfo.isLoggedIn()).thenReturn(false);
    provider.onSetAutoPickWizard(new SetAutoPickWizardEvent(PlayerDataSet.GURU));
    Mockito.verifyZeroInteractions(serverRpc);
  }

  @Test
  public void testSetSort() {
    provider.setSort(new ColumnSort(PlayerColumn.AB, true));
    Assert.assertEquals(PlayerColumn.AB, provider.getTableSpec().getSortCol());
    Assert.assertTrue(provider.getTableSpec().isAscending());
  }

  @Test
  public void testSetPlayerDataSet() {
    provider.setPlayerDataSet(PlayerDataSet.AVERAGES);
    Assert.assertEquals(PlayerDataSet.AVERAGES, provider.getTableSpec().getPlayerDataSet());
    Mockito.verify(view).playerDataSetUpdated();
  }

  @Test
  public void testSetPositionFilter() {
    InOrder inOrder = Mockito.inOrder(view);
    SinglePositionFilter pitchers = new SinglePositionFilter(Position.P);
    provider.setPositionFilter(pitchers, EnumSet.noneOf(Position.class));
    Assert.assertEquals(pitchers, provider.getPositionFilterProvider().get());
    SinglePositionFilter firstBasemen = new SinglePositionFilter(Position.FB);
    provider.setPositionFilter(firstBasemen, EnumSet.noneOf(Position.class));
    Assert.assertEquals(firstBasemen, provider.getPositionFilterProvider().get());
    OpenPositions openPositions = Mockito.mock(OpenPositions.class);
    Mockito.when(openPositions.get()).thenReturn(EnumSet.of(Position.SB, Position.SS));
    UnfilledPositionsFilter middleInfielders = new UnfilledPositionsFilter(openPositions);
    provider.setPositionFilter(middleInfielders, EnumSet.noneOf(Position.class));
    Assert.assertEquals(middleInfielders, provider.getPositionFilterProvider().get());
    inOrder.verify(view, Mockito.calls(2)).positionFilterUpdated(true);
    inOrder.verify(view).positionFilterUpdated(false);
  }

  private UnclaimedPlayerListResponse createPlayerListResponse() {
    UnclaimedPlayerListResponse playerListResponse = beanFactory.createUnclaimedPlayerListResponse().as();
    List<Player> players = Lists.newArrayList(
        playerGenerator.generatePlayer(1, Position.C, 1),
        playerGenerator.generatePlayer(2, Position.C, 2),
        playerGenerator.generatePlayer(3, Position.C, 3)
    );
    playerListResponse.setPlayers(players);
    return playerListResponse;
  }
}