package com.mayhew3.drafttower.server;

import com.google.inject.Inject;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.mayhew3.drafttower.server.BindingAnnotations.AutoPickWizards;
import com.mayhew3.drafttower.server.BindingAnnotations.TeamTokens;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.DraftStatus;
import com.mayhew3.drafttower.shared.LoginResponse;
import com.mayhew3.drafttower.shared.PlayerDataSet;

import javax.servlet.ServletException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * Handles login requests.
 */
public class LoginHandler {

  private final TeamDataSource teamDataSource;
  private final DraftStatus draftStatus;
  private final BeanFactory beanFactory;
  private final Map<String, TeamDraftOrder> teamTokens;
  private final Map<TeamDraftOrder, PlayerDataSet> autoPickWizardTables;
  
  @Inject
  public LoginHandler(
      TeamDataSource teamDataSource,
      DraftStatus draftStatus,
      BeanFactory beanFactory,
      @TeamTokens Map<String, TeamDraftOrder> teamTokens,
      @AutoPickWizards Map<TeamDraftOrder, PlayerDataSet> autoPickWizardTables) {
    this.teamDataSource = teamDataSource;
    this.draftStatus = draftStatus;
    this.beanFactory = beanFactory;
    this.teamTokens = teamTokens;
    this.autoPickWizardTables = autoPickWizardTables;
  }  

  public AutoBean<LoginResponse> doLogin(
      Map<String, String> cookiesMap,
      String username,
      String password) throws IOException, ServletException {
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
          username,
          password);
      if (teamDraftOrder != null) {
        if (draftStatus.getConnectedTeams().contains(teamDraftOrder.get())) {
          responseBean = createAlreadyLoggedInResponse();
        } else {
          String teamToken = UUID.randomUUID().toString();
          responseBean = createSuccessResponse(teamDraftOrder, teamToken);
          teamTokens.put(teamToken, teamDraftOrder);
        }
      }
    }
    return responseBean;
  }

  private AutoBean<LoginResponse> createSuccessResponse(TeamDraftOrder teamDraftOrder, String teamToken)
      throws ServletException, IOException {
    AutoBean<LoginResponse> responseBean = beanFactory.createLoginResponse();
    LoginResponse response = responseBean.as();
    response.setTeam(teamDraftOrder.get());
    response.setTeamToken(teamToken);
    response.setInitialWizardTable(autoPickWizardTables.get(teamDraftOrder));
    try {
      response.setTeams(teamDataSource.getTeams());
      response.setCommissionerTeam(teamDataSource.isCommissionerTeam(teamDraftOrder));
    } catch (SQLException e) {
      throw new ServletException(e);
    }
    return responseBean;
  }

  private AutoBean<LoginResponse> createAlreadyLoggedInResponse() throws IOException {
    AutoBean<LoginResponse> responseBean = beanFactory.createLoginResponse();
    LoginResponse response = responseBean.as();
    response.setAlreadyLoggedIn(true);
    return responseBean;
  }
}