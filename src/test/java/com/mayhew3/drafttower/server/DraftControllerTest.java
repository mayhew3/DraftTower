package com.mayhew3.drafttower.server;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.shared.*;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Tests for {@link DraftControllerImpl}.
 */
public class DraftControllerTest {

  private DraftStatus draftStatus;
  private ListMultimap<TeamDraftOrder, Integer> keepers;
  private ListMultimap<TeamDraftOrder, QueueEntry> queues;
  private PlayerDataSource playerDataSource;
  private BeanFactory beanFactory;
  private List<DraftPick> picks;

  @Before
  public void setUp() throws Exception {
    beanFactory = AutoBeanFactorySource.create(BeanFactory.class);
    draftStatus = beanFactory.createDraftStatus().as();
    keepers = ArrayListMultimap.create();
    queues = ArrayListMultimap.create();
    picks = new ArrayList<>();
    playerDataSource = Mockito.mock(PlayerDataSource.class);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        draftStatus.setPicks(picks);
        return null;
      }
    }).when(playerDataSource).populateDraftStatus(Mockito.<DraftStatus>any());
    Mockito.when(playerDataSource.getBestPlayerId(
        Mockito.<PlayerDataSet>any(),
        Mockito.<TeamDraftOrder>any(),
        Mockito.anySetOf(Position.class)))
        .thenAnswer(new Answer<Long>() {
          @Override
          public Long answer(InvocationOnMock invocation) throws Throwable {
            return (long) picks.size();
          }
        });
  }

  private DraftControllerImpl createDraftController() throws DataSourceException {
    return new DraftControllerImpl(
        Mockito.mock(DraftTowerWebSocketServlet.class),
        beanFactory,
        playerDataSource,
        Mockito.mock(TeamDataSource.class),
        Mockito.mock(DraftTimer.class),
        draftStatus,
        new LockImpl(),
        new HashMap<String, TeamDraftOrder>(),
        keepers,
        queues,
        new HashMap<TeamDraftOrder, PlayerDataSet>(),
        10);
  }

  @Test
  public void testFirstTeamCurrentWhenConstructingAfterFullRound() throws Exception{
    reset();
    picks = createPicksList(10);
    DraftControllerImpl draftController = createDraftController();
    Assert.assertEquals(1, draftStatus.getCurrentTeam());
  }

  @Test
  public void testNextPickKeeperTeamsWhenConstructingFirstRoundStart() throws Exception{
    reset();
    keepers.put(new TeamDraftOrder(4), 3);
    keepers.put(new TeamDraftOrder(6), 5);
    keepers.put(new TeamDraftOrder(6), 15);
    DraftControllerImpl draftController = createDraftController();
    Assert.assertEquals(Sets.newHashSet(4, 6), draftStatus.getNextPickKeeperTeams());
  }

  @Test
  public void testNextPickKeeperTeamsWhenConstructingFirstRoundMiddle() throws Exception{
    reset();
    picks = createPicksList(4);
    keepers.put(new TeamDraftOrder(4), 3);
    keepers.put(new TeamDraftOrder(6), 5);
    keepers.put(new TeamDraftOrder(6), 15);
    DraftControllerImpl draftController = createDraftController();
    Assert.assertEquals(Sets.newHashSet(6), draftStatus.getNextPickKeeperTeams());
  }

  @Test
  public void testNextPickKeeperTeamsWhenConstructingSecondRound() throws Exception{
    reset();
    picks = createPicksList(10);
    keepers.put(new TeamDraftOrder(4), 3);
    keepers.put(new TeamDraftOrder(6), 5);
    keepers.put(new TeamDraftOrder(6), 15);
    DraftControllerImpl draftController = createDraftController();
    Assert.assertEquals(Sets.newHashSet(6), draftStatus.getNextPickKeeperTeams());
  }

  @Test
  public void testNextPickKeeperTeamsWhenConstructingThirdRound() throws Exception{
    reset();
    picks = createPicksList(20);
    keepers.put(new TeamDraftOrder(2), 1);
    keepers.put(new TeamDraftOrder(4), 3);
    keepers.put(new TeamDraftOrder(4), 13);
    keepers.put(new TeamDraftOrder(6), 5);
    keepers.put(new TeamDraftOrder(6), 15);
    keepers.put(new TeamDraftOrder(6), 25);
    DraftControllerImpl draftController = createDraftController();
    Assert.assertEquals(Sets.newHashSet(6), draftStatus.getNextPickKeeperTeams());
  }

  @Test
  public void testNextPickKeeperTeamsWhenConstructingFourthRound() throws Exception{
    reset();
    picks = createPicksList(30);
    keepers.put(new TeamDraftOrder(4), 3);
    keepers.put(new TeamDraftOrder(6), 5);
    keepers.put(new TeamDraftOrder(6), 15);
    keepers.put(new TeamDraftOrder(6), 25);
    DraftControllerImpl draftController = createDraftController();
    Assert.assertEquals(Sets.<Integer>newHashSet(), draftStatus.getNextPickKeeperTeams());
  }

  @Test
  public void testBackOutLastPick() throws Exception {
    reset();
    picks = createPicksList(4);
    DraftControllerImpl draftController = createDraftController();
    draftController.backOutLastPick();
    Assert.assertEquals(4, draftStatus.getCurrentTeam());
    Assert.assertEquals(3, draftStatus.getPicks().size());
  }

  @Test
  public void testBackOutLastPickByFirstTeam() throws Exception {
    reset();
    picks = createPicksList(10);
    DraftControllerImpl draftController = createDraftController();
    draftController.backOutLastPick();
    Assert.assertEquals(10, draftStatus.getCurrentTeam());
    Assert.assertEquals(9, draftStatus.getPicks().size());
  }

  @Test
  public void testBackOutLastPickStaysPaused() throws Exception {
    reset();
    draftStatus.setPaused(true);
    picks = createPicksList(4);
    DraftControllerImpl draftController = createDraftController();
    draftController.backOutLastPick();
    Assert.assertTrue(draftStatus.isPaused());
  }

  @Test
  public void testBackOutLastPickSkipsKeepers() throws Exception {
    reset();
    picks = createPicksList(4);
    keepers.put(new TeamDraftOrder(4), 0);
    DraftControllerImpl draftController = createDraftController();
    draftController.backOutLastPick();
    Assert.assertEquals(3, draftStatus.getCurrentTeam());
    Assert.assertEquals(2, draftStatus.getPicks().size());
  }

  @Test
  public void testAutoPickNoQueueSetsRobotMode() throws Exception {
    reset();
    DraftControllerImpl draftController = createDraftController();
    draftController.timerExpired();
    Assert.assertTrue(draftStatus.getRobotTeams().contains(1));
  }

  @Test
  public void testAutoPickFromQueueDoesNotSetRobotMode() throws Exception {
    reset();
    QueueEntry queueEntry = beanFactory.createQueueEntry().as();
    queueEntry.setPlayerId(0);
    queues.put(new TeamDraftOrder(1), queueEntry);
    DraftControllerImpl draftController = createDraftController();
    draftController.timerExpired();
    Assert.assertFalse(draftStatus.getRobotTeams().contains(1));
  }

  @Test
  public void testPickAdvancesFromLastTeamToFirstTeam() throws Exception {
    reset();
    picks = createPicksList(9);
    DraftControllerImpl draftController = createDraftController();
    draftController.doPick(new TeamDraftOrder(10), 9, false, false);
    Assert.assertEquals(1, draftStatus.getCurrentTeam());
  }

  @Test
  public void testInvalidPickDoesNotAdvanceTeam() throws Exception {
    reset();
    picks = createPicksList(9);
    DraftControllerImpl draftController = createDraftController();
    draftController.doPick(new TeamDraftOrder(10), 8, false, false);
    Assert.assertEquals(10, draftStatus.getCurrentTeam());
  }

  @Test
  public void testPickPicksFollowingKeeper() throws Exception {
    reset();
    picks = createPicksList(2);
    keepers.put(new TeamDraftOrder(4), 3);
    DraftControllerImpl draftController = createDraftController();
    draftController.doPick(new TeamDraftOrder(3), 2, false, false);
    Assert.assertEquals(5, draftStatus.getCurrentTeam());
    Assert.assertEquals(3, draftStatus.getPicks().get(3).getPlayerId());
  }

  @Test
  public void testPickIgnoresKeeperLaterRounds() throws Exception {
    reset();
    picks = createPicksList(12);
    keepers.put(new TeamDraftOrder(4), 3);
    DraftControllerImpl draftController = createDraftController();
    draftController.doPick(new TeamDraftOrder(3), 12, false, false);
    Assert.assertEquals(4, draftStatus.getCurrentTeam());
  }

  @Test
  public void testPickRemovesPlayerFromQueues() throws Exception {
    reset();
    TeamDraftOrder team3 = new TeamDraftOrder(3);
    TeamDraftOrder team6 = new TeamDraftOrder(6);
    queues.put(team3, createQueueEntry(0));
    queues.put(team3, createQueueEntry(1));
    queues.put(team6, createQueueEntry(0));
    DraftControllerImpl draftController = createDraftController();
    draftController.doPick(new TeamDraftOrder(1), 0, false, false);
    Assert.assertEquals(1, queues.get(team3).size());
    Assert.assertEquals(1, queues.get(team3).get(0).getPlayerId());
    Assert.assertEquals(0, queues.get(team6).size());
  }

  @Test
  public void testItsOver() throws Exception {
    reset();
    picks = createPicksList(219);
    DraftControllerImpl draftController = createDraftController();
    draftController.doPick(new TeamDraftOrder(10), 219, false, false);
    Assert.assertTrue(draftStatus.isOver());
  }

  private List<DraftPick> createPicksList(int numPicks) {
    List<DraftPick> picksList = new ArrayList<>();
    for (int i = 0; i < numPicks; i++) {
      DraftPick pick = beanFactory.createDraftPick().as();
      pick.setTeam((i % 10) + 1);
      pick.setPlayerId(i);
      pick.setEligibilities(Lists.newArrayList("P"));
      picksList.add(pick);
    }
    return picksList;
  }

  private QueueEntry createQueueEntry(int playerId) {
    QueueEntry queueEntry = beanFactory.createQueueEntry().as();
    queueEntry.setPlayerId(playerId);
    return queueEntry;
  }

  private void reset() {
    keepers.clear();
    queues.clear();
    picks.clear();
  }
}