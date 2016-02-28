package com.mayhew3.drafttower.client.players.queue;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.view.client.HasData;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.mayhew3.drafttower.client.TeamsInfo;
import com.mayhew3.drafttower.client.events.DequeuePlayerEvent;
import com.mayhew3.drafttower.client.events.EnqueuePlayerEvent;
import com.mayhew3.drafttower.client.events.PlayerSelectedEvent;
import com.mayhew3.drafttower.client.events.ReorderPlayerQueueEvent;
import com.mayhew3.drafttower.client.players.PlayerDataProvider;
import com.mayhew3.drafttower.client.players.PlayerTableView;
import com.mayhew3.drafttower.client.serverrpc.ServerRpc;
import com.mayhew3.drafttower.shared.*;

import java.util.List;

/**
 * Data provider for players queue.
 */
@Singleton
public class QueueDataProvider extends PlayerDataProvider<QueueEntry> implements
    EnqueuePlayerEvent.Handler,
    DequeuePlayerEvent.Handler,
    ReorderPlayerQueueEvent.Handler {

  static final int FAKE_ENTRY_ID = -1;

  private final BeanFactory beanFactory;
  private final ServerRpc serverRpc;
  private final TeamsInfo teamsInfo;
  private final EventBus eventBus;

  @VisibleForTesting List<QueueEntry> queue;

  @Inject
  public QueueDataProvider(
      BeanFactory beanFactory,
      ServerRpc serverRpc,
      TeamsInfo teamsInfo,
      EventBus eventBus) {
    super(eventBus);

    this.beanFactory = beanFactory;
    this.serverRpc = serverRpc;
    this.teamsInfo = teamsInfo;
    this.eventBus = eventBus;

    eventBus.addHandler(EnqueuePlayerEvent.TYPE, this);
    eventBus.addHandler(DequeuePlayerEvent.TYPE, this);
    eventBus.addHandler(ReorderPlayerQueueEvent.TYPE, this);
  }

  @Override
  public void setView(PlayerTableView<QueueEntry> view) {
    super.setView(view);
  }

  @Override
  protected void rangeChanged(final HasData<QueueEntry> display) {
    if (!teamsInfo.isLoggedIn()) {
      return;
    }
    if (teamsInfo.isGuest()) {
      display.setRowCount(0);
      return;
    }
    AutoBean<GetPlayerQueueRequest> requestBean =
        beanFactory.createPlayerQueueRequest();
    GetPlayerQueueRequest request = requestBean.as();
    request.setTeamToken(teamsInfo.getTeamToken());
    serverRpc.sendGetPlayerQueueRequest(requestBean, new Function<GetPlayerQueueResponse, Void>() {
      @Override
      public Void apply(GetPlayerQueueResponse queueResponse) {
        handlePlayerQueueResponse(queueResponse, display);
        return null;
      }
    });
  }

  @VisibleForTesting
  void handlePlayerQueueResponse(GetPlayerQueueResponse queueResponse,
      HasData<QueueEntry> display) {
    queue = queueResponse.getQueue();
    if (queue.isEmpty()) {
      QueueEntry fakeEntry = beanFactory.createQueueEntry().as();
      fakeEntry.setPlayerId(FAKE_ENTRY_ID);
      fakeEntry.setPlayerName("Drag players here");
      fakeEntry.setEligibilities(ImmutableList.<String>of());
      queue = ImmutableList.of(fakeEntry);
    }
    display.setRowData(0, queue);
    display.setRowCount(queue.size());
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
        getView().refresh();
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
        getView().refresh();
      }
    });
  }

  public boolean isPlayerQueued(long playerId) {
    return queue != null && Iterables.any(queue, new QueueEntryPredicate(playerId));
  }

  @Override
  protected Predicate<QueueEntry> createPredicate(long playerId) {
    return new QueueEntryPredicate(playerId);
  }

  public void select(QueueEntry entry) {
    if (entry.getPlayerId() != FAKE_ENTRY_ID) {
      eventBus.fireEvent(new PlayerSelectedEvent(entry.getPlayerId(), entry.getPlayerName()));
    }
  }

  public void enqueue(Player player, Integer position) {
    eventBus.fireEvent(new EnqueuePlayerEvent(
        player.getPlayerId(),
        position));
  }

  public void dequeue(QueueEntry entry) {
    eventBus.fireEvent(new DequeuePlayerEvent(entry.getPlayerId()));
  }

  public void reorderQueue(QueueEntry entry, int targetPosition) {
    eventBus.fireEvent(new ReorderPlayerQueueEvent(
        entry.getPlayerId(),
        targetPosition));
  }

  public float getPickPrediction(long playerId) {
    return lastStatus.getPickPredictions().containsKey(playerId)
        ? lastStatus.getPickPredictions().get(playerId) : 0f;
  }
}