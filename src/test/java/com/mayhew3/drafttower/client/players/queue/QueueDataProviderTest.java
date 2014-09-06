package com.mayhew3.drafttower.client.players.queue;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gwt.event.shared.EventBus;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.google.web.bindery.event.shared.Event;
import com.mayhew3.drafttower.client.TeamsInfo;
import com.mayhew3.drafttower.client.events.*;
import com.mayhew3.drafttower.client.players.PlayerTableView;
import com.mayhew3.drafttower.client.serverrpc.ServerRpc;
import com.mayhew3.drafttower.shared.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

/**
 * Test for {@link QueueDataProvider}.
 */
public class QueueDataProviderTest {

  private BeanFactory beanFactory;
  private ServerRpc serverRpc;
  private TeamsInfo teamsInfo;
  private EventBus eventBus;
  private QueueDataProvider provider;
  private PlayerTableView<QueueEntry> view;

  @Captor private ArgumentCaptor<AutoBean<GetPlayerQueueRequest>> getQueueRequestCaptor;
  @Captor private ArgumentCaptor<AutoBean<EnqueueOrDequeuePlayerRequest>> modifyRequestCaptor;
  @Captor private ArgumentCaptor<AutoBean<ReorderPlayerQueueRequest>> reorderRequestCaptor;
  @Captor private ArgumentCaptor<List<QueueEntry>> queueCaptor;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    beanFactory = AutoBeanFactorySource.create(BeanFactory.class);
    serverRpc = Mockito.mock(ServerRpc.class);
    teamsInfo = Mockito.mock(TeamsInfo.class);
    Mockito.when(teamsInfo.isLoggedIn()).thenReturn(true);
    Mockito.when(teamsInfo.getTeamToken()).thenReturn("1");
    eventBus = Mockito.mock(EventBus.class);
    provider = new QueueDataProvider(beanFactory,
        serverRpc,
        teamsInfo,
        eventBus);
    view = Mockito.mock(PlayerTableView.class);
    provider.setView(view);
    Mockito.reset(serverRpc);
  }

  @Test
  public void testRefreshOnDraftStatusChangeFirstStatus() {
    DraftPick draftPick =
        DraftStatusTestUtil.createDraftPick(1, "", false, "P", 1, beanFactory);
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(
        Lists.newArrayList(draftPick), beanFactory);
    provider.onDraftStatusChanged(new DraftStatusChangedEvent(draftStatus));
    Mockito.verify(view).refresh();
  }

  @Test
  public void testNoRefreshOnDraftStatusChangeNoNewPicks() {
    DraftPick draftPick =
        DraftStatusTestUtil.createDraftPick(1, "", false, "P", 1, beanFactory);
    DraftStatus initialDraftStatus = DraftStatusTestUtil.createDraftStatus(
        Lists.newArrayList(draftPick), beanFactory);
    provider.onDraftStatusChanged(new DraftStatusChangedEvent(initialDraftStatus));
    Mockito.reset(view);
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(
        Lists.newArrayList(draftPick), beanFactory);
    provider.onDraftStatusChanged(new DraftStatusChangedEvent(draftStatus));
    Mockito.verifyZeroInteractions(view);
  }

  @Test
  public void testNoRefreshOnDraftStatusChangePickBackedOut() {
    DraftPick draftPick =
        DraftStatusTestUtil.createDraftPick(1, "", false, "P", 1, beanFactory);
    DraftStatus initialDraftStatus = DraftStatusTestUtil.createDraftStatus(
        Lists.newArrayList(draftPick), beanFactory);
    provider.onDraftStatusChanged(new DraftStatusChangedEvent(initialDraftStatus));
    Mockito.reset(view);
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(
        Lists.<DraftPick>newArrayList(), beanFactory);
    provider.onDraftStatusChanged(new DraftStatusChangedEvent(draftStatus));
    Mockito.verify(view).refresh();
  }

  @Test
  public void testRefreshOnDraftStatusChangeQueuedPlayerPicked() {
    DraftStatus initialDraftStatus = DraftStatusTestUtil.createDraftStatus(
        Lists.<DraftPick>newArrayList(), beanFactory);
    provider.onDraftStatusChanged(new DraftStatusChangedEvent(initialDraftStatus));
    Mockito.reset(view);
    QueueEntry queueEntry = beanFactory.createQueueEntry().as();
    queueEntry.setPlayerId(1);
    Mockito.when(view.getVisibleItems()).thenReturn(Lists.newArrayList(queueEntry));
    DraftPick draftPick =
        DraftStatusTestUtil.createDraftPick(1, "", false, "P", 1, beanFactory);
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(
        Lists.newArrayList(draftPick), beanFactory);
    provider.onDraftStatusChanged(new DraftStatusChangedEvent(draftStatus));
    Mockito.verify(view).refresh();
  }

  @Test
  public void testNoRefreshOnDraftStatusChangeNoQueuedPlayerPicked() {
    DraftStatus initialDraftStatus = DraftStatusTestUtil.createDraftStatus(
        Lists.<DraftPick>newArrayList(), beanFactory);
    provider.onDraftStatusChanged(new DraftStatusChangedEvent(initialDraftStatus));
    Mockito.reset(view);
    QueueEntry queueEntry = beanFactory.createQueueEntry().as();
    queueEntry.setPlayerId(2);
    Mockito.when(view.getVisibleItems()).thenReturn(Lists.newArrayList(queueEntry));
    DraftPick draftPick =
        DraftStatusTestUtil.createDraftPick(1, "", false, "P", 1, beanFactory);
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(
        Lists.newArrayList(draftPick), beanFactory);
    provider.onDraftStatusChanged(new DraftStatusChangedEvent(draftStatus));
    Mockito.verify(view, Mockito.never()).refresh();
  }

  @Test
  public void testNoRequestSentOnRangeChangeBeforeLogin() {
    Mockito.when(teamsInfo.isLoggedIn()).thenReturn(false);
    provider.rangeChanged(view);
    Mockito.verifyZeroInteractions(serverRpc);
  }

  @Test
  public void testRequestSentOnRangeChange() {
    provider.rangeChanged(view);
    Mockito.verify(serverRpc).sendGetPlayerQueueRequest(getQueueRequestCaptor.capture(),
        Mockito.<Function<GetPlayerQueueResponse, Void>>any());
    GetPlayerQueueRequest sentRequest = getQueueRequestCaptor.getValue().as();
    Assert.assertEquals("1", sentRequest.getTeamToken());
  }

  @Test
  public void testHandleQueueResponseEmptyQueue() {
    GetPlayerQueueResponse response = beanFactory.createPlayerQueueResponse().as();
    response.setQueue(Lists.<QueueEntry>newArrayList());
    provider.handlePlayerQueueResponse(response, view);
    Mockito.verify(view).setRowCount(1);
    Mockito.verify(view).setRowData(Mockito.eq(0), queueCaptor.capture());
    List<QueueEntry> queue = queueCaptor.getValue();
    QueueEntry queueEntry = queue.get(0);
    Assert.assertEquals(QueueDataProvider.FAKE_ENTRY_ID, queueEntry.getPlayerId());
    Assert.assertTrue(queueEntry.getEligibilities().isEmpty());
    Assert.assertEquals("Drag players here", queueEntry.getPlayerName());
  }

  @Test
  public void testHandleQueueResponse() {
    GetPlayerQueueResponse response = beanFactory.createPlayerQueueResponse().as();
    ArrayList<QueueEntry> queueEntries = Lists.newArrayList(
        createQueueEntry(1, "1", Lists.newArrayList("P")),
        createQueueEntry(2, "2", Lists.newArrayList("1B", "OF")));
    response.setQueue(queueEntries);
    provider.handlePlayerQueueResponse(response, view);
    Mockito.verify(view).setRowCount(2);
    Mockito.verify(view).setRowData(Mockito.eq(0), queueCaptor.capture());
    List<QueueEntry> queue = queueCaptor.getValue();
    Assert.assertEquals(queueEntries, queue);
  }

  @Test
  public void testNoRequestSentOnPlayerEnqueueBeforeLogin() {
    Mockito.when(teamsInfo.isLoggedIn()).thenReturn(false);
    provider.onPlayerEnqueued(new EnqueuePlayerEvent(1, 0));
    Mockito.verifyZeroInteractions(serverRpc);
  }

  @Test
  public void testNoRequestSentOnEnqueueAlreadyEnqueuedPlayer() {
    provider.queue = Lists.newArrayList(createQueueEntry(1, "1", Lists.newArrayList("P")));
    provider.onPlayerEnqueued(new EnqueuePlayerEvent(1, 0));
    Mockito.verifyZeroInteractions(serverRpc);
  }

  @Test
  public void testEnqueuePlayer() {
    provider.onPlayerEnqueued(new EnqueuePlayerEvent(1, 0));
    Mockito.verify(serverRpc).sendEnqueueOrDequeueRequest(
        Mockito.eq(ServletEndpoints.QUEUE_ADD),
        modifyRequestCaptor.capture(),
        Mockito.<Runnable>any());
    EnqueueOrDequeuePlayerRequest sentRequest = modifyRequestCaptor.getValue().as();
    Assert.assertEquals("1", sentRequest.getTeamToken());
    Assert.assertEquals(1, sentRequest.getPlayerId());
    Assert.assertEquals(0, (int) sentRequest.getPosition());
  }

  @Test
  public void testNoRequestSentOnPlayerDequeueBeforeLogin() {
    Mockito.when(teamsInfo.isLoggedIn()).thenReturn(false);
    provider.onPlayerDequeued(new DequeuePlayerEvent(1));
    Mockito.verifyZeroInteractions(serverRpc);
  }

  @Test
  public void testNoRequestSentOnDequeueNonEnqueuedPlayer() {
    provider.onPlayerDequeued(new DequeuePlayerEvent(1));
    Mockito.verifyZeroInteractions(serverRpc);
  }

  @Test
  public void testDequeuePlayer() {
    provider.queue = Lists.newArrayList(createQueueEntry(1, "1", Lists.newArrayList("P")));
    provider.onPlayerDequeued(new DequeuePlayerEvent(1));
    Mockito.verify(serverRpc).sendEnqueueOrDequeueRequest(
        Mockito.eq(ServletEndpoints.QUEUE_REMOVE),
        modifyRequestCaptor.capture(),
        Mockito.<Runnable>any());
    EnqueueOrDequeuePlayerRequest sentRequest = modifyRequestCaptor.getValue().as();
    Assert.assertEquals("1", sentRequest.getTeamToken());
    Assert.assertEquals(1, sentRequest.getPlayerId());
  }

  @Test
  public void testNoRequestSentOnPlayerReorderBeforeLogin() {
    Mockito.when(teamsInfo.isLoggedIn()).thenReturn(false);
    provider.onQueueReordered(new ReorderPlayerQueueEvent(1, 0));
    Mockito.verifyZeroInteractions(serverRpc);
  }

  @Test
  public void testReorderPlayer() {
    provider.queue = Lists.newArrayList(createQueueEntry(1, "1", Lists.newArrayList("P")));
    provider.onQueueReordered(new ReorderPlayerQueueEvent(1, 1));
    Mockito.verify(serverRpc).sendReorderQueueRequest(
        reorderRequestCaptor.capture(),
        Mockito.<Runnable>any());
    ReorderPlayerQueueRequest sentRequest = reorderRequestCaptor.getValue().as();
    Assert.assertEquals("1", sentRequest.getTeamToken());
    Assert.assertEquals(1, sentRequest.getPlayerId());
    Assert.assertEquals(1, sentRequest.getNewPosition());
  }

  @Test
  public void testIsPlayerQueuedQueueNotSetYet() {
    Assert.assertFalse(provider.isPlayerQueued(1));
  }

  @Test
  public void testIsPlayerQueued() {
    provider.queue = Lists.newArrayList(createQueueEntry(1, "1", Lists.newArrayList("P")));
    Assert.assertTrue(provider.isPlayerQueued(1));
    Assert.assertFalse(provider.isPlayerQueued(2));
  }

  @Test
  public void testSelect() {
    provider.select(createQueueEntry(1, "1", Lists.newArrayList("P")));
    ArgumentCaptor<PlayerSelectedEvent> eventCaptor = ArgumentCaptor.forClass(PlayerSelectedEvent.class);
    Mockito.verify(eventBus).fireEvent(eventCaptor.capture());
    PlayerSelectedEvent firedEvent = eventCaptor.getValue();
    Assert.assertEquals(1, (long) firedEvent.getPlayerId());
    Assert.assertEquals("1", firedEvent.getPlayerName());
  }

  @Test
  public void testNoEventFiredOnSelectingFakeEntry() {
    provider.select(createQueueEntry(
        QueueDataProvider.FAKE_ENTRY_ID, "", Lists.<String>newArrayList()));
    Mockito.verify(eventBus, Mockito.never()).fireEvent(Mockito.<Event<?>>any());
  }

  private QueueEntry createQueueEntry(long id, String name, List<String> eligibilities) {
    QueueEntry queueEntry = beanFactory.createQueueEntry().as();
    queueEntry.setPlayerId(id);
    queueEntry.setPlayerName(name);
    queueEntry.setEligibilities(eligibilities);
    return queueEntry;
  }
}