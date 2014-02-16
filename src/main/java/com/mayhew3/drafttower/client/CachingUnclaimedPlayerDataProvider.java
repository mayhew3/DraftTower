package com.mayhew3.drafttower.client;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.Response;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.mayhew3.drafttower.client.DraftTowerGinModule.ChangePlayerRankUrl;
import com.mayhew3.drafttower.client.DraftTowerGinModule.CopyPlayerRanksUrl;
import com.mayhew3.drafttower.client.DraftTowerGinModule.SetAutoPickWizardUrl;
import com.mayhew3.drafttower.client.DraftTowerGinModule.UnclaimedPlayerInfoUrl;
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent;
import com.mayhew3.drafttower.client.events.LoginEvent;
import com.mayhew3.drafttower.shared.*;

import java.util.*;

/**
 * {@link UnclaimedPlayerDataProvider} which makes a single request (per dataset), then serves all
 * UI requests out of a cache.
 */
@Singleton
public class CachingUnclaimedPlayerDataProvider extends UnclaimedPlayerDataProvider implements
    DraftStatusChangedEvent.Handler,
    LoginEvent.Handler {

  private static final class SortSpec {
    private final PlayerColumn column;
    private final boolean ascending;

    private SortSpec(PlayerColumn column, boolean ascending) {
      this.column = column;
      this.ascending = ascending;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      SortSpec sortSpec = (SortSpec) o;

      if (ascending != sortSpec.ascending) return false;
      if (column != sortSpec.column) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = column.hashCode();
      result = 31 * result + (ascending ? 1 : 0);
      return result;
    }
  }

  private final class PlayerList {
    private final Map<SortSpec, List<Player>> playersBySortCol = new HashMap<>();
    private final Set<Long> pickedPlayers = new HashSet<>();

    private PlayerList(List<Player> players, PlayerColumn defaultSortCol, boolean defaultSortAscending) {
      playersBySortCol.put(new SortSpec(defaultSortCol, defaultSortAscending), players);
    }

    private Iterable<Player> getPlayers(TableSpec tableSpec,
        int rowStart, int rowCount,
        final Position positionFilter,
        final boolean hideInjuries,
        final String nameFilter) {
      // TODO(kprevas): refetch when nameFilter is set to get 0 AB/0 INN players?
      SortSpec sortSpec = new SortSpec(tableSpec.getSortCol(), tableSpec.isAscending());
      if (!playersBySortCol.containsKey(sortSpec)) {
        List<Player> players = playersBySortCol.values().iterator().next();
        playersBySortCol.put(sortSpec,
            Ordering.from(sortSpec.column.getComparator(sortSpec.ascending)).sortedCopy(players));
      }
      return Iterables.limit(Iterables.skip(Iterables.filter(
          playersBySortCol.get(sortSpec),
          new Predicate<Player>() {
            @Override
            public boolean apply(Player player) {
              return (nameFilter == null
                      || player.getColumnValues().get(PlayerColumn.NAME).toLowerCase()
                          .contains(nameFilter.toLowerCase()))
                  && (!hideInjuries || player.getInjury() == null)
                  && (positionFilter == null || positionFilter.apply(player, openPositions))
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
  }

  private final Map<PlayerDataSet, PlayerList> playersByDataSet = new HashMap<>();

  private Set<Position> openPositions;

  @Inject
  public CachingUnclaimedPlayerDataProvider(BeanFactory beanFactory,
      @UnclaimedPlayerInfoUrl String playerInfoUrl,
      @ChangePlayerRankUrl String changePlayerRankUrl,
      @CopyPlayerRanksUrl String copyPlayerRanksUrl,
      @SetAutoPickWizardUrl String setAutoPickWizardUrl,
      TeamsInfo teamsInfo,
      EventBus eventBus) {
    super(beanFactory, playerInfoUrl, changePlayerRankUrl, copyPlayerRanksUrl, setAutoPickWizardUrl, teamsInfo, eventBus);
    eventBus.addHandler(DraftStatusChangedEvent.TYPE, this);
    eventBus.addHandler(LoginEvent.TYPE, this);
  }

  @Override
  public void onLogin(LoginEvent event) {
    requestData(UnclaimedPlayerTable.DEFAULT_DATA_SET,
        UnclaimedPlayerTable.DEFAULT_SORT_COL,
        UnclaimedPlayerTable.DEFAULT_SORT_ASCENDING, null);
  }

  private void requestData(
      final PlayerDataSet dataSet,
      final PlayerColumn defaultSortCol,
      final boolean defaultSortAscending,
      final Runnable callback) {
    if (!teamsInfo.isLoggedIn()) {
      return;
    }
    RequestBuilder requestBuilder =
        new RequestBuilder(RequestBuilder.POST, playerInfoUrl);
    AutoBean<UnclaimedPlayerListRequest> requestBean =
        beanFactory.createUnclaimedPlayerListRequest();
    UnclaimedPlayerListRequest request = requestBean.as();
    request.setTeamToken(teamsInfo.getTeamToken());

    request.setRowCount(-1);
    request.setRowStart(-1);

    TableSpec tableSpec = beanFactory.createTableSpec().as();
    tableSpec.setPlayerDataSet(dataSet);
    tableSpec.setSortCol(defaultSortCol);
    tableSpec.setAscending(defaultSortAscending);
    request.setTableSpec(tableSpec);

    RequestCallbackWithBackoff.sendRequest(requestBuilder,
        AutoBeanCodex.encode(requestBean).getPayload(),
        new RequestCallbackWithBackoff() {
          @Override
          public void onResponseReceived(Request request, Response response) {
            UnclaimedPlayerListResponse playerListResponse =
                AutoBeanCodex.decode(beanFactory, UnclaimedPlayerListResponse.class,
                    response.getText()).as();
            playersByDataSet.put(dataSet,
                new PlayerList(playerListResponse.getPlayers(), defaultSortCol, defaultSortAscending));
            if (callback != null) {
              callback.run();
            }
          }
        });
  }

  @Override
  protected void onRangeChanged(final HasData<Player> display) {
    final int rowStart = display.getVisibleRange().getStart();
    int rowCount = display.getVisibleRange().getLength();
    if (display instanceof UnclaimedPlayerTable) {
      UnclaimedPlayerTable table = (UnclaimedPlayerTable) display;
      Position positionFilter = table.getPositionFilter();
      boolean hideInjuries = table.getHideInjuries();
      TableSpec tableSpec = table.getTableSpec();
      String nameFilter = table.getNameFilter();
      if (!playersByDataSet.containsKey(tableSpec.getPlayerDataSet())) {
        requestData(tableSpec.getPlayerDataSet(), tableSpec.getSortCol(), tableSpec.isAscending(),
            new Runnable() {
              @Override
              public void run() {
                onRangeChanged(display);
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
    List<DraftPick> picks = event.getStatus().getPicks();
    for (PlayerList playerList : playersByDataSet.values()) {
      playerList.ensurePlayersRemoved(picks);
    }
    openPositions = RosterUtil.getOpenPositions(
        Lists.newArrayList(Iterables.filter(picks,
            new Predicate<DraftPick>() {
              @Override
              public boolean apply(DraftPick input) {
                return input.getTeam() == teamsInfo.getTeam();
              }
            })));
  }
}