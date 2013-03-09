package com.mayhew3.drafttower.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.mayhew3.drafttower.server.ServerModule.TeamTokens;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.LoginResponse;

import javax.servlet.ServletException;
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
  private final Map<String, Integer> teamTokens;

  @Inject
  public LoginServlet(TeamDataSource teamDataSource,
      BeanFactory beanFactory,
      @TeamTokens Map<String, Integer> teamTokens) {
    this.teamDataSource = teamDataSource;
    this.beanFactory = beanFactory;
    this.teamTokens = teamTokens;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Integer team = teamDataSource.getTeamNumber(req.getParameter("username"), req.getParameter("password"));
    if (team != null) {
      String teamToken = UUID.randomUUID().toString();
      resp.setContentType("text/plain");
      AutoBean<LoginResponse> responseBean = beanFactory.createLoginResponse();
      LoginResponse response = responseBean.as();
      response.setTeam(team);
      response.setTeamToken(teamToken);
      try {
        response.setTeams(teamDataSource.getTeams());
        response.setCommissionerTeam(teamDataSource.isCommissionerTeam(team));
      } catch (SQLException e) {
        throw new ServletException(e);
      }
      resp.getWriter().append(AutoBeanCodex.encode(responseBean).getPayload());
      teamTokens.put(teamToken, team);
    } else {
      resp.setStatus(403);
    }
  }


}