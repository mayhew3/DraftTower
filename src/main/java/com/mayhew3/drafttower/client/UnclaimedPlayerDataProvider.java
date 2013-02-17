package com.mayhew3.drafttower.client;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.*;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.mayhew3.drafttower.client.DraftTowerGinModule.ChangePlayerRankUrl;
import com.mayhew3.drafttower.client.DraftTowerGinModule.UnclaimedPlayerInfoUrl;
import com.mayhew3.drafttower.client.events.ChangePlayerRankEvent;
import com.mayhew3.drafttower.shared.*;

/**
 * Data provider for player tables.
 */
@Singleton
public class UnclaimedPlayerDataProvider extends AsyncDataProvider<Player> implements
    ChangePlayerRankEvent.Handler {

  private final BeanFactory beanFactory;
  private final String playerInfoUrl;
  private final String changePlayerRankUrl;
  private final TeamInfo teamInfo;

  @Inject
  public UnclaimedPlayerDataProvider(
      BeanFactory beanFactory,
      @UnclaimedPlayerInfoUrl String playerInfoUrl,
      @ChangePlayerRankUrl String changePlayerRankUrl,
      TeamInfo teamInfo,
      EventBus eventBus) {
    this.beanFactory = beanFactory;
    this.playerInfoUrl = playerInfoUrl;
    this.changePlayerRankUrl = changePlayerRankUrl;
    this.teamInfo = teamInfo;

    eventBus.addHandler(ChangePlayerRankEvent.TYPE, this);
  }

  @Override
  protected void onRangeChanged(final HasData<Player> display) {
    if (!teamInfo.isLoggedIn()) {
      return;
    }
    RequestBuilder requestBuilder =
        new RequestBuilder(RequestBuilder.POST, playerInfoUrl);
    try {
      AutoBean<UnclaimedPlayerListRequest> requestBean =
          beanFactory.createUnclaimedPlayerListRequest();
      UnclaimedPlayerListRequest request = requestBean.as();
      request.setTeamToken(teamInfo.getTeamToken());

      final int rowStart = display.getVisibleRange().getStart();
      int rowCount = display.getVisibleRange().getLength();
      request.setRowCount(rowCount);
      request.setRowStart(rowStart);

      if (display instanceof UnclaimedPlayerTable) {
        UnclaimedPlayerTable table = (UnclaimedPlayerTable) display;
        request.setSortCol(table.getSortedColumn());
        request.setPositionFilter(table.getPositionFilter());
        request.setPlayerDataSet(table.getPlayerDataSet());
        request.setHideInjuries(table.getHideInjuries());
      }

      requestBuilder.sendRequest(AutoBeanCodex.encode(requestBean).getPayload(),
          new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
              UnclaimedPlayerListResponse playerListResponse =
                  AutoBeanCodex.decode(beanFactory, UnclaimedPlayerListResponse.class,
                      response.getText()).as();
              display.setRowData(rowStart, playerListResponse.getPlayers());
              display.setRowCount(playerListResponse.getTotalPlayers(), true);
            }

            @Override
            public void onError(Request request, Throwable exception) {
              // TODO
            }
          });
    } catch (RequestException e) {
      // TODO
      throw new RuntimeException(e);
    }
  }

  @Override
  public void onChangePlayerRank(ChangePlayerRankEvent event) {
    if (!teamInfo.isLoggedIn()) {
      return;
    }
    RequestBuilder requestBuilder =
        new RequestBuilder(RequestBuilder.POST, changePlayerRankUrl);
    try {
      AutoBean<ChangePlayerRankRequest> requestBean =
          beanFactory.createChangePlayerRankRequest();
      ChangePlayerRankRequest request = requestBean.as();
      request.setTeamToken(teamInfo.getTeamToken());

      request.setPlayerId(event.getPlayerId());
      request.setNewRank(event.getNewRank());

      requestBuilder.sendRequest(AutoBeanCodex.encode(requestBean).getPayload(),
          new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
              for (HasData<Player> dataDisplay : getDataDisplays()) {
                dataDisplay.setVisibleRangeAndClearData(dataDisplay.getVisibleRange(), true);
              }
            }

            @Override
            public void onError(Request request, Throwable exception) {
              // TODO
            }
          });
    } catch (RequestException e) {
      // TODO
      throw new RuntimeException(e);
    }
  }
}