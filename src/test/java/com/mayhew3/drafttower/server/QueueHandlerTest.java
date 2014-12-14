package com.mayhew3.drafttower.server;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.shared.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for {@link QueueHandler}.
 */
public class QueueHandlerTest {

  private QueueHandler handler;
  private List<DraftPick> picks;
  private ListMultimap<TeamDraftOrder, QueueEntry> queues;
  private BeanFactory beanFactory;

  @Before
  public void setUp() throws Exception {
    beanFactory = AutoBeanFactorySource.create(BeanFactory.class);
    queues = ArrayListMultimap.create();
    DraftStatus draftStatus = Mockito.mock(DraftStatus.class);
    picks = new ArrayList<>();
    Mockito.when(draftStatus.getPicks()).thenReturn(picks);

    handler = new QueueHandler(beanFactory,
        Mockito.mock(PlayerDataSource.class),
        queues,
        draftStatus,
        new LockImpl());
  }

  @Test
  public void testGetQueue() {
    List<QueueEntry> queue = new ArrayList<>();
    queue.add(createQueueEntry(0));
    queue.add(createQueueEntry(1));
    queue.add(createQueueEntry(2));
    queues.putAll(new TeamDraftOrder(1), queue);
    Assert.assertEquals(queue, handler.getQueue(new TeamDraftOrder(1)));
  }

  @Test
  public void testEnqueue() throws DataSourceException {
    handler.enqueue(new TeamDraftOrder(1), 0, 0);
    Assert.assertEquals(1, queues.get(new TeamDraftOrder(1)).size());
    Assert.assertEquals(0, queues.get(new TeamDraftOrder(1)).get(0).getPlayerId());
    handler.enqueue(new TeamDraftOrder(1), 1, 0);
    Assert.assertEquals(2, queues.get(new TeamDraftOrder(1)).size());
    Assert.assertEquals(1, queues.get(new TeamDraftOrder(1)).get(0).getPlayerId());
    Assert.assertEquals(0, queues.get(new TeamDraftOrder(1)).get(1).getPlayerId());
    handler.enqueue(new TeamDraftOrder(1), 2, 2);
    Assert.assertEquals(3, queues.get(new TeamDraftOrder(1)).size());
    Assert.assertEquals(1, queues.get(new TeamDraftOrder(1)).get(0).getPlayerId());
    Assert.assertEquals(0, queues.get(new TeamDraftOrder(1)).get(1).getPlayerId());
    Assert.assertEquals(2, queues.get(new TeamDraftOrder(1)).get(2).getPlayerId());
  }

  @Test
  public void testEnqueueSelectedPlayer() throws DataSourceException {
    picks.add(DraftStatusTestUtil.createDraftPick(2, "", false, "P", 8, beanFactory));
    handler.enqueue(new TeamDraftOrder(1), 8, 0);
    Assert.assertEquals(0, queues.get(new TeamDraftOrder(1)).size());
  }

  @Test
  public void testReEnqueueSelectedPlayer() throws DataSourceException {
    handler.enqueue(new TeamDraftOrder(1), 8, 0);
    picks.add(DraftStatusTestUtil.createDraftPick(2, "", false, "P", 8, beanFactory));
    handler.enqueue(new TeamDraftOrder(1), 8, 0);
    Assert.assertEquals(0, queues.get(new TeamDraftOrder(1)).size());
  }

  @Test
  public void testDequeuePlayer() {
    queues.put(new TeamDraftOrder(1), createQueueEntry(0));
    handler.dequeue(new TeamDraftOrder(1), 0);
    Assert.assertEquals(0, queues.get(new TeamDraftOrder(1)).size());
  }

  @Test
  public void testReorderQueue() {
    List<QueueEntry> queue = new ArrayList<>();
    queue.add(createQueueEntry(0));
    queue.add(createQueueEntry(1));
    queue.add(createQueueEntry(2));
    queue.add(createQueueEntry(3));
    queue.add(createQueueEntry(4));
    queues.putAll(new TeamDraftOrder(1), queue);
    // first -> middle
    handler.reorderQueue(new TeamDraftOrder(1), 0, 3);
    Assert.assertEquals(1, queues.get(new TeamDraftOrder(1)).get(0).getPlayerId());
    Assert.assertEquals(2, queues.get(new TeamDraftOrder(1)).get(1).getPlayerId());
    Assert.assertEquals(0, queues.get(new TeamDraftOrder(1)).get(2).getPlayerId());
    Assert.assertEquals(3, queues.get(new TeamDraftOrder(1)).get(3).getPlayerId());
    Assert.assertEquals(4, queues.get(new TeamDraftOrder(1)).get(4).getPlayerId());
    // first -> last
    handler.reorderQueue(new TeamDraftOrder(1), 1, 5);
    Assert.assertEquals(2, queues.get(new TeamDraftOrder(1)).get(0).getPlayerId());
    Assert.assertEquals(0, queues.get(new TeamDraftOrder(1)).get(1).getPlayerId());
    Assert.assertEquals(3, queues.get(new TeamDraftOrder(1)).get(2).getPlayerId());
    Assert.assertEquals(4, queues.get(new TeamDraftOrder(1)).get(3).getPlayerId());
    Assert.assertEquals(1, queues.get(new TeamDraftOrder(1)).get(4).getPlayerId());
    // middle -> first
    handler.reorderQueue(new TeamDraftOrder(1), 4, 0);
    Assert.assertEquals(4, queues.get(new TeamDraftOrder(1)).get(0).getPlayerId());
    Assert.assertEquals(2, queues.get(new TeamDraftOrder(1)).get(1).getPlayerId());
    Assert.assertEquals(0, queues.get(new TeamDraftOrder(1)).get(2).getPlayerId());
    Assert.assertEquals(3, queues.get(new TeamDraftOrder(1)).get(3).getPlayerId());
    Assert.assertEquals(1, queues.get(new TeamDraftOrder(1)).get(4).getPlayerId());
    // middle -> last
    handler.reorderQueue(new TeamDraftOrder(1), 0, 5);
    Assert.assertEquals(4, queues.get(new TeamDraftOrder(1)).get(0).getPlayerId());
    Assert.assertEquals(2, queues.get(new TeamDraftOrder(1)).get(1).getPlayerId());
    Assert.assertEquals(3, queues.get(new TeamDraftOrder(1)).get(2).getPlayerId());
    Assert.assertEquals(1, queues.get(new TeamDraftOrder(1)).get(3).getPlayerId());
    Assert.assertEquals(0, queues.get(new TeamDraftOrder(1)).get(4).getPlayerId());
    // last -> first
    handler.reorderQueue(new TeamDraftOrder(1), 0, 0);
    Assert.assertEquals(0, queues.get(new TeamDraftOrder(1)).get(0).getPlayerId());
    Assert.assertEquals(4, queues.get(new TeamDraftOrder(1)).get(1).getPlayerId());
    Assert.assertEquals(2, queues.get(new TeamDraftOrder(1)).get(2).getPlayerId());
    Assert.assertEquals(3, queues.get(new TeamDraftOrder(1)).get(3).getPlayerId());
    Assert.assertEquals(1, queues.get(new TeamDraftOrder(1)).get(4).getPlayerId());
    // last -> middle
    handler.reorderQueue(new TeamDraftOrder(1), 1, 1);
    Assert.assertEquals(0, queues.get(new TeamDraftOrder(1)).get(0).getPlayerId());
    Assert.assertEquals(1, queues.get(new TeamDraftOrder(1)).get(1).getPlayerId());
    Assert.assertEquals(4, queues.get(new TeamDraftOrder(1)).get(2).getPlayerId());
    Assert.assertEquals(2, queues.get(new TeamDraftOrder(1)).get(3).getPlayerId());
    Assert.assertEquals(3, queues.get(new TeamDraftOrder(1)).get(4).getPlayerId());
    // noop
    handler.reorderQueue(new TeamDraftOrder(1), 2, 3);
    Assert.assertEquals(0, queues.get(new TeamDraftOrder(1)).get(0).getPlayerId());
    Assert.assertEquals(1, queues.get(new TeamDraftOrder(1)).get(1).getPlayerId());
    Assert.assertEquals(4, queues.get(new TeamDraftOrder(1)).get(2).getPlayerId());
    Assert.assertEquals(2, queues.get(new TeamDraftOrder(1)).get(3).getPlayerId());
    Assert.assertEquals(3, queues.get(new TeamDraftOrder(1)).get(4).getPlayerId());
  }

  private QueueEntry createQueueEntry(long playerId) {
    QueueEntry queueEntry = beanFactory.createQueueEntry().as();
    queueEntry.setPlayerId(playerId);
    return queueEntry;
  }
}