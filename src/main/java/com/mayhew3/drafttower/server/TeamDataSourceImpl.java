package com.mayhew3.drafttower.server;

import com.google.common.base.Optional;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.PlayerDataSet;
import com.mayhew3.drafttower.shared.Team;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
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
  private BiMap<TeamId, TeamDraftOrder> teamIdDraftOrderMap;

  @Inject
  public TeamDataSourceImpl(DataSource db, BeanFactory beanFactory) {
    this.db = db;
    this.beanFactory = beanFactory;
  }

  /** Returns the team number corresponding to the given login credentials, or null for an invalid login. */
  @Override
  public TeamDraftOrder getTeamDraftOrder(String username, String password) throws DataSourceException {
    String sql = "select teams.DraftOrder " +
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
        return new TeamDraftOrder(resultSet.getInt("DraftOrder"));
      } else {
        return null;
      }
    } catch (SQLException e) {
      throw new DataSourceException("Cannot connect to login server.");
    }  finally {
      close(resultSet);
    }
  }

  @Override
  public boolean isCommissionerTeam(TeamDraftOrder teamDraftOrder) throws DataSourceException {
    String sql = "select userrole.Role " +
        "from users " +
        "inner join teams " +
        " on teams.userid = users.user " +
        "inner join userrole " +
        " on userrole.user = users.user " +
        "where teams.DraftOrder = '" + teamDraftOrder + "' " +
        "and userrole.site = 'uncharted'";

    ResultSet resultSet = null;
    try {
      resultSet = executeQuery(sql);
      return resultSet.next() && resultSet.getString("Role").equals("admin");
    } catch (SQLException e) {
      throw new DataSourceException(e);
    }  finally {
      close(resultSet);
    }
  }

  @Override
  public Map<String, Team> getTeams() throws DataSourceException {
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
          } catch (SQLException e) {
            throw new DataSourceException(e);
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

  @Override
  public HashMap<TeamDraftOrder, PlayerDataSet> getAutoPickWizards() {
    String sql = "SELECT * FROM autoPickWizards";

    HashMap<TeamDraftOrder, PlayerDataSet> autoPickWizards = new HashMap<>();

    ResultSet resultSet = null;
    try {
      resultSet = executeQuery(sql);
      while (resultSet.next()) {
        TeamDraftOrder teamDraftOrder = getDraftOrderByTeamId(new TeamId(resultSet.getInt("teamID")));

        String dataSetName = resultSet.getString("WizardTable");

        if (dataSetName != null) {
          Optional<PlayerDataSet> dataSet = PlayerDataSet.getDataSetWithName(dataSetName);

          if (!dataSet.isPresent()) {
            throw new RuntimeException("Team " + teamDraftOrder + " is linked to unrecognized DataSet '" + dataSetName + "'.");
          }

          autoPickWizards.put(teamDraftOrder, dataSet.get());
        }
      }

    } catch (DataSourceException | SQLException e) {
      logger.log(Level.SEVERE, "Couldn't fetch team selections for which auto-pick source to use. Using default of CBSSPORTS, as backup.");
    } finally {
      close(resultSet);
    }

    return autoPickWizards;
  }

  @Override
  public TeamDraftOrder getDraftOrderByTeamId(TeamId teamID) throws DataSourceException {
    return getTeamIdDraftOrderMap().get(teamID);
  }

  @Override
  public TeamId getTeamIdByDraftOrder(TeamDraftOrder draftOrder) throws DataSourceException {
    return getTeamIdDraftOrderMap().inverse().get(draftOrder);
  }

  private BiMap<TeamId, TeamDraftOrder> getTeamIdDraftOrderMap() throws DataSourceException {
    if (teamIdDraftOrderMap == null) {
      synchronized (this) {
        if (teamIdDraftOrderMap == null) {
          teamIdDraftOrderMap = HashBiMap.create();
          String sql = "select id, DraftOrder " +
              "from teams";

          ResultSet resultSet = null;
          try {
            resultSet = executeQuery(sql);
            while (resultSet.next()) {
              teamIdDraftOrderMap.put(new TeamId(resultSet.getInt("id")),
                  new TeamDraftOrder(resultSet.getInt("DraftOrder")));
            }
          } catch (SQLException e) {
            throw new DataSourceException(e);
          } finally {
            close(resultSet);
          }

        }
      }
    }
    return teamIdDraftOrderMap;
  }

  @Override
  public void updateAutoPickWizard(TeamDraftOrder teamDraftOrder, PlayerDataSet wizardTable) {
    String sql = "UPDATE autoPickWizards " +
        "SET WizardTable = ? " +
        "WHERE TeamID = ?";
    String wizardTableName = wizardTable == null ? "" : wizardTable.getDisplayName();

    Statement statement = null;
    try {
      statement = prepareStatementUpdate(sql, wizardTableName,
          getTeamIdByDraftOrder(teamDraftOrder).get());
    } catch (DataSourceException | SQLException e) {
      logger.log(Level.SEVERE, "Unable to update auto-pick preference from user input, wizardTable is '" + wizardTableName + "'", e);
    } finally {
      close(statement);
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
        if ("".equals(param)) {
          preparedStatement.setNull(i, Types.VARCHAR);
        } else {
          preparedStatement.setString(i, (String) param);
        }
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