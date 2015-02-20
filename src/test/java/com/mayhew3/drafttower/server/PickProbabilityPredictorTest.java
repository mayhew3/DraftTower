package com.mayhew3.drafttower.server;

import com.google.common.collect.Lists;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.shared.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Tests for {@link PickProbabilityPredictor}.
 */
public class PickProbabilityPredictorTest {

  private PickProbabilityPredictor pickProbabilityPredictor;

  private List<Player> players;
  private TestPlayerGenerator testPlayerGenerator;
  private BeanFactory beanFactory;

  @Before
  public void setUp() throws Exception {
    beanFactory = AutoBeanFactorySource.create(BeanFactory.class);

    testPlayerGenerator = new TestPlayerGenerator(beanFactory);
    players = new ArrayList<>();
    for (int i = 0; i < 20; i++) {
      players.add(testPlayerGenerator.generatePlayer(i, Position.C, i));
    }

    PlayerDataSource playerDataSource = Mockito.mock(PlayerDataSource.class);
    Mockito.when(playerDataSource.getPlayers(
        Mockito.any(TeamId.class), Mockito.any(TableSpec.class)))
        .thenReturn(players);
    TeamDataSource teamDataSource = Mockito.mock(TeamDataSource.class);
    Mockito.when(teamDataSource.getTeamIdByDraftOrder(Mockito.any(TeamDraftOrder.class)))
        .then(new Answer<TeamId>() {
          @Override
          public TeamId answer(InvocationOnMock invocation) throws Throwable {
            return new TeamId(((TeamDraftOrder) invocation.getArguments()[0]).get());
          }
        });
    DraftController draftController = Mockito.mock(DraftController.class);
    pickProbabilityPredictor = new PickProbabilityPredictor(
        playerDataSource,
        teamDataSource,
        draftController,
        beanFactory,
        new RosterUtil(),
        new TestPredictionModel());
  }

  @Test
  public void testEmptyDraftStatusPredictsFirstTeam() {
    pickProbabilityPredictor.onDraftStatusChanged(
        DraftStatusTestUtil.createDraftStatus(new ArrayList<DraftPick>(), beanFactory));
    Map<Long, Float> predictions = pickProbabilityPredictor.getTeamPredictions(new TeamDraftOrder(1));
    Assert.assertEquals(3, predictions.size());
    Assert.assertEquals(.9f, predictions.get(0l), 0.00001f);
    Assert.assertEquals(.8f, predictions.get(1l), 0.00001f);
    Assert.assertEquals(.7f, predictions.get(2l), 0.00001f);

    Assert.assertEquals(0, pickProbabilityPredictor.getTeamPredictions(new TeamDraftOrder(2)).size());
  }

  @Test
  public void testDraftStatusOnePickPredictsFirstTwoTeams() {
    pickProbabilityPredictor.onDraftStatusChanged(
        DraftStatusTestUtil.createDraftStatus(
            Lists.newArrayList(DraftStatusTestUtil.createDraftPick(1, "", false, "C", 0, beanFactory)),
            beanFactory));
    Map<Long, Float> predictions = pickProbabilityPredictor.getTeamPredictions(new TeamDraftOrder(1));
    Assert.assertEquals(3, predictions.size());
    Assert.assertEquals(.9f, predictions.get(0l), 0.00001f);
    Assert.assertEquals(.8f, predictions.get(1l), 0.00001f);
    Assert.assertEquals(.7f, predictions.get(2l), 0.00001f);

    predictions = pickProbabilityPredictor.getTeamPredictions(new TeamDraftOrder(2));
    Assert.assertEquals(3, predictions.size());
    Assert.assertEquals(.9101f, predictions.get(1l), 0.00001f);
    Assert.assertEquals(.8101f, predictions.get(2l), 0.00001f);
    Assert.assertEquals(.7101f, predictions.get(3l), 0.00001f);

    Assert.assertEquals(0, pickProbabilityPredictor.getTeamPredictions(new TeamDraftOrder(3)).size());
  }

  @Test
  public void testDraftStatusFullFirstRoundPredictsAllTeams() {
    pickProbabilityPredictor.onDraftStatusChanged(
        DraftStatusTestUtil.createDraftStatus(
            Lists.newArrayList(
                DraftStatusTestUtil.createDraftPick(1, "", false, "C", 0, beanFactory),
                DraftStatusTestUtil.createDraftPick(2, "", false, "C", 1, beanFactory),
                DraftStatusTestUtil.createDraftPick(3, "", false, "C", 2, beanFactory),
                DraftStatusTestUtil.createDraftPick(4, "", false, "C", 3, beanFactory),
                DraftStatusTestUtil.createDraftPick(5, "", false, "C", 4, beanFactory),
                DraftStatusTestUtil.createDraftPick(6, "", false, "C", 5, beanFactory),
                DraftStatusTestUtil.createDraftPick(7, "", false, "C", 6, beanFactory),
                DraftStatusTestUtil.createDraftPick(8, "", false, "C", 7, beanFactory),
                DraftStatusTestUtil.createDraftPick(9, "", false, "C", 8, beanFactory),
                DraftStatusTestUtil.createDraftPick(10, "", false, "C", 9, beanFactory)),
            beanFactory));
    Map<Long, Float> predictions = pickProbabilityPredictor.getTeamPredictions(new TeamDraftOrder(1));
    Assert.assertEquals(3, predictions.size());
    Assert.assertEquals(1.001f, predictions.get(10l), 0.00001f);
    Assert.assertEquals(.901f, predictions.get(11l), 0.00001f);
    Assert.assertEquals(.801f, predictions.get(12l), 0.00001f);

    predictions = pickProbabilityPredictor.getTeamPredictions(new TeamDraftOrder(2));
    Assert.assertEquals(3, predictions.size());
    Assert.assertEquals(.9101f, predictions.get(1l), 0.00001f);
    Assert.assertEquals(.8101f, predictions.get(2l), 0.00001f);
    Assert.assertEquals(.7101f, predictions.get(3l), 0.00001f);

    predictions = pickProbabilityPredictor.getTeamPredictions(new TeamDraftOrder(3));
    Assert.assertEquals(3, predictions.size());
    Assert.assertEquals(.9202f, predictions.get(2l), 0.00001f);
    Assert.assertEquals(.8202f, predictions.get(3l), 0.00001f);
    Assert.assertEquals(.7202f, predictions.get(4l), 0.00001f);

    predictions = pickProbabilityPredictor.getTeamPredictions(new TeamDraftOrder(4));
    Assert.assertEquals(3, predictions.size());
    Assert.assertEquals(.9303f, predictions.get(3l), 0.00001f);
    Assert.assertEquals(.8303f, predictions.get(4l), 0.00001f);
    Assert.assertEquals(.7303f, predictions.get(5l), 0.00001f);

    predictions = pickProbabilityPredictor.getTeamPredictions(new TeamDraftOrder(5));
    Assert.assertEquals(3, predictions.size());
    Assert.assertEquals(.9404f, predictions.get(4l), 0.00001f);
    Assert.assertEquals(.8404f, predictions.get(5l), 0.00001f);
    Assert.assertEquals(.7404f, predictions.get(6l), 0.00001f);

    predictions = pickProbabilityPredictor.getTeamPredictions(new TeamDraftOrder(6));
    Assert.assertEquals(3, predictions.size());
    Assert.assertEquals(.9505f, predictions.get(5l), 0.00001f);
    Assert.assertEquals(.8505f, predictions.get(6l), 0.00001f);
    Assert.assertEquals(.7505f, predictions.get(7l), 0.00001f);

    predictions = pickProbabilityPredictor.getTeamPredictions(new TeamDraftOrder(7));
    Assert.assertEquals(3, predictions.size());
    Assert.assertEquals(.9606f, predictions.get(6l), 0.00001f);
    Assert.assertEquals(.8606f, predictions.get(7l), 0.00001f);
    Assert.assertEquals(.7606f, predictions.get(8l), 0.00001f);

    predictions = pickProbabilityPredictor.getTeamPredictions(new TeamDraftOrder(8));
    Assert.assertEquals(3, predictions.size());
    Assert.assertEquals(.9707f, predictions.get(7l), 0.00001f);
    Assert.assertEquals(.8707f, predictions.get(8l), 0.00001f);
    Assert.assertEquals(.7707f, predictions.get(9l), 0.00001f);

    predictions = pickProbabilityPredictor.getTeamPredictions(new TeamDraftOrder(9));
    Assert.assertEquals(3, predictions.size());
    Assert.assertEquals(.9808f, predictions.get(8l), 0.00001f);
    Assert.assertEquals(.8808f, predictions.get(9l), 0.00001f);
    Assert.assertEquals(.7808f, predictions.get(10l), 0.00001f);

    predictions = pickProbabilityPredictor.getTeamPredictions(new TeamDraftOrder(10));
    Assert.assertEquals(3, predictions.size());
    Assert.assertEquals(.9909f, predictions.get(9l), 0.00001f);
    Assert.assertEquals(.8909f, predictions.get(10l), 0.00001f);
    Assert.assertEquals(.7909f, predictions.get(11l), 0.00001f);
  }

  @Test
  public void testDraftStatusUpdatesPredictionForNextTeam() {
    for (int i = 0; i < 20; i++) {
      players.add(testPlayerGenerator.generatePlayer(i + 20, Position.FB, i));
    }

    ArrayList<DraftPick> picks = Lists.newArrayList(
        DraftStatusTestUtil.createDraftPick(1, "", false, "C", 0, beanFactory),
        DraftStatusTestUtil.createDraftPick(2, "", false, "C", 1, beanFactory),
        DraftStatusTestUtil.createDraftPick(3, "", false, "C", 2, beanFactory),
        DraftStatusTestUtil.createDraftPick(4, "", false, "C", 3, beanFactory),
        DraftStatusTestUtil.createDraftPick(5, "", false, "C", 4, beanFactory),
        DraftStatusTestUtil.createDraftPick(6, "", false, "C", 5, beanFactory),
        DraftStatusTestUtil.createDraftPick(7, "", false, "C", 6, beanFactory),
        DraftStatusTestUtil.createDraftPick(8, "", false, "C", 7, beanFactory),
        DraftStatusTestUtil.createDraftPick(9, "", false, "C", 8, beanFactory),
        DraftStatusTestUtil.createDraftPick(10, "", false, "C", 9, beanFactory));
    pickProbabilityPredictor.onDraftStatusChanged(
        DraftStatusTestUtil.createDraftStatus(picks, beanFactory));
    Map<Long, Float> predictions = pickProbabilityPredictor.getTeamPredictions(new TeamDraftOrder(2));
    Assert.assertEquals(6, predictions.size());
    Assert.assertEquals(.9001f, predictions.get(20l), 0.00001f);
    Assert.assertEquals(.8001f, predictions.get(21l), 0.00001f);
    Assert.assertEquals(.7001f, predictions.get(22l), 0.00001f);

    picks.add(DraftStatusTestUtil.createDraftPick(1, "", false, "1B", 20, beanFactory));
    pickProbabilityPredictor.onDraftStatusChanged(
        DraftStatusTestUtil.createDraftStatus(picks, beanFactory));
    predictions = pickProbabilityPredictor.getTeamPredictions(new TeamDraftOrder(2));
    Assert.assertEquals(6, predictions.size());
    Assert.assertEquals(.9111f, predictions.get(21l), 0.00001f);
    Assert.assertEquals(.8111f, predictions.get(22l), 0.00001f);
    Assert.assertEquals(.7111f, predictions.get(23l), 0.00001f);
  }

  @Test
  public void testPredictionsResetAfterBackedOutPick() {
    ArrayList<DraftPick> picks = Lists.newArrayList(
        DraftStatusTestUtil.createDraftPick(1, "", false, "C", 3, beanFactory));
    pickProbabilityPredictor.onDraftStatusChanged(
        DraftStatusTestUtil.createDraftStatus(picks, beanFactory));
    // back out pick
    picks.remove(0);
    pickProbabilityPredictor.onDraftStatusChanged(
        DraftStatusTestUtil.createDraftStatus(picks, beanFactory));
    // new pick
    picks.add(DraftStatusTestUtil.createDraftPick(1, "", false, "C", 0, beanFactory));
    pickProbabilityPredictor.onDraftStatusChanged(
        DraftStatusTestUtil.createDraftStatus(picks, beanFactory));
    Map<Long, Float> predictions = pickProbabilityPredictor.getTeamPredictions(new TeamDraftOrder(2));
    Assert.assertFalse(predictions.containsKey(0l));
    Assert.assertEquals(.9101f, predictions.get(1l), 0.00001f);
    Assert.assertEquals(.8101f, predictions.get(2l), 0.00001f);
    Assert.assertEquals(.7101f, predictions.get(3l), 0.00001f);
  }
}