package com.mayhew3.drafttower.server;

import com.google.common.collect.Lists;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.shared.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests for {@link PlayerDataProvider}.
 */
public class PlayerDataProviderTest {

  private BeanFactory beanFactory;
  private Map<TeamDraftOrder, PlayerDataSet> autoPickWizards;
  private Map<TeamDraftOrder, Integer> minClosers;
  private Map<TeamDraftOrder, Integer> maxClosers;
  private PlayerDataProvider playerDataProvider;
  private Map<Long, Float> pickProbabilities;

  @Before
  public void setup() throws Exception {
    beanFactory = AutoBeanFactorySource.create(BeanFactory.class);
    autoPickWizards = new HashMap<>();
    minClosers = new HashMap<>();
    maxClosers = new HashMap<>();
    TeamDataSource teamDataSource = Mockito.mock(TeamDataSource.class);
    Mockito.when(teamDataSource.getTeamIdByDraftOrder(Mockito.any(TeamDraftOrder.class)))
        .thenReturn(new TeamId(1));
    pickProbabilities = new HashMap<>();
    playerDataProvider = new TestPlayerDataProvider(
        new TestPlayerDataSourceServer(beanFactory),
        beanFactory,
        teamDataSource,
        autoPickWizards,
        minClosers,
        maxClosers,
        new HashMap<String, TeamDraftOrder>());
  }

  @Test
  public void testGetBestPlayer() throws Exception {
    long bestPlayerId = playerDataProvider.getBestPlayerId(new TeamDraftOrder(1),
        new ArrayList<DraftPick>(),
        EnumSet.allOf(Position.class),
        pickProbabilities);
    Assert.assertEquals(0, bestPlayerId);
  }

  @Test
  public void testGetBestPlayerSkipsSelectedPlayer() throws Exception {
    long bestPlayerId = playerDataProvider.getBestPlayerId(new TeamDraftOrder(1),
        Lists.newArrayList(DraftStatusTestUtil.createDraftPick(1, "", false, "P", 0, beanFactory)),
        EnumSet.allOf(Position.class),
        pickProbabilities);
    Assert.assertEquals(1, bestPlayerId);
  }

  @Test
  public void testGetBestPlayerSkipsFilledPositions() throws Exception {
    long bestPlayerId = playerDataProvider.getBestPlayerId(new TeamDraftOrder(1),
        new ArrayList<DraftPick>(),
        EnumSet.of(Position.FB),
        pickProbabilities);
    Assert.assertEquals(155, bestPlayerId);
  }

  @Test
  public void testGetBestPlayerAllPositionsFilled() throws Exception {
    long bestPlayerId = playerDataProvider.getBestPlayerId(new TeamDraftOrder(1),
        new ArrayList<DraftPick>(),
        EnumSet.noneOf(Position.class),
        pickProbabilities);
    Assert.assertEquals(0, bestPlayerId);
  }

  @Test
  public void testGetBestPlayerNoPlayerForOpenPosition() throws Exception {
    long bestPlayerId = playerDataProvider.getBestPlayerId(new TeamDraftOrder(1),
        Lists.newArrayList(
            DraftStatusTestUtil.createDraftPick(1, "", false, "FB", 155, beanFactory),
            DraftStatusTestUtil.createDraftPick(1, "", false, "FB", 156, beanFactory),
            DraftStatusTestUtil.createDraftPick(1, "", false, "FB", 157, beanFactory),
            DraftStatusTestUtil.createDraftPick(1, "", false, "FB", 158, beanFactory),
            DraftStatusTestUtil.createDraftPick(1, "", false, "FB", 159, beanFactory),
            DraftStatusTestUtil.createDraftPick(1, "", false, "FB", 160, beanFactory),
            DraftStatusTestUtil.createDraftPick(1, "", false, "FB", 161, beanFactory),
            DraftStatusTestUtil.createDraftPick(1, "", false, "FB", 162, beanFactory),
            DraftStatusTestUtil.createDraftPick(1, "", false, "FB", 163, beanFactory),
            DraftStatusTestUtil.createDraftPick(1, "", false, "FB", 164, beanFactory),
            DraftStatusTestUtil.createDraftPick(1, "", false, "FB", 165, beanFactory),
            DraftStatusTestUtil.createDraftPick(1, "", false, "FB", 166, beanFactory),
            DraftStatusTestUtil.createDraftPick(1, "", false, "FB", 167, beanFactory),
            DraftStatusTestUtil.createDraftPick(1, "", false, "FB", 168, beanFactory),
            DraftStatusTestUtil.createDraftPick(1, "", false, "FB", 169, beanFactory)),
        EnumSet.of(Position.FB),
        pickProbabilities);
    Assert.assertEquals(0, bestPlayerId);
  }

  @Test
  public void testGetBestPlayerUsesWizardTable() throws Exception {
    pickProbabilities.put(139l, .9f);
    pickProbabilities.put(138l, .8f);
    pickProbabilities.put(137l, .7f);
    autoPickWizards.put(new TeamDraftOrder(1), PlayerDataSet.CBSSPORTS);
    long bestPlayerId = playerDataProvider.getBestPlayerId(new TeamDraftOrder(1),
        new ArrayList<DraftPick>(),
        EnumSet.allOf(Position.class),
        pickProbabilities);
    Assert.assertEquals(139, bestPlayerId);
  }

  @Test
  public void testGetBestPlayerWithMinMaxClosersNoRestrictions() throws Exception {
    minClosers.put(new TeamDraftOrder(1), 2);
    maxClosers.put(new TeamDraftOrder(1), 4);
    long bestPlayerId = playerDataProvider.getBestPlayerId(new TeamDraftOrder(1),
        new ArrayList<DraftPick>(),
        EnumSet.of(Position.P),
        pickProbabilities);
    Assert.assertEquals(0, bestPlayerId);
  }

  @Test
  public void testGetBestPlayerWithMinMaxClosersAllSlotsFilled() throws Exception {
    minClosers.put(new TeamDraftOrder(1), 2);
    maxClosers.put(new TeamDraftOrder(1), 4);
    ArrayList<DraftPick> picks = Lists.newArrayList(
        DraftStatusTestUtil.createDraftPick(1, "", false, "P", 8, beanFactory),
        DraftStatusTestUtil.createDraftPick(1, "", false, "P", 10, beanFactory),
        DraftStatusTestUtil.createDraftPick(1, "", false, "P", 12, beanFactory),
        DraftStatusTestUtil.createDraftPick(1, "", false, "P", 14, beanFactory),
        DraftStatusTestUtil.createDraftPick(1, "", false, "P", 16, beanFactory),
        DraftStatusTestUtil.createDraftPick(1, "", false, "P", 18, beanFactory),
        DraftStatusTestUtil.createDraftPick(1, "", false, "P", 20, beanFactory),
        DraftStatusTestUtil.createDraftPick(1, "", false, "FB", 140, beanFactory),
        DraftStatusTestUtil.createDraftPick(1, "", false, "SB", 155, beanFactory),
        DraftStatusTestUtil.createDraftPick(1, "", false, "TB", 170, beanFactory));
    long bestPlayerId = playerDataProvider.getBestPlayerId(
        new TeamDraftOrder(1), picks, EnumSet.of(Position.P), pickProbabilities);
    Assert.assertEquals(0, bestPlayerId);

    picks.add(DraftStatusTestUtil.createDraftPick(2, "", false, "P", 0, beanFactory));
    bestPlayerId = playerDataProvider.getBestPlayerId(
        new TeamDraftOrder(1), picks, EnumSet.of(Position.P), pickProbabilities);
    Assert.assertEquals(1, bestPlayerId);
  }

  @Test
  public void testGetBestPlayerWithMinMaxClosersNeedCloser() throws Exception {
    minClosers.put(new TeamDraftOrder(1), 2);
    maxClosers.put(new TeamDraftOrder(1), 4);
    ArrayList<DraftPick> picks = Lists.newArrayList(
        DraftStatusTestUtil.createDraftPick(1, "", false, "P", 8, beanFactory),
        DraftStatusTestUtil.createDraftPick(1, "", false, "P", 10, beanFactory),
        DraftStatusTestUtil.createDraftPick(1, "", false, "P", 12, beanFactory),
        DraftStatusTestUtil.createDraftPick(1, "", false, "P", 14, beanFactory),
        DraftStatusTestUtil.createDraftPick(1, "", false, "P", 16, beanFactory),
        DraftStatusTestUtil.createDraftPick(1, "", false, "FB", 140, beanFactory),
        DraftStatusTestUtil.createDraftPick(1, "", false, "SB", 155, beanFactory),
        DraftStatusTestUtil.createDraftPick(1, "", false, "TB", 170, beanFactory));

    long bestPlayerId = playerDataProvider.getBestPlayerId(
        new TeamDraftOrder(1), picks, EnumSet.of(Position.P), pickProbabilities);
    Assert.assertEquals(1, bestPlayerId);

    picks.add(DraftStatusTestUtil.createDraftPick(2, "", false, "P", 1, beanFactory));
    bestPlayerId = playerDataProvider.getBestPlayerId(
        new TeamDraftOrder(1), picks, EnumSet.of(Position.P), pickProbabilities);
    Assert.assertEquals(3, bestPlayerId);
  }

  @Test
  public void testGetBestPlayerWithMinMaxClosersNoMoreClosers() throws Exception {
    minClosers.put(new TeamDraftOrder(1), 2);
    maxClosers.put(new TeamDraftOrder(1), 4);
    ArrayList<DraftPick> picks = Lists.newArrayList(
        DraftStatusTestUtil.createDraftPick(1, "", false, "P", 8, beanFactory),
        DraftStatusTestUtil.createDraftPick(1, "", false, true, "P", 11, beanFactory),
        DraftStatusTestUtil.createDraftPick(1, "", false, true, "P", 13, beanFactory),
        DraftStatusTestUtil.createDraftPick(1, "", false, true, "P", 15, beanFactory),
        DraftStatusTestUtil.createDraftPick(1, "", false, true, "P", 17, beanFactory),
        DraftStatusTestUtil.createDraftPick(1, "", false, "FB", 140, beanFactory),
        DraftStatusTestUtil.createDraftPick(1, "", false, "SB", 155, beanFactory),
        DraftStatusTestUtil.createDraftPick(1, "", false, "TB", 170, beanFactory));

    long bestPlayerId = playerDataProvider.getBestPlayerId(
        new TeamDraftOrder(1), picks, EnumSet.of(Position.P), pickProbabilities);
    Assert.assertEquals(0, bestPlayerId);

    picks.add(DraftStatusTestUtil.createDraftPick(2, "", false, "P", 0, beanFactory));
    bestPlayerId = playerDataProvider.getBestPlayerId(
        new TeamDraftOrder(1), picks, EnumSet.of(Position.P), pickProbabilities);
    Assert.assertEquals(2, bestPlayerId);
  }
}