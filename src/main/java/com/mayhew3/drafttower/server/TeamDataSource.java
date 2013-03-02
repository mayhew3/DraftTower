package com.mayhew3.drafttower.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import javax.servlet.ServletException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Looks up players in the database.
 */
@Singleton
public class TeamDataSource {

  private final DataSource db;

  @Inject
  public TeamDataSource(DataSource db) {
    this.db = db;
  }

  /** Returns the team number corresponding to the given login credentials, or null for an invalid login. */
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
      try {
        close(resultSet);
      } catch (SQLException e) {
        throw new ServletException("Error closing DB resources.", e);
      }
    }
  }

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

  private ResultSet executeQuery(String sql) throws SQLException {
    Statement statement = db.getConnection().createStatement();
    return statement.executeQuery(sql);
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