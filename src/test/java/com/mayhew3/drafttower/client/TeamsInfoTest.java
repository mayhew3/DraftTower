package com.mayhew3.drafttower.client;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.DraftStatus;
import com.mayhew3.drafttower.shared.LoginResponse;
import com.mayhew3.drafttower.shared.Team;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Class description...
 */
public class TeamsInfoTest {

  private TeamsInfo teamsInfo;
  private BeanFactory beanFactory;
  private LoginResponse loginResponse;

  @Before
  public void setUp() throws Exception {
    teamsInfo = new TeamsInfo(10);
    beanFactory = AutoBeanFactorySource.create(BeanFactory.class);
    loginResponse = beanFactory.createLoginResponse().as();
    loginResponse.setTeam(5);
    loginResponse.setTeamToken("teamToken");
    loginResponse.setCommissionerTeam(true);
    Team team1 = beanFactory.createTeam().as();
    team1.setShortName("tt1");
    team1.setLongName("test team 1");
    Team team2 = beanFactory.createTeam().as();
    team2.setShortName("tt2");
    team2.setLongName("test team 2");
    Team team5 = beanFactory.createTeam().as();
    team5.setShortName("tt5");
    team5.setLongName("test team 5");
    loginResponse.setTeams(ImmutableMap.<String, Team>builder()
        .put("1", team1)
        .put("2", team2)
        .put("5", team5)
        .build());
    teamsInfo.setLoginResponse(loginResponse);
  }

  @Test
  public void testGetTeamToken() {
    Assert.assertEquals("teamToken", teamsInfo.getTeamToken());
  }

  @Test
  public void testGetTeam() {
    Assert.assertEquals(5, teamsInfo.getTeam());
  }

  @Test
  public void testGetShortTeamName() {
    Assert.assertEquals("tt1", teamsInfo.getShortTeamName(1));
    Assert.assertEquals("tt2", teamsInfo.getShortTeamName(2));
  }

  @Test
  public void testGetLongTeamName() {
    Assert.assertEquals("test team 1", teamsInfo.getLongTeamName(1));
    Assert.assertEquals("test team 2", teamsInfo.getLongTeamName(2));
  }

  @Test
  public void testGetShortTeamNameNotLoggedIn() {
    teamsInfo.setLoginResponse(null);
    Assert.assertEquals("Team 1", teamsInfo.getShortTeamName(1));
    Assert.assertEquals("Team 2", teamsInfo.getShortTeamName(2));
  }

  @Test
  public void testGetLongTeamNameNotLoggedIn() {
    teamsInfo.setLoginResponse(null);
    Assert.assertEquals("Team 1", teamsInfo.getLongTeamName(1));
    Assert.assertEquals("Team 2", teamsInfo.getLongTeamName(2));
  }

  @Test
  public void testIsCommissionerTeam() {
    Assert.assertTrue(teamsInfo.isCommissionerTeam());
  }

  @Test
  public void testIsLoggedIn() {
    Assert.assertTrue(teamsInfo.isLoggedIn());
  }

  @Test
  public void testIsNotLoggedIn() {
    teamsInfo.setLoginResponse(null);
    Assert.assertFalse(teamsInfo.isLoggedIn());
  }

  @Test
  public void testIsMyPick() {
    DraftStatus status = beanFactory.createDraftStatus().as();
    status.setCurrentTeam(5);
    Assert.assertTrue(teamsInfo.isMyPick(status));
  }

  @Test
  public void testIsNotMyPick() {
    DraftStatus status = beanFactory.createDraftStatus().as();
    status.setCurrentTeam(4);
    Assert.assertFalse(teamsInfo.isMyPick(status));
  }

  @Test
  public void testIsOnDeckNoKeepers() throws Exception {
    DraftStatus draftStatus = beanFactory.createDraftStatus().as();
    draftStatus.setNextPickKeeperTeams(ImmutableSet.<Integer>of());

    draftStatus.setCurrentTeam(4);
    Assert.assertTrue(teamsInfo.isOnDeck(draftStatus));

    draftStatus.setCurrentTeam(5);
    Assert.assertFalse(teamsInfo.isOnDeck(draftStatus));

    draftStatus.setCurrentTeam(6);
    Assert.assertFalse(teamsInfo.isOnDeck(draftStatus));

    loginResponse.setTeam(1);
    draftStatus.setCurrentTeam(10);
    Assert.assertTrue(teamsInfo.isOnDeck(draftStatus));
  }

  @Test
  public void testIsOnDeckKeepers() throws Exception {
    DraftStatus draftStatus = beanFactory.createDraftStatus().as();

    draftStatus.setCurrentTeam(3);
    draftStatus.setNextPickKeeperTeams(ImmutableSet.of(4));
    Assert.assertTrue(teamsInfo.isOnDeck(draftStatus));

    draftStatus.setCurrentTeam(2);
    Assert.assertFalse(teamsInfo.isOnDeck(draftStatus));

    draftStatus.setNextPickKeeperTeams(ImmutableSet.of(3, 4));
    Assert.assertTrue(teamsInfo.isOnDeck(draftStatus));

    loginResponse.setTeam(3);
    draftStatus.setCurrentTeam(9);
    draftStatus.setNextPickKeeperTeams(ImmutableSet.of(10, 1, 2));
    Assert.assertTrue(teamsInfo.isOnDeck(draftStatus));
  }
}