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
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.UUID;

/**
 * Servlet for logins.
 */
@Singleton
public class LoginServlet extends HttpServlet {

  private final DataSource db;
  private final BeanFactory beanFactory;
  private final Map<String, Integer> teamTokens;

  @Inject
  public LoginServlet(DataSource dataSource,
      BeanFactory beanFactory,
      @TeamTokens Map<String, Integer> teamTokens) {
    this.db = dataSource;
    this.beanFactory = beanFactory;
    this.teamTokens = teamTokens;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Integer team = getTeamNumber(req.getParameter("username"), req.getParameter("password"));
    if (team != null) {
      String teamToken = UUID.randomUUID().toString();
      resp.setContentType("text/plain");
      AutoBean<LoginResponse> responseBean = beanFactory.createLoginResponse();
      LoginResponse response = responseBean.as();
      response.setTeam(team);
      response.setTeamToken(teamToken);
      resp.getWriter().append(AutoBeanCodex.encode(responseBean).getPayload());
      teamTokens.put(teamToken, team);
    } else {
      resp.setStatus(403);
    }
  }


  private ResultSet executeQuery(String sql) throws SQLException {
    Statement statement = db.getConnection().createStatement();
    return statement.executeQuery(sql);
  }


  /** Returns the team number corresponding to the given login credentials, or null for an invalid login. */
  private Integer getTeamNumber(String username, String password) throws ServletException {
    String sql = "select teams.id " +
        "from users " +
        "inner join userrole " +
        " on userrole.user = users.user " +
        "inner join teams " +
        " on teams.userid = users.user " +
        "where site = 'Uncharted' " +
        "and userrole.Role in ('user', 'admin') " +
        "and users.user = '" + username + "' " +
        "and users.Pword = '" + password + "'";

    ResultSet resultSet = null;
    try {
      resultSet = executeQuery(sql);
      if (resultSet.next()) {
        return resultSet.getInt("id");
      } else {
        return null;
      }
    } catch (SQLException e) {
      throw new ServletException("Cannot connect to login server.");
    }  finally {
      try {
        close(resultSet);
      } catch (SQLException e) {
        throw new ServletException("Error closing DB resources.", e);
      }
    }
  }


  private static void close(ResultSet resultSet) throws SQLException {
    if (resultSet == null) {
      return;
    }
    Statement statement = resultSet.getStatement();
    Connection connection = statement.getConnection();
    resultSet.close();
    statement.close();
    connection.close();
  }
}