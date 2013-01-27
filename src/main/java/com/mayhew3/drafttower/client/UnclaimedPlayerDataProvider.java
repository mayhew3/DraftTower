package com.mayhew3.drafttower.client;

import com.google.gwt.http.client.*;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.mayhew3.drafttower.client.DraftTowerGinModule.UnclaimedPlayerInfoUrl;
import com.mayhew3.drafttower.client.PlayerTable.PlayerTableColumn;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.Player;
import com.mayhew3.drafttower.shared.UnclaimedPlayerListRequest;
import com.mayhew3.drafttower.shared.UnclaimedPlayerListResponse;

/**
 * Data provider for player tables.
 */
@Singleton
public class UnclaimedPlayerDataProvider extends AsyncDataProvider<Player> {

  private final BeanFactory beanFactory;
  private final String playerInfoUrl;

  @Inject
  public UnclaimedPlayerDataProvider(
      BeanFactory beanFactory,
      @UnclaimedPlayerInfoUrl String playerInfoUrl) {
    this.beanFactory = beanFactory;
    this.playerInfoUrl = playerInfoUrl;
  }

  @Override
  protected void onRangeChanged(final HasData<Player> display) {
    RequestBuilder requestBuilder =
        new RequestBuilder(RequestBuilder.POST, playerInfoUrl);
    try {
      AutoBean<UnclaimedPlayerListRequest> requestBean =
          beanFactory.createUnclaimedPlayerListRequest();
      UnclaimedPlayerListRequest request = requestBean.as();
      final int rowStart = display.getVisibleRange().getStart();
      int rowCount = display.getVisibleRange().getLength();
      request.setRowCount(rowCount);
      request.setRowStart(rowStart);

      if (display instanceof PlayerTable) {
        PlayerTable table = (PlayerTable) display;
        ColumnSortList sortColumns = table.getColumnSortList();
        if (sortColumns.size() > 0) {
          request.setSortCol(((PlayerTableColumn) sortColumns.get(0).getColumn()).getColumn());
        }
        request.setPositionFilter(table.getPositionFilter());
      }

      requestBuilder.sendRequest(AutoBeanCodex.encode(requestBean).getPayload(),
          new RequestCallback() {
            public void onResponseReceived(Request request, Response response) {
              UnclaimedPlayerListResponse playerListResponse =
                  AutoBeanCodex.decode(beanFactory, UnclaimedPlayerListResponse.class,
                      response.getText()).as();
              display.setRowData(rowStart, playerListResponse.getPlayers());
              display.setRowCount(playerListResponse.getTotalPlayers(), true);
            }

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