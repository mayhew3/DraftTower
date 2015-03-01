package com.mayhew3.drafttower.server;

import com.google.inject.Inject;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.mayhew3.drafttower.server.BindingAnnotations.AutoPickWizards;
import com.mayhew3.drafttower.server.BindingAnnotations.MaxClosers;
import com.mayhew3.drafttower.server.BindingAnnotations.MinClosers;
import com.mayhew3.drafttower.server.BindingAnnotations.TeamTokens;
import com.mayhew3.drafttower.shared.*;

import java.util.Map;
import java.util.Map.Entry;

/**
 * Handles login requests.
 */
public class LoginHandler {

  private final TeamDataSource teamDataSource;
  private final DraftStatus draftStatus;
  private final BeanFactory beanFactory;
  private final TokenGenerator tokenGenerator;
  private final Map<String, TeamDraftOrder> teamTokens;
  private final Map<TeamDraftOrder, PlayerDataSet> autoPickWizardTables;
  private final Map<TeamDraftOrder, Integer> minClosers;
  private final Map<TeamDraftOrder, Integer> maxClosers;

  @Inject
  public LoginHandler(
      TeamDataSource teamDataSource,
      DraftStatus draftStatus,
      BeanFactory beanFactory,
      TokenGenerator tokenGenerator,
      @TeamTokens Map<String, TeamDraftOrder> teamTokens,
      @AutoPickWizards Map<TeamDraftOrder, PlayerDataSet> autoPickWizardTables,
      @MinClosers Map<TeamDraftOrder, Integer> minClosers,
      @MaxClosers Map<TeamDraftOrder, Integer> maxClosers) {
    this.teamDataSource = teamDataSource;
    this.draftStatus = draftStatus;
    this.beanFactory = beanFactory;
    this.tokenGenerator = tokenGenerator;
    this.teamTokens = teamTokens;
    this.autoPickWizardTables = autoPickWizardTables;
    this.minClosers = minClosers;
    this.maxClosers = maxClosers;
  }  

  public AutoBean<LoginResponse> doLogin(
      Map<String, String> cookiesMap,
      String username,
      String password) throws DataSourceException {
    AutoBean<LoginResponse> responseBean = null;
    for (Entry<String, String> cookie : cookiesMap.entrySet()) {
      if (LoginResponse.TEAM_TOKEN_COOKIE.equals(cookie.getKey())) {
        String teamToken = cookie.getValue();
        if (teamTokens.containsKey(teamToken)) {
          TeamDraftOrder teamDraftOrder = teamTokens.get(teamToken);
          if (draftStatus.getConnectedTeams().contains(teamDraftOrder.get())) {
            responseBean = createAlreadyLoggedInResponse();
          } else {
            responseBean = createSuccessResponse(teamDraftOrder, teamToken);
          }
          break;
        }
      }
    }
    if (responseBean == null) {
      TeamDraftOrder teamDraftOrder = teamDataSource.getTeamDraftOrder(
          username, password);
      if (teamDraftOrder != null) {
        if (draftStatus.getConnectedTeams().contains(teamDraftOrder.get())) {
          responseBean = createAlreadyLoggedInResponse();
        } else {
          String teamToken = tokenGenerator.get();
          responseBean = createSuccessResponse(teamDraftOrder, teamToken);
          teamTokens.put(teamToken, teamDraftOrder);
        }
      }
    }
    return responseBean;
  }

  private AutoBean<LoginResponse> createSuccessResponse(TeamDraftOrder teamDraftOrder, String teamToken)
      throws DataSourceException {
    AutoBean<LoginResponse> responseBean = beanFactory.createLoginResponse();
    LoginResponse response = responseBean.as();
    response.setTeam(teamDraftOrder.get());
    response.setTeamToken(teamToken);
    response.setInitialWizardTable(autoPickWizardTables.get(teamDraftOrder));
    Integer teamMinClosers = minClosers.get(teamDraftOrder);
    response.setMinClosers(teamMinClosers == null ? 0 : teamMinClosers);
    Integer teamMaxClosers = maxClosers.get(teamDraftOrder);
    response.setMaxClosers(teamMaxClosers == null ? RosterUtil.POSITIONS_AND_COUNTS.get(Position.P) : teamMaxClosers);
    response.setTeams(teamDataSource.getTeams());
    response.setCommissionerTeam(teamDataSource.isCommissionerTeam(teamDraftOrder));
    response.setWebSocketPort(Integer.parseInt(System.getProperty("ws.port", "8080")));
    return responseBean;
  }

  private AutoBean<LoginResponse> createAlreadyLoggedInResponse() {
    AutoBean<LoginResponse> responseBean = beanFactory.createLoginResponse();
    LoginResponse response = responseBean.as();
    response.setAlreadyLoggedIn(true);
    return responseBean;
  }
}