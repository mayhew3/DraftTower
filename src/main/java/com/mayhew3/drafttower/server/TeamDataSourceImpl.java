package com.mayhew3.drafttower.server;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.Team;

import javax.servlet.ServletException;
import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

/**
 * Looks up teams in the database.
 */
@Singleton
public class TeamDataSourceImpl implements TeamDataSource {

  private static final Logger logger = Logger.getLogger(TeamDataSourceImpl.class.getName());

  private final DataSource db;
  private final BeanFactory beanFactory;
  private Map<String, Team> teamNamesCache;

  @Inject
  public TeamDataSourceImpl(DataSource db, BeanFactory beanFactory) {
    this.db = db;
    this.beanFactory = beanFactory;
  }

  /** Returns the team number corresponding to the given login credentials, or null for an invalid login. */
  @Override
  public Integer getTeamNumber(String username, String password) throws ServletException {
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
      close(resultSet);
    }
  }

  @Override
  public boolean isCommissionerTeam(int team) throws SQLException {
    String sql = "select users.UserRole " +
        "from users " +
        "inner join teams " +
        " on teams.userid = users.user " +
        "where teams.DraftOrder = '" + team + "'";

    ResultSet resultSet = null;
    try {
      resultSet = executeQuery(sql);
      return resultSet.next() && resultSet.getString("UserRole").equals("admin");
    }  finally {
      close(resultSet);
    }
  }

  @Override
  public Map<String, Team> getTeams() throws SQLException {
    if (teamNamesCache == null) {
      synchronized (this) {
        if (teamNamesCache == null) {
          Builder<String, Team> teamNamesBuilder = ImmutableMap.builder();
          String sql = "select Name, users.FirstName, DraftOrder " +
              "from teams " +
              "inner join users " +
              "on teams.userid = users.user";
          ResultSet resultSet = null;
          try {
            resultSet = executeQuery(sql);
            while (resultSet.next()) {
              Team team = beanFactory.createTeam().as();
              team.setShortName(resultSet.getString("FirstName"));
              team.setLongName(resultSet.getString("Name"));
              teamNamesBuilder.put(resultSet.getString("DraftOrder"), team);
            }
          } finally {
            close(resultSet);
          }
          teamNamesCache = teamNamesBuilder.build();
        }
      }
    }
    return teamNamesCache;
  }

  private ResultSet executeQuery(String sql) throws SQLException {
    Statement statement = db.getConnection().createStatement();
    return statement.executeQuery(sql);
  }

  private static void close(ResultSet resultSet) {
    try {
      if (resultSet == null) {
        return;
      }
      Statement statement = resultSet.getStatement();
      Connection connection = statement.getConnection();
      resultSet.close();
      statement.close();
      connection.close();
    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Unable to close connection after use.", e);
    }
  }


  protected Statement prepareStatementUpdate(String sql, Object... params) throws SQLException {
    PreparedStatement preparedStatement = prepareStatement(sql, Lists.newArrayList(params));

    preparedStatement.executeUpdate();
    return preparedStatement;
  }

  private PreparedStatement prepareStatement(String sql, List<Object> params) throws SQLException {
    PreparedStatement preparedStatement = db.getConnection().prepareStatement(sql);

    int i = 1;
    for (Object param : params) {
      if (param instanceof String) {
        preparedStatement.setString(i, (String) param);
      } else if (param instanceof Integer) {
        preparedStatement.setInt(i, (Integer) param);
      } else if (param instanceof Long) {
        preparedStatement.setLong(i, (Long) param);
      } else if (param instanceof Boolean) {
        preparedStatement.setBoolean(i, (Boolean) param);
      } else {
        throw new IllegalArgumentException("Unknown type of param: " + param + " of type " + param.getClass());
      }
      i++;
    }
    return preparedStatement;
  }


  private void close(Statement statement) {
    try {
      Connection connection = statement.getConnection();
      statement.close();
      connection.close();
    } catch (SQLException e) {
      logger.log(SEVERE, "Unable to close SQL connection after use.", e);
    }
  }

}