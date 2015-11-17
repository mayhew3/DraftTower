package com.mayhew3.drafttower.server;

import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.shared.*;
import com.mayhew3.drafttower.shared.CurrentTimeProvider.FakeCurrentTimeProvider;
import com.mayhew3.drafttower.shared.DraftCommand.Command;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.*;

/**
 * Tests for {@link DraftControllerImpl}.
 */
public class DraftControllerTest {

  private DraftStatus draftStatus;
  private ListMultimap<TeamDraftOrder, Integer> keepers;
  private ListMultimap<TeamDraftOrder, QueueEntry> queues;
  private PlayerDataProvider playerDataSource;
  private BeanFactory beanFactory;
  private List<DraftPick> picks;
  private FakeCurrentTimeProvider currentTimeProvider;
  private DraftTowerWebSocketServlet socketServlet;

  @Before
  public void setUp() throws Exception {
    beanFactory = AutoBeanFactorySource.create(BeanFactory.class);
    draftStatus = beanFactory.createDraftStatus().as();
    keepers = ArrayListMultimap.create();
    queues = ArrayListMultimap.create();
    picks = new ArrayList<>();
    playerDataSource = Mockito.mock(PlayerDataProvider.class);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        draftStatus.setPicks(picks);
        return null;
      }
    }).when(playerDataSource).populateDraftStatus(Mockito.<DraftStatus>any());
    Mockito.when(playerDataSource.getBestPlayerId(
        Mockito.<TeamDraftOrder>any(),
        Mockito.anyListOf(DraftPick.class),
        Mockito.<EnumSet<Position>>any(),
        Mockito.<Map<Long,Float>>any()))
        .thenAnswer(new Answer<Long>() {
          @Override
          public Long answer(InvocationOnMock invocation) throws Throwable {
            return (long) picks.size();
          }
        });
    currentTimeProvider = new FakeCurrentTimeProvider();
    currentTimeProvider.setCurrentTimeMillis(1000);
    socketServlet = Mockito.mock(DraftTowerWebSocketServlet.class);
  }

  private DraftControllerImpl createDraftController() throws DataSourceException {
    HashMap<String, TeamDraftOrder> teamTokens = new HashMap<>();
    teamTokens.put("1", new TeamDraftOrder(1));
    teamTokens.put("2", new TeamDraftOrder(2));
    TeamDataSource teamDataSource = Mockito.mock(TeamDataSource.class);
    Mockito.when(teamDataSource.isCommissionerTeam(Mockito.eq(new TeamDraftOrder(1))))
        .thenReturn(true);
    Mockito.when(teamDataSource.isCommissionerTeam(Mockito.eq(new TeamDraftOrder(2))))
        .thenReturn(false);
    PickProbabilityPredictor pickProbabilityPredictor = new PickProbabilityPredictor(
        playerDataSource, teamDataSource, beanFactory, RosterTestUtils.createSimpleFakeRosterUtil(), new TestPredictionModel());
    DraftControllerImpl draftController = new DraftControllerImpl(
        socketServlet,
        beanFactory,
        playerDataSource,
        pickProbabilityPredictor, 
        teamDataSource,
        currentTimeProvider,
        Mockito.mock(DraftTimer.class),
        draftStatus,
        new LockImpl(),
        new RosterUtil(),
        teamTokens,
        keepers,
        queues,
        10);
    Mockito.reset(socketServlet);
    return draftController;
  }

  @Test
  public void testFirstTeamCurrentWhenConstructingAfterFullRound() throws Exception{
    picks = createPicksList(10);
    createDraftController();
    Assert.assertEquals(1, draftStatus.getCurrentTeam());
  }

  @Test
  public void testNextPickKeeperTeamsWhenConstructingFirstRoundStart() throws Exception{
    keepers.put(new TeamDraftOrder(4), 3);
    keepers.put(new TeamDraftOrder(6), 5);
    keepers.put(new TeamDraftOrder(6), 15);
    createDraftController();
    Assert.assertEquals(Sets.newHashSet(4, 6), draftStatus.getNextPickKeeperTeams());
  }

  @Test
  public void testNextPickKeeperTeamsWhenConstructingFirstRoundMiddle() throws Exception{
    picks = createPicksList(4);
    keepers.put(new TeamDraftOrder(4), 3);
    keepers.put(new TeamDraftOrder(6), 5);
    keepers.put(new TeamDraftOrder(6), 15);
    createDraftController();
    Assert.assertEquals(Sets.newHashSet(6), draftStatus.getNextPickKeeperTeams());
  }

  @Test
  public void testNextPickKeeperTeamsWhenConstructingSecondRound() throws Exception{
    picks = createPicksList(10);
    keepers.put(new TeamDraftOrder(4), 3);
    keepers.put(new TeamDraftOrder(6), 5);
    keepers.put(new TeamDraftOrder(6), 15);
    createDraftController();
    Assert.assertEquals(Sets.newHashSet(6), draftStatus.getNextPickKeeperTeams());
  }

  @Test
  public void testNextPickKeeperTeamsWhenConstructingThirdRound() throws Exception{
    picks = createPicksList(20);
    keepers.put(new TeamDraftOrder(2), 1);
    keepers.put(new TeamDraftOrder(4), 3);
    keepers.put(new TeamDraftOrder(4), 13);
    keepers.put(new TeamDraftOrder(6), 5);
    keepers.put(new TeamDraftOrder(6), 15);
    keepers.put(new TeamDraftOrder(6), 25);
    createDraftController();
    Assert.assertEquals(Sets.newHashSet(6), draftStatus.getNextPickKeeperTeams());
  }

  @Test
  public void testNextPickKeeperTeamsWhenConstructingFourthRound() throws Exception{
    picks = createPicksList(30);
    keepers.put(new TeamDraftOrder(4), 3);
    keepers.put(new TeamDraftOrder(6), 5);
    keepers.put(new TeamDraftOrder(6), 15);
    keepers.put(new TeamDraftOrder(6), 25);
    createDraftController();
    Assert.assertEquals(Sets.<Integer>newHashSet(), draftStatus.getNextPickKeeperTeams());
  }

  @Test
  public void testBackOutLastPick() throws Exception {
    picks = createPicksList(4);
    DraftControllerImpl draftController = createDraftController();
    draftController.backOutLastPick();
    Assert.assertEquals(4, draftStatus.getCurrentTeam());
    Assert.assertEquals(3, draftStatus.getPicks().size());
  }

  @Test
  public void testBackOutLastPickByFirstTeam() throws Exception {
    picks = createPicksList(10);
    DraftControllerImpl draftController = createDraftController();
    draftController.backOutLastPick();
    Assert.assertEquals(10, draftStatus.getCurrentTeam());
    Assert.assertEquals(9, draftStatus.getPicks().size());
  }

  @Test
  public void testBackOutLastPickStaysPaused() throws Exception {
    draftStatus.setPaused(true);
    picks = createPicksList(4);
    DraftControllerImpl draftController = createDraftController();
    draftController.backOutLastPick();
    Assert.assertTrue(draftStatus.isPaused());
  }

  @Test
  public void testBackOutLastPickSkipsKeepers() throws Exception {
    picks = createPicksList(4);
    keepers.put(new TeamDraftOrder(4), 0);
    DraftControllerImpl draftController = createDraftController();
    draftController.backOutLastPick();
    Assert.assertEquals(3, draftStatus.getCurrentTeam());
    Assert.assertEquals(2, draftStatus.getPicks().size());
  }

  @Test
  public void testBackOutLastPickNoPicks() throws Exception {
    DraftControllerImpl draftController = createDraftController();
    draftController.backOutLastPick();
    Assert.assertEquals(1, draftStatus.getCurrentTeam());
    Assert.assertEquals(0, draftStatus.getPicks().size());
  }

  @Test
  public void testBackOutFirstPickKeeperNoOp() throws Exception {
    picks = createPicksList(1);
    keepers.put(new TeamDraftOrder(1), 0);
    DraftControllerImpl draftController = createDraftController();
    draftController.backOutLastPick();
    Assert.assertEquals(2, draftStatus.getCurrentTeam());
    Assert.assertEquals(1, draftStatus.getPicks().size());
  }

  @Test
  public void testAutoPickNoQueueSetsRobotMode() throws Exception {
    DraftControllerImpl draftController = createDraftController();
    draftController.timerExpired();
    Assert.assertTrue(draftStatus.getRobotTeams().contains(1));
  }

  @Test
  public void testAutoPickFromQueueDoesNotSetRobotMode() throws Exception {
    QueueEntry queueEntry = beanFactory.createQueueEntry().as();
    queueEntry.setPlayerId(0);
    queues.put(new TeamDraftOrder(1), queueEntry);
    DraftControllerImpl draftController = createDraftController();
    draftController.timerExpired();
    Assert.assertFalse(draftStatus.getRobotTeams().contains(1));
  }

  @Test
  public void testPickAdvancesFromLastTeamToFirstTeam() throws Exception {
    picks = createPicksList(9);
    DraftControllerImpl draftController = createDraftController();
    draftController.doPick(new TeamDraftOrder(10), 9, false, false);
    Assert.assertEquals(1, draftStatus.getCurrentTeam());
  }

  @Test
  public void testInvalidPickDoesNotAdvanceTeam() throws Exception {
    picks = createPicksList(9);
    DraftControllerImpl draftController = createDraftController();
    draftController.doPick(new TeamDraftOrder(10), 8, false, false);
    Assert.assertEquals(10, draftStatus.getCurrentTeam());
  }

  @Test
  public void testPickPicksFollowingKeeper() throws Exception {
    picks = createPicksList(2);
    keepers.put(new TeamDraftOrder(4), 3);
    DraftControllerImpl draftController = createDraftController();
    draftController.doPick(new TeamDraftOrder(3), 2, false, false);
    Assert.assertEquals(5, draftStatus.getCurrentTeam());
    Assert.assertEquals(3, draftStatus.getPicks().get(3).getPlayerId());
  }

  @Test
  public void testPickIgnoresKeeperLaterRounds() throws Exception {
    picks = createPicksList(12);
    keepers.put(new TeamDraftOrder(4), 3);
    DraftControllerImpl draftController = createDraftController();
    draftController.doPick(new TeamDraftOrder(3), 12, false, false);
    Assert.assertEquals(4, draftStatus.getCurrentTeam());
  }

  @Test
  public void testPickRemovesPlayerFromQueues() throws Exception {
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
    picks = createPicksList(219);
    DraftControllerImpl draftController = createDraftController();
    draftController.doPick(new TeamDraftOrder(10), 219, false, false);
    Assert.assertTrue(draftStatus.isOver());
  }

  @Test
  public void testOnDraftCommandBadTeamToken() throws Exception {
    DraftControllerImpl draftController = createDraftController();
    DraftCommand draftCommand = beanFactory.createDraftCommand().as();
    draftCommand.setTeamToken("bad");
    try {
      draftController.onDraftCommand(draftCommand);
    } catch (TerminateSocketException e) {
      // expected
      Mockito.verifyZeroInteractions(socketServlet);
      return;
    }
    Assert.fail("No exception thrown");
  }

  @Test
  public void testOnDraftCommandNonCommish() throws Exception {
    DraftControllerImpl draftController = createDraftController();
    DraftCommand draftCommand = beanFactory.createDraftCommand().as();
    draftCommand.setTeamToken("2");
    draftCommand.setCommandType(Command.START_DRAFT);
    draftController.onDraftCommand(draftCommand);
    Assert.assertEquals(0, draftStatus.getCurrentPickDeadline());
    Mockito.verifyZeroInteractions(socketServlet);
  }

  @Test
  public void testOnDraftCommandIdentifyAlreadyConnectedTeam() throws Exception {
    DraftControllerImpl draftController = createDraftController();
    draftStatus.getConnectedTeams().add(1);
    DraftCommand draftCommand = beanFactory.createDraftCommand().as();
    draftCommand.setTeamToken("1");
    draftCommand.setCommandType(Command.IDENTIFY);
    try {
      draftController.onDraftCommand(draftCommand);
    } catch (TerminateSocketException e) {
      // expected
      Mockito.verifyZeroInteractions(socketServlet);
      return;
    }
    Assert.fail("No exception thrown");
  }

  @Test
  public void testOnDraftCommandIdentifyAddsToConnectedTeams() throws Exception {
    DraftControllerImpl draftController = createDraftController();
    DraftCommand draftCommand = beanFactory.createDraftCommand().as();
    draftCommand.setTeamToken("1");
    draftCommand.setCommandType(Command.IDENTIFY);
    draftController.onDraftCommand(draftCommand);
    Assert.assertTrue(draftStatus.getConnectedTeams().contains(1));
    Mockito.verify(socketServlet).sendMessage(Mockito.<Function<String,String>>any());
  }

  @Test
  public void testOnDraftCommandIdentifyRemovesFromRobotTeams() throws Exception {
    DraftControllerImpl draftController = createDraftController();
    draftStatus.getRobotTeams().add(1);
    DraftCommand draftCommand = beanFactory.createDraftCommand().as();
    draftCommand.setTeamToken("1");
    draftCommand.setCommandType(Command.IDENTIFY);
    draftController.onDraftCommand(draftCommand);
    Assert.assertFalse(draftStatus.getRobotTeams().contains(1));
    Mockito.verify(socketServlet).sendMessage(Mockito.<Function<String,String>>any());
  }

  @Test
  public void testOnDraftCommandStartDraft() throws Exception {
    DraftControllerImpl draftController = createDraftController();
    DraftCommand draftCommand = beanFactory.createDraftCommand().as();
    draftCommand.setTeamToken("1");
    draftCommand.setCommandType(Command.START_DRAFT);
    draftController.onDraftCommand(draftCommand);
    Assert.assertEquals(76000, draftStatus.getCurrentPickDeadline());
    Mockito.verify(socketServlet).sendMessage(Mockito.<Function<String,String>>any());
  }

  @Test
  public void testOnDraftCommandRobotTime() throws Exception {
    DraftControllerImpl draftController = createDraftController();
    draftStatus.getRobotTeams().add(1);
    DraftCommand draftCommand = beanFactory.createDraftCommand().as();
    draftCommand.setTeamToken("1");
    draftCommand.setCommandType(Command.START_DRAFT);
    draftController.onDraftCommand(draftCommand);
    Assert.assertEquals(8000, draftStatus.getCurrentPickDeadline());
  }

  @Test
  public void testOnDraftCommandDoPick() throws Exception {
    picks = createPicksList(1);
    DraftControllerImpl draftController = createDraftController();
    DraftCommand draftCommand = beanFactory.createDraftCommand().as();
    draftCommand.setTeamToken("2");
    draftCommand.setCommandType(Command.DO_PICK);
    draftCommand.setPlayerId(100l);
    draftController.onDraftCommand(draftCommand);
    Assert.assertEquals(2, draftStatus.getPicks().size());
    Assert.assertEquals(100l, draftStatus.getPicks().get(1).getPlayerId());
    Mockito.verify(socketServlet).sendMessage(Mockito.<Function<String,String>>any());
  }

  @Test
  public void testOnDraftCommandPauseResume() throws Exception {
    DraftControllerImpl draftController = createDraftController();
    draftStatus.setCurrentPickDeadline(76000);
    DraftCommand draftCommand = beanFactory.createDraftCommand().as();
    draftCommand.setTeamToken("1");
    draftCommand.setCommandType(Command.PAUSE);
    draftController.onDraftCommand(draftCommand);
    Assert.assertTrue(draftStatus.isPaused());
    Mockito.verify(socketServlet).sendMessage(Mockito.<Function<String,String>>any());

    currentTimeProvider.setCurrentTimeMillis(5000);

    draftCommand = beanFactory.createDraftCommand().as();
    draftCommand.setTeamToken("1");
    draftCommand.setCommandType(Command.RESUME);
    draftController.onDraftCommand(draftCommand);
    Assert.assertFalse(draftStatus.isPaused());
    Assert.assertEquals(80000, draftStatus.getCurrentPickDeadline());
    Mockito.verify(socketServlet, Mockito.times(2))
        .sendMessage(Mockito.<Function<String,String>>any());
  }

  @Test
  public void testOnDraftCommandBackOut() throws Exception {
    picks = createPicksList(1);
    DraftControllerImpl draftController = createDraftController();
    DraftCommand draftCommand = beanFactory.createDraftCommand().as();
    draftCommand.setTeamToken("1");
    draftCommand.setCommandType(Command.BACK_OUT);
    draftController.onDraftCommand(draftCommand);
    Assert.assertEquals(0, draftStatus.getPicks().size());
    Mockito.verify(socketServlet).sendMessage(Mockito.<Function<String,String>>any());
  }

  @Test
  public void testOnDraftCommandForcePickAuto() throws Exception {
    picks = createPicksList(12);
    DraftControllerImpl draftController = createDraftController();
    DraftCommand draftCommand = beanFactory.createDraftCommand().as();
    draftCommand.setTeamToken("1");
    draftCommand.setCommandType(Command.FORCE_PICK);
    draftController.onDraftCommand(draftCommand);
    Assert.assertEquals(13, draftStatus.getPicks().size());
    Assert.assertEquals(12l, draftStatus.getPicks().get(12).getPlayerId());
    Mockito.verify(socketServlet).sendMessage(Mockito.<Function<String,String>>any());
  }

  @Test
  public void testOnDraftCommandForcePickQueue() throws Exception {
    picks = createPicksList(1);
    QueueEntry queueEntry = beanFactory.createQueueEntry().as();
    queueEntry.setPlayerId(100l);
    queues.put(new TeamDraftOrder(2), queueEntry);
    DraftControllerImpl draftController = createDraftController();
    DraftCommand draftCommand = beanFactory.createDraftCommand().as();
    draftCommand.setTeamToken("1");
    draftCommand.setCommandType(Command.FORCE_PICK);
    draftController.onDraftCommand(draftCommand);
    Assert.assertEquals(2, draftStatus.getPicks().size());
    Assert.assertEquals(100l, draftStatus.getPicks().get(1).getPlayerId());
    Mockito.verify(socketServlet).sendMessage(Mockito.<Function<String,String>>any());
  }

  @Test
  public void testOnDraftCommandForcePickManual() throws Exception {
    picks = createPicksList(1);
    DraftControllerImpl draftController = createDraftController();
    DraftCommand draftCommand = beanFactory.createDraftCommand().as();
    draftCommand.setTeamToken("1");
    draftCommand.setCommandType(Command.FORCE_PICK);
    draftCommand.setPlayerId(100l);
    draftController.onDraftCommand(draftCommand);
    Assert.assertEquals(2, draftStatus.getPicks().size());
    Assert.assertEquals(100l, draftStatus.getPicks().get(1).getPlayerId());
    Mockito.verify(socketServlet).sendMessage(Mockito.<Function<String,String>>any());
  }

  @Test
  public void testOnDraftCommandWakeUp() throws Exception {
    DraftControllerImpl draftController = createDraftController();
    draftStatus.getRobotTeams().add(2);
    DraftCommand draftCommand = beanFactory.createDraftCommand().as();
    draftCommand.setTeamToken("2");
    draftCommand.setCommandType(Command.WAKE_UP);
    draftController.onDraftCommand(draftCommand);
    Assert.assertFalse(draftStatus.getRobotTeams().contains(2));
    Mockito.verify(socketServlet).sendMessage(Mockito.<Function<String,String>>any());
  }

  @Test
  public void testOnDraftCommandDoPickAlreadyOver() throws Exception {
    picks = createPicksList(1);
    draftStatus.setOver(true);
    DraftControllerImpl draftController = createDraftController();
    DraftCommand draftCommand = beanFactory.createDraftCommand().as();
    draftCommand.setTeamToken("2");
    draftCommand.setCommandType(Command.DO_PICK);
    draftCommand.setPlayerId(100l);
    draftController.onDraftCommand(draftCommand);
    Assert.assertEquals(1, draftStatus.getPicks().size());
    Mockito.verifyZeroInteractions(socketServlet);
  }

  @Test
  public void testOnDraftCommandDoPickWrongTeam() throws Exception {
    picks = createPicksList(1);
    DraftControllerImpl draftController = createDraftController();
    DraftCommand draftCommand = beanFactory.createDraftCommand().as();
    draftCommand.setTeamToken("1");
    draftCommand.setCommandType(Command.DO_PICK);
    draftCommand.setPlayerId(100l);
    draftController.onDraftCommand(draftCommand);
    Assert.assertEquals(1, draftStatus.getPicks().size());
    Mockito.verifyZeroInteractions(socketServlet);
  }

  @Test
  public void testOnDraftCommandForcePickAlreadyOver() throws Exception {
    picks = createPicksList(1);
    draftStatus.setOver(true);
    DraftControllerImpl draftController = createDraftController();
    DraftCommand draftCommand = beanFactory.createDraftCommand().as();
    draftCommand.setTeamToken("1");
    draftCommand.setCommandType(Command.FORCE_PICK);
    draftController.onDraftCommand(draftCommand);
    Assert.assertEquals(1, draftStatus.getPicks().size());
    Mockito.verifyZeroInteractions(socketServlet);
  }

  @Test
  public void testOnDraftCommandResetDraft() throws Exception {
    picks = createPicksList(25);
    DraftControllerImpl draftController = createDraftController();
    DraftCommand draftCommand = beanFactory.createDraftCommand().as();
    draftCommand.setTeamToken("1");
    draftCommand.setCommandType(Command.RESET_DRAFT);
    draftController.onDraftCommand(draftCommand);
    Assert.assertEquals(0, draftStatus.getPicks().size());
    Assert.assertEquals(1, draftStatus.getCurrentTeam());
    Mockito.verify(socketServlet).sendMessage(Mockito.<Function<String,String>>any());
  }

  @Test
  public void testOnClientDisconnected() throws Exception {
    DraftControllerImpl draftController = createDraftController();
    draftStatus.getConnectedTeams().add(2);
    draftController.onClientDisconnected("2");
    Assert.assertFalse(draftStatus.getConnectedTeams().contains(2));
    Mockito.verify(socketServlet).sendMessage(Mockito.<Function<String,String>>any());
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
}