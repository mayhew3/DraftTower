package com.mayhew3.drafttower.server;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.vm.AutoBeanFactorySource;
import com.mayhew3.drafttower.shared.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

/**
 * Tests for {@link LoginHandler}.
 */
public class LoginHandlerTest {

  private LoginHandler handler;
  private Map<String, TeamDraftOrder> teamTokens;
  private Map<TeamDraftOrder, PlayerDataSet> autoPickWizardTables;
  private Map<String, Team> teamsMap;

  @Before
  public void setUp() throws Exception {
    BeanFactory beanFactory = AutoBeanFactorySource.create(BeanFactory.class);
    teamTokens = new HashMap<>();
    teamTokens.put("t4token", new TeamDraftOrder(4));
    teamTokens.put("t5token", new TeamDraftOrder(5));

    autoPickWizardTables = new HashMap<>();
    autoPickWizardTables.put(new TeamDraftOrder(4), PlayerDataSet.GURU);

    TeamDataSource teamDataSource = Mockito.mock(TeamDataSource.class);
    teamsMap = new HashMap<>();
    teamsMap.put("4", beanFactory.createTeam().as());
    Mockito.when(teamDataSource.getTeams()).thenReturn(teamsMap);
    Mockito.when(teamDataSource.isCommissionerTeam(Mockito.<TeamDraftOrder>any()))
        .thenReturn(false);
    Mockito.when(teamDataSource.isCommissionerTeam(Mockito.eq(new TeamDraftOrder(4))))
        .thenReturn(true);
    Mockito.when(teamDataSource.getTeamDraftOrder(
        Mockito.anyString(), Mockito.anyString()))
        .thenReturn(null);
    Mockito.when(teamDataSource.getTeamDraftOrder(
        Mockito.anyString(), Mockito.eq("goodpass")))
        .thenReturn(new TeamDraftOrder(8));
    Mockito.when(teamDataSource.getTeamDraftOrder(
        Mockito.anyString(), Mockito.eq("loggedin")))
        .thenReturn(new TeamDraftOrder(5));
    Mockito.when(teamDataSource.getTeamDraftOrder(
        Mockito.anyString(), Mockito.eq("commish")))
        .thenReturn(new TeamDraftOrder(4));

    DraftStatus draftStatus = Mockito.mock(DraftStatus.class);
    Mockito.when(draftStatus.getConnectedTeams())
        .thenReturn(Sets.newHashSet(5));

    TokenGenerator tokenGenerator = Mockito.mock(TokenGenerator.class);
    Mockito.when(tokenGenerator.get()).thenReturn("token");

    handler = new LoginHandler(
        teamDataSource,
        draftStatus,
        beanFactory,
        tokenGenerator,
        teamTokens,
        autoPickWizardTables);
  }

  @Test
  public void testSuccessfulLoginNoCookies() throws DataSourceException {
    AutoBean<LoginResponse> responseBean = handler.doLogin(new HashMap<String, String>(), "username", "goodpass");
    Assert.assertNotNull(responseBean);
    LoginResponse response = responseBean.as();
    Assert.assertEquals(8, response.getTeam());
    Assert.assertEquals("token", response.getTeamToken());
    Assert.assertNull(response.getInitialWizardTable());
    Assert.assertEquals(teamsMap, response.getTeams());
    Assert.assertFalse(response.isCommissionerTeam());
  }

  @Test
  public void testUnsuccessfulLogin() throws DataSourceException {
    AutoBean<LoginResponse> responseBean = handler.doLogin(new HashMap<String, String>(), "username", "badpass");
    Assert.assertNull(responseBean);
  }

  @Test
  public void testSuccessfulAutoLogin() throws DataSourceException {
    AutoBean<LoginResponse> responseBean = handler.doLogin(
        ImmutableMap.<String, String>builder()
            .put(LoginResponse.TEAM_TOKEN_COOKIE, "t4token")
            .build(),
        "", "");
    Assert.assertNotNull(responseBean);
    LoginResponse response = responseBean.as();
    Assert.assertEquals(4, response.getTeam());
    Assert.assertEquals("t4token", response.getTeamToken());
    Assert.assertEquals(PlayerDataSet.GURU, response.getInitialWizardTable());
    Assert.assertEquals(teamsMap, response.getTeams());
    Assert.assertTrue(response.isCommissionerTeam());
  }

  @Test
  public void testUnsuccessfulAutoLoginWrongCookie() throws DataSourceException {
    AutoBean<LoginResponse> responseBean = handler.doLogin(
        ImmutableMap.<String, String>builder()
            .put("garbage", "t4token")
            .build(),
        "", "");
    Assert.assertNull(responseBean);
  }

  @Test
  public void testUnsuccessfulAutoLoginBadToken() throws DataSourceException {
    AutoBean<LoginResponse> responseBean = handler.doLogin(
        ImmutableMap.<String, String>builder()
            .put(LoginResponse.TEAM_TOKEN_COOKIE, "badtoken")
            .build(),
        "", "");
    Assert.assertNull(responseBean);
  }

  @Test
  public void testAlreadyLoggedInNoCookie() throws DataSourceException {
    AutoBean<LoginResponse> responseBean = handler.doLogin(new HashMap<String, String>(), "username", "loggedin");
    Assert.assertNotNull(responseBean);
    LoginResponse response = responseBean.as();
    Assert.assertTrue(response.isAlreadyLoggedIn());
  }

  @Test
  public void testAlreadyLoggedInAutoLogin() throws DataSourceException {
    AutoBean<LoginResponse> responseBean = handler.doLogin(
        ImmutableMap.<String, String>builder()
            .put(LoginResponse.TEAM_TOKEN_COOKIE, "t5token")
            .build(),
        "", "");
    Assert.assertNotNull(responseBean);
    LoginResponse response = responseBean.as();
    Assert.assertTrue(response.isAlreadyLoggedIn());
  }
}