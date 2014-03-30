package com.mayhew3.drafttower.server;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.shared.*;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;

/**
 * Tests for {@link DraftController}.
 */
public class DraftControllerTest {

  private DraftController draftController;
  private DraftStatus draftStatus;
  private ListMultimap<TeamDraftOrder, Integer> keepers;
  private ListMultimap<TeamDraftOrder, QueueEntry> queues;
  private BeanFactory beanFactory;

  @Before
  public void setUp() throws Exception {
    beanFactory = AutoBeanFactorySource.create(BeanFactory.class);
    draftStatus = beanFactory.createDraftStatus().as();
    keepers = ArrayListMultimap.create();
    queues = ArrayListMultimap.create();
    draftController = new DraftController(
        Mockito.mock(DraftTowerWebSocketServlet.class),
        beanFactory,
        Mockito.mock(PlayerDataSource.class),
        Mockito.mock(TeamDataSource.class),
        Mockito.mock(DraftTimer.class),
        draftStatus,
        new HashMap<String, TeamDraftOrder>(),
        keepers,
        queues,
        new HashMap<TeamDraftOrder, PlayerDataSet>(),
        10);
  }

  @Test
  public void testBackOutLastPickSkipsKeepers() throws Exception {
    reset();
    List<DraftPick> picks = Lists.newArrayList(
        beanFactory.createDraftPick().as(),
        beanFactory.createDraftPick().as(),
        beanFactory.createDraftPick().as(),
        beanFactory.createDraftPick().as());
    draftStatus.setPicks(picks);
    draftStatus.setCurrentTeam(5);
    keepers.put(new TeamDraftOrder(4), 0);
    draftController.backOutLastPick();
    Assert.assertEquals(3, draftStatus.getCurrentTeam());
    Assert.assertEquals(2, draftStatus.getPicks().size());
  }

  @Test
  public void testAutoPickSetsRobotMode() throws Exception {
    reset();
    draftController.timerExpired();
    Assert.assertTrue(draftStatus.getRobotTeams().contains(1));
  }

  @Test
  public void testAutoPickFromQueueDoesNotSetRobotMode() throws Exception {
    reset();
    QueueEntry queueEntry = beanFactory.createQueueEntry().as();
    queueEntry.setPlayerId(0);
    queues.put(new TeamDraftOrder(1), queueEntry);
    draftController.timerExpired();
    Assert.assertFalse(draftStatus.getRobotTeams().contains(1));
  }

  private void reset() {
    draftStatus.getPicks().clear();
    draftStatus.setCurrentTeam(1);
    draftStatus.getRobotTeams().clear();
    keepers.clear();
    queues.clear();
  }
}