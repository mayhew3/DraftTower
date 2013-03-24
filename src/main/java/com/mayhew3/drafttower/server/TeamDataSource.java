package com.mayhew3.drafttower.server;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayhew3.drafttower.shared.*;

import javax.servlet.ServletException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Looks up players in the database.
 */
@Singleton
public class TeamDataSource {

  private static final Logger logger = Logger.getLogger(TeamDataSource.class.getName());

  private final DataSource db;
  private final BeanFactory beanFactory;
  private Map<String, Team> teamNamesCache;

  @Inject
  public TeamDataSource(DataSource db, BeanFactory beanFactory) {
    this.db = db;
    this.beanFactory = beanFactory;
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
      close(resultSet);
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

  public Map<Integer,TableSpec> getAutoPickTableSpecs(int numTeams) {
    String sql = "SELECT * FROM autoPickSources";

    HashMap<Integer,TableSpec> autoPickTableSpecs = Maps.newHashMap();

    ResultSet resultSet = null;
    try {
      resultSet = executeQuery(sql);
      while (resultSet.next()) {
        int teamID = resultSet.getInt("teamID");

        String dataSetName = resultSet.getString("DataSet");
        String sortColumnName = resultSet.getString("SortColumn");

        Optional<PlayerDataSet> dataSet = PlayerDataSet.getDataSetWithName(dataSetName);
        Optional<PlayerColumn> sortColumn = PlayerColumn.getColumnWithDBName(sortColumnName);

        if (!dataSet.isPresent()) {
          throw new RuntimeException("Team " + teamID + " is linked to unrecognized DataSet '" + dataSetName + "'.");
        }
        if (!sortColumn.isPresent()) {
          throw new RuntimeException("Team " + teamID + " is linked to unrecognized Sort Column '" + sortColumnName + "'.");
        }

        TableSpec tableSpec = beanFactory.createTableSpec().as();
        tableSpec.setPlayerDataSet(dataSet.get());
        tableSpec.setSortCol(sortColumn.get());
        tableSpec.setAscending(resultSet.getBoolean("Ascending"));

        autoPickTableSpecs.put(teamID, tableSpec);
      }

    } catch (SQLException e) {
      logger.log(Level.SEVERE, "Couldn't fetch team selections for which auto-pick source to use. Using default of CBS, as backup.");
      autoPickTableSpecs = Maps.newHashMap();
      for (int i = 1; i <= numTeams; i++) {
        TableSpec tableSpec = beanFactory.createTableSpec().as();
        tableSpec.setPlayerDataSet(PlayerDataSet.WIZARD);
        tableSpec.setSortCol(PlayerColumn.RATING);
        autoPickTableSpecs.put(i, tableSpec);
      }
    } finally {
      close(resultSet);
    }

    if (autoPickTableSpecs.size() != numTeams) {
      throw new RuntimeException("Expected " + numTeams + " autopick preferences, but there are only " + autoPickTableSpecs.size() + ".");
    }

    return autoPickTableSpecs;
  }
}