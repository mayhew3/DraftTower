package com.mayhew3.drafttower.server;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.guiceberry.junit4.GuiceBerryRule;
import com.google.inject.Inject;
import com.mayhew3.drafttower.server.BindingAnnotations.Keepers;
import com.mayhew3.drafttower.server.BindingAnnotations.Queues;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.DraftStatus;
import com.mayhew3.drafttower.shared.QueueEntry;
import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * Tests for {@link DraftController}.
 */
public class DraftControllerTest {

  @Rule
  public final GuiceBerryRule guiceBerry = new GuiceBerryRule(GuiceBerryEnv.class);

  @Inject private DraftController draftController;
  @Inject private DraftStatus draftStatus;
  @Inject @Keepers private ListMultimap<TeamDraftOrder, Integer> keepers;
  @Inject @Queues private ListMultimap<TeamDraftOrder, QueueEntry> queues;
  @Inject private BeanFactory beanFactory;

  @Test
  public void testBackOutLastPickSkipsKeepers() throws Exception {
    reset();
    draftStatus.setPicks(Lists.newArrayList(
        beanFactory.createDraftPick().as(),
        beanFactory.createDraftPick().as(),
        beanFactory.createDraftPick().as(),
        beanFactory.createDraftPick().as()));
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