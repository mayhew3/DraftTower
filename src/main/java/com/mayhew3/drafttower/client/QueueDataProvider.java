package com.mayhew3.drafttower.client;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.mayhew3.drafttower.client.events.DequeuePlayerEvent;
import com.mayhew3.drafttower.client.events.EnqueuePlayerEvent;
import com.mayhew3.drafttower.client.events.ReorderPlayerQueueEvent;
import com.mayhew3.drafttower.client.serverrpc.ServerRpc;
import com.mayhew3.drafttower.shared.*;

import java.util.List;

/**
 * Data provider for players queue.
 */
@Singleton
public class QueueDataProvider extends AsyncDataProvider<QueueEntry> implements
    EnqueuePlayerEvent.Handler,
    DequeuePlayerEvent.Handler,
    ReorderPlayerQueueEvent.Handler {

  private final BeanFactory beanFactory;
  private final ServerRpc serverRpc;
  private final TeamsInfo teamsInfo;

  private List<QueueEntry> queue;

  @Inject
  public QueueDataProvider(
      BeanFactory beanFactory,
      ServerRpc serverRpc,
      TeamsInfo teamsInfo,
      EventBus eventBus) {
    this.beanFactory = beanFactory;
    this.serverRpc = serverRpc;
    this.teamsInfo = teamsInfo;

    eventBus.addHandler(EnqueuePlayerEvent.TYPE, this);
    eventBus.addHandler(DequeuePlayerEvent.TYPE, this);
    eventBus.addHandler(ReorderPlayerQueueEvent.TYPE, this);
  }

  @Override
  protected void onRangeChanged(final HasData<QueueEntry> display) {
    if (!teamsInfo.isLoggedIn()) {
      return;
    }
    AutoBean<GetPlayerQueueRequest> requestBean =
        beanFactory.createPlayerQueueRequest();
    GetPlayerQueueRequest request = requestBean.as();
    request.setTeamToken(teamsInfo.getTeamToken());
    serverRpc.sendGetPlayerQueueRequest(requestBean, new Function<GetPlayerQueueResponse, Void>() {
      @Override
      public Void apply(GetPlayerQueueResponse queueResponse) {
        queue = queueResponse.getQueue();
        if (queue.isEmpty()) {
          QueueEntry fakeEntry = beanFactory.createQueueEntry().as();
          fakeEntry.setPlayerId(-1);
          fakeEntry.setPlayerName("Drag players here");
          fakeEntry.setEligibilities(ImmutableList.<String>of());
          queue = ImmutableList.of(fakeEntry);
        }
        display.setRowData(0, queue);
        display.setRowCount(queue.size());
        return null;
      }
    });
  }

  @Override
  public void onPlayerEnqueued(EnqueuePlayerEvent event) {
    if (!isPlayerQueued(event.getPlayerId())) {
      enqueueOrDequeue(ServletEndpoints.QUEUE_ADD, event.getPlayerId(), event.getPosition());
    }
  }

  @Override
  public void onPlayerDequeued(DequeuePlayerEvent event) {
    if (isPlayerQueued(event.getPlayerId())) {
      enqueueOrDequeue(ServletEndpoints.QUEUE_REMOVE, event.getPlayerId(), null);
    }
  }

  private void enqueueOrDequeue(String action, long playerId, Integer position) {
    if (!teamsInfo.isLoggedIn()) {
      return;
    }
    AutoBean<EnqueueOrDequeuePlayerRequest> requestBean =
        beanFactory.createEnqueueOrDequeuePlayerRequest();
    EnqueueOrDequeuePlayerRequest request = requestBean.as();
    request.setTeamToken(teamsInfo.getTeamToken());
    request.setPlayerId(playerId);
    request.setPosition(position);
    serverRpc.sendEnqueueOrDequeueRequest(action, requestBean, new Runnable() {
      @Override
      public void run() {
        for (HasData<QueueEntry> dataDisplay : getDataDisplays()) {
          dataDisplay.setVisibleRangeAndClearData(dataDisplay.getVisibleRange(), true);
        }
      }
    });
  }

  @Override
  public void onQueueReordered(ReorderPlayerQueueEvent event) {
    if (!teamsInfo.isLoggedIn()) {
      return;
    }
    AutoBean<ReorderPlayerQueueRequest> requestBean =
        beanFactory.createReorderPlayerQueueRequest();
    ReorderPlayerQueueRequest request = requestBean.as();
    request.setTeamToken(teamsInfo.getTeamToken());
    request.setPlayerId(event.getPlayerId());
    request.setNewPosition(event.getNewPosition());
    serverRpc.sendReorderQueueRequest(requestBean, new Runnable() {
      @Override
      public void run() {
        for (HasData<QueueEntry> dataDisplay : getDataDisplays()) {
          dataDisplay.setVisibleRangeAndClearData(dataDisplay.getVisibleRange(), true);
        }
      }
    });
  }

  public boolean isPlayerQueued(long playerId) {
    return queue != null && Iterables.any(queue, new QueueEntryPredicate(playerId));
  }
}