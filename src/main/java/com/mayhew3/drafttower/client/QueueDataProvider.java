package com.mayhew3.drafttower.client;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.*;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.mayhew3.drafttower.client.DraftTowerGinModule.QueuesUrl;
import com.mayhew3.drafttower.client.events.DequeuePlayerEvent;
import com.mayhew3.drafttower.client.events.EnqueuePlayerEvent;
import com.mayhew3.drafttower.client.events.ReorderPlayerQueueEvent;
import com.mayhew3.drafttower.shared.*;

/**
 * Data provider for players queue.
 */
@Singleton
public class QueueDataProvider extends AsyncDataProvider<QueueEntry> implements
    EnqueuePlayerEvent.Handler,
    DequeuePlayerEvent.Handler,
    ReorderPlayerQueueEvent.Handler {

  private final BeanFactory beanFactory;
  private final String queuesUrl;
  private final TeamInfo teamInfo;

  @Inject
  public QueueDataProvider(
      BeanFactory beanFactory,
      @QueuesUrl String queuesUrl,
      TeamInfo teamInfo,
      EventBus eventBus) {
    this.beanFactory = beanFactory;
    this.queuesUrl = queuesUrl;
    this.teamInfo = teamInfo;

    eventBus.addHandler(EnqueuePlayerEvent.TYPE, this);
    eventBus.addHandler(DequeuePlayerEvent.TYPE, this);
    eventBus.addHandler(ReorderPlayerQueueEvent.TYPE, this);
  }

  @Override
  protected void onRangeChanged(final HasData<QueueEntry> display) {
    if (!teamInfo.isLoggedIn()) {
      return;
    }
    RequestBuilder requestBuilder =
        new RequestBuilder(RequestBuilder.POST, queuesUrl + "/" + ServletEndpoints.QUEUE_GET);
    try {
      AutoBean<GetPlayerQueueRequest> requestBean =
          beanFactory.createPlayerQueueRequest();
      GetPlayerQueueRequest request = requestBean.as();
      request.setTeamToken(teamInfo.getTeamToken());

      requestBuilder.sendRequest(AutoBeanCodex.encode(requestBean).getPayload(),
          new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
              GetPlayerQueueResponse queueResponse =
                  AutoBeanCodex.decode(beanFactory, GetPlayerQueueResponse.class,
                      response.getText()).as();
              display.setRowData(0, queueResponse.getQueue());
              display.setRowCount(queueResponse.getQueue().size());
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
  public void onPlayerEnqueued(EnqueuePlayerEvent event) {
    enqueueOrDequeue(ServletEndpoints.QUEUE_ADD, event.getPlayerId());
  }

  @Override
  public void onPlayerDequeued(DequeuePlayerEvent event) {
    enqueueOrDequeue(ServletEndpoints.QUEUE_REMOVE, event.getPlayerId());
  }

  private void enqueueOrDequeue(String action, long playerId) {
    if (!teamInfo.isLoggedIn()) {
      return;
    }
    RequestBuilder requestBuilder =
        new RequestBuilder(RequestBuilder.POST, queuesUrl + "/" + action);
    try {
      AutoBean<EnqueueOrDequeuePlayerRequest> requestBean =
          beanFactory.createEnqueueOrDequeuePlayerRequest();
      EnqueueOrDequeuePlayerRequest request = requestBean.as();
      request.setTeamToken(teamInfo.getTeamToken());
      request.setPlayerId(playerId);

      requestBuilder.sendRequest(AutoBeanCodex.encode(requestBean).getPayload(),
          new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
              for (HasData<QueueEntry> dataDisplay : getDataDisplays()) {
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
  public void onQueueReordered(ReorderPlayerQueueEvent event) {
    if (!teamInfo.isLoggedIn()) {
      return;
    }
    RequestBuilder requestBuilder =
        new RequestBuilder(RequestBuilder.POST, queuesUrl + "/" + ServletEndpoints.QUEUE_REORDER);
    try {
      AutoBean<ReorderPlayerQueueRequest> requestBean =
          beanFactory.createReorderPlayerQueueRequest();
      ReorderPlayerQueueRequest request = requestBean.as();
      request.setTeamToken(teamInfo.getTeamToken());
      request.setPlayerId(event.getPlayerId());
      request.setNewPosition(event.getNewPosition());

      requestBuilder.sendRequest(AutoBeanCodex.encode(requestBean).getPayload(),
          new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
              for (HasData<QueueEntry> dataDisplay : getDataDisplays()) {
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