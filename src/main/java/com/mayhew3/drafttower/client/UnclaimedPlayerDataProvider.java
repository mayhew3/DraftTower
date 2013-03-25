package com.mayhew3.drafttower.client;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.*;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.mayhew3.drafttower.client.DraftTowerGinModule.CopyPlayerRanksUrl;
import com.mayhew3.drafttower.client.DraftTowerGinModule.ChangePlayerRankUrl;
import com.mayhew3.drafttower.client.DraftTowerGinModule.SetAutoPickTableSpecUrl;
import com.mayhew3.drafttower.client.DraftTowerGinModule.UnclaimedPlayerInfoUrl;
import com.mayhew3.drafttower.client.events.ChangePlayerRankEvent;
import com.mayhew3.drafttower.client.events.CopyAllPlayerRanksEvent;
import com.mayhew3.drafttower.client.events.IsUsersAutoPickTableSpecEvent;
import com.mayhew3.drafttower.client.events.SetAutoPickTableSpecEvent;
import com.mayhew3.drafttower.shared.*;

import java.util.Set;

/**
 * Data provider for player tables.
 */
@Singleton
public class UnclaimedPlayerDataProvider extends AsyncDataProvider<Player> implements
    ChangePlayerRankEvent.Handler,
    SetAutoPickTableSpecEvent.Handler,
    CopyAllPlayerRanksEvent.Handler {

  private final BeanFactory beanFactory;
  private final String playerInfoUrl;
  private final String changePlayerRankUrl;
  private final String copyPlayerRanksUrl;
  private final String setAutoPickTableSpecUrl;
  private final TeamsInfo teamsInfo;
  private final EventBus eventBus;

  @Inject
  public UnclaimedPlayerDataProvider(
      BeanFactory beanFactory,
      @UnclaimedPlayerInfoUrl String playerInfoUrl,
      @ChangePlayerRankUrl String changePlayerRankUrl,
      @CopyPlayerRanksUrl String copyPlayerRanksUrl,
      @SetAutoPickTableSpecUrl String setAutoPickTableSpecUrl,
      TeamsInfo teamsInfo,
      EventBus eventBus) {
    this.beanFactory = beanFactory;
    this.playerInfoUrl = playerInfoUrl;
    this.changePlayerRankUrl = changePlayerRankUrl;
    this.copyPlayerRanksUrl = copyPlayerRanksUrl;
    this.setAutoPickTableSpecUrl = setAutoPickTableSpecUrl;
    this.teamsInfo = teamsInfo;
    this.eventBus = eventBus;

    eventBus.addHandler(ChangePlayerRankEvent.TYPE, this);
    eventBus.addHandler(SetAutoPickTableSpecEvent.TYPE, this);
    eventBus.addHandler(CopyAllPlayerRanksEvent.TYPE, this);
  }

  @Override
  protected void onRangeChanged(final HasData<Player> display) {
    if (!teamsInfo.isLoggedIn()) {
      return;
    }
    RequestBuilder requestBuilder =
        new RequestBuilder(RequestBuilder.POST, playerInfoUrl);
    try {
      AutoBean<UnclaimedPlayerListRequest> requestBean =
          beanFactory.createUnclaimedPlayerListRequest();
      UnclaimedPlayerListRequest request = requestBean.as();
      request.setTeamToken(teamsInfo.getTeamToken());

      final int rowStart = display.getVisibleRange().getStart();
      int rowCount = display.getVisibleRange().getLength();
      request.setRowCount(rowCount);
      request.setRowStart(rowStart);

      if (display instanceof UnclaimedPlayerTable) {
        UnclaimedPlayerTable table = (UnclaimedPlayerTable) display;
        request.setPositionFilter(table.getPositionFilter());
        request.setHideInjuries(table.getHideInjuries());
        request.setTableSpec(table.getTableSpec());
        request.setSearchQuery(table.getNameFilter());
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
              eventBus.fireEvent(new IsUsersAutoPickTableSpecEvent(playerListResponse.isUsersAutoPickTableSpec()));
              if (display instanceof UnclaimedPlayerTable) {
                ((UnclaimedPlayerTable) display).computePageSize();
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

  @Override
  public void onChangePlayerRank(ChangePlayerRankEvent event) {
    if (!teamsInfo.isLoggedIn()) {
      return;
    }
    RequestBuilder requestBuilder =
        new RequestBuilder(RequestBuilder.POST, changePlayerRankUrl);
    try {
      AutoBean<ChangePlayerRankRequest> requestBean =
          beanFactory.createChangePlayerRankRequest();
      ChangePlayerRankRequest request = requestBean.as();
      request.setTeamToken(teamsInfo.getTeamToken());

      request.setPlayerId(event.getPlayerId());
      request.setNewRank(event.getNewRank());
      request.setPrevRank(event.getPrevRank());

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

  @Override
  public void onCopyAllPlayerRanks(CopyAllPlayerRanksEvent event) {
    if (!teamsInfo.isLoggedIn()) {
      return;
    }
    RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.POST, copyPlayerRanksUrl);
    try {
      AutoBean<CopyAllPlayerRanksRequest> requestBean =
          beanFactory.createCopyAllPlayerRanksRequest();
      CopyAllPlayerRanksRequest request = requestBean.as();
      request.setTeamToken(teamsInfo.getTeamToken());

      request.setTableSpec(event.getTableSpec());

      requestBuilder.sendRequest(AutoBeanCodex.encode(requestBean).getPayload(),
          new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
              Set<HasData<Player>> dataDisplays = getDataDisplays();
              for (HasData<Player> dataDisplay : dataDisplays) {
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

  @Override
  public void onSetAutoPickTableSpec(SetAutoPickTableSpecEvent event) {
    if (!teamsInfo.isLoggedIn()) {
      return;
    }
    RequestBuilder requestBuilder =
        new RequestBuilder(RequestBuilder.POST, setAutoPickTableSpecUrl);
    try {
      AutoBean<SetAutoPickTableSpecRequest> requestBean =
          beanFactory.createSetAutoPickTableSpecRequest();
      SetAutoPickTableSpecRequest request = requestBean.as();
      request.setTeamToken(teamsInfo.getTeamToken());

      request.setTableSpec(event.getTableSpec());

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