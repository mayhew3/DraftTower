package com.mayhew3.drafttower.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.mayhew3.drafttower.server.BindingAnnotations.AutoPickWizards;
import com.mayhew3.drafttower.server.BindingAnnotations.TeamTokens;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.LoginResponse;
import com.mayhew3.drafttower.shared.PlayerDataSet;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

/**
 * Servlet for logins.
 */
@Singleton
public class LoginServlet extends HttpServlet {

  private final TeamDataSource teamDataSource;
  private final BeanFactory beanFactory;
  private final Map<String, TeamDraftOrder> teamTokens;
  private final Map<TeamDraftOrder, PlayerDataSet> autoPickWizardTables;

  @Inject
  public LoginServlet(TeamDataSource teamDataSource,
      BeanFactory beanFactory,
      @TeamTokens Map<String, TeamDraftOrder> teamTokens,
      @AutoPickWizards Map<TeamDraftOrder, PlayerDataSet> autoPickWizardTables) {
    this.teamDataSource = teamDataSource;
    this.beanFactory = beanFactory;
    this.teamTokens = teamTokens;
    this.autoPickWizardTables = autoPickWizardTables;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    for (Cookie cookie : req.getCookies()) {
      if (LoginResponse.TEAM_TOKEN_COOKIE.equals(cookie.getName())) {
        String teamToken = cookie.getValue();
        if (teamTokens.containsKey(teamToken)) {
          populateResponse(resp, teamTokens.get(teamToken), teamToken);
          return;
        }
      }
    }
    TeamDraftOrder teamDraftOrder = teamDataSource.getTeamDraftOrder(req.getParameter("username"), req.getParameter("password"));
    if (teamDraftOrder != null) {
      String teamToken = UUID.randomUUID().toString();
      populateResponse(resp, teamDraftOrder, teamToken);
      teamTokens.put(teamToken, teamDraftOrder);
      resp.addCookie(new Cookie(LoginResponse.TEAM_TOKEN_COOKIE, teamToken));
    } else {
      resp.setStatus(403);
    }
  }

  private void populateResponse(HttpServletResponse resp, TeamDraftOrder teamDraftOrder, String teamToken) throws ServletException, IOException {
    resp.setContentType("text/plain");
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
    resp.getWriter().append(AutoBeanCodex.encode(responseBean).getPayload());
  }


}