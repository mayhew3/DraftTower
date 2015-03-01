package com.mayhew3.drafttower.server;

import com.google.common.base.Optional;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.PlayerDataSet;
import com.mayhew3.drafttower.shared.Team;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    try {
      final int[] result = new int[1];
      executeQuery(sql, new ResultSetCallback() {
        @Override
        public void onResultSet(ResultSet resultSet) throws SQLException, DataSourceException {
          if (resultSet.next()) {
            result[0] = resultSet.getInt("DraftOrder");
          } else {
            result[0] = -1;
          }
        }
      });
      return result[0] == -1 ? null : new TeamDraftOrder(result[0]);
    } catch (SQLException e) {
      throw new DataSourceException("Cannot connect to login server.");
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

    try {
      final boolean[] result = new boolean[1];
      executeQuery(sql, new ResultSetCallback() {
        @Override
        public void onResultSet(ResultSet resultSet) throws SQLException, DataSourceException {
          result[0] = resultSet.next() && resultSet.getString("Role").equals("admin");
        }
      });
      return result[0];
    } catch (SQLException e) {
      throw new DataSourceException(e);
    }
  }

  @Override
  public Map<String, Team> getTeams() throws DataSourceException {
    if (teamNamesCache == null) {
      synchronized (this) {
        if (teamNamesCache == null) {
          final Builder<String, Team> teamNamesBuilder = ImmutableMap.builder();
          String sql = "select Name, users.FirstName, DraftOrder " +
              "from teams " +
              "inner join users " +
              "on teams.userid = users.user";
          try {
            executeQuery(sql, new ResultSetCallback() {
              @Override
              public void onResultSet(ResultSet resultSet) throws SQLException, DataSourceException {
                while (resultSet.next()) {
                  Team team = beanFactory.createTeam().as();
                  team.setShortName(resultSet.getString("FirstName"));
                  team.setLongName(resultSet.getString("Name"));
                  teamNamesBuilder.put(resultSet.getString("DraftOrder"), team);
                }
              }
            });
          } catch (SQLException e) {
            throw new DataSourceException(e);
          }
          teamNamesCache = teamNamesBuilder.build();
        }
      }
    }
    return teamNamesCache;
  }

  private void executeQuery(String sql, ResultSetCallback callback) throws SQLException, DataSourceException {
    try (Connection connection = db.getConnection()) {
      try (Statement statement = connection.createStatement()) {
        try (ResultSet resultSet = statement.executeQuery(sql)) {
          callback.onResultSet(resultSet);
        }
      }
    }
  }

  @Override
  public HashMap<TeamDraftOrder, PlayerDataSet> getAutoPickWizards() {
    String sql = "SELECT * FROM autopickwizards";
    final HashMap<TeamDraftOrder, PlayerDataSet> autoPickWizards = new HashMap<>();
    try {
      executeQuery(sql, new ResultSetCallback() {
        @Override
        public void onResultSet(ResultSet resultSet) throws SQLException, DataSourceException {
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
        }
      });
    } catch (DataSourceException | SQLException e) {
      logger.log(Level.SEVERE, "Couldn't fetch team selections for which auto-pick source to use. Using default of CBSSPORTS, as backup.");
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

  @Override
  public Map<TeamDraftOrder, Integer> getMinClosers() {
    // TODO(m3): read from DB
    return new ConcurrentHashMap<>();
  }

  @Override
  public Map<TeamDraftOrder, Integer> getMaxClosers() {
    // TODO(m3): read from DB
    return new ConcurrentHashMap<>();
  }

  @Override
  public void updateCloserLimits(TeamDraftOrder teamDraftOrder, int teamMinClosers, int teamMaxClosers) {
    // TODO(m3): update DB
  }

  private BiMap<TeamId, TeamDraftOrder> getTeamIdDraftOrderMap() throws DataSourceException {
    if (teamIdDraftOrderMap == null) {
      synchronized (this) {
        if (teamIdDraftOrderMap == null) {
          teamIdDraftOrderMap = HashBiMap.create();
          String sql = "select id, DraftOrder " +
              "from teams";

          try {
            executeQuery(sql, new ResultSetCallback() {
              @Override
              public void onResultSet(ResultSet resultSet) throws SQLException, DataSourceException {
                while (resultSet.next()) {
                  teamIdDraftOrderMap.put(new TeamId(resultSet.getInt("id")),
                      new TeamDraftOrder(resultSet.getInt("DraftOrder")));
                }
              }
            });
          } catch (SQLException e) {
            throw new DataSourceException(e);
          }

        }
      }
    }
    return teamIdDraftOrderMap;
  }

  @Override
  public void updateAutoPickWizard(TeamDraftOrder teamDraftOrder, PlayerDataSet wizardTable) {
    String sql = "UPDATE autopickwizards " +
        "SET WizardTable = ? " +
        "WHERE TeamID = ?";
    String wizardTableName = wizardTable == null ? "" : wizardTable.getDisplayName();

    try {
      prepareStatementUpdate(sql, wizardTableName, getTeamIdByDraftOrder(teamDraftOrder).get());
    } catch (DataSourceException | SQLException e) {
      logger.log(Level.SEVERE, "Unable to update auto-pick preference from user input, wizardTable is '" + wizardTableName + "'", e);
    }
  }


  protected void prepareStatementUpdate(String sql, Object... params) throws SQLException {
    try (Connection connection = db.getConnection()) {
      try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
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
        preparedStatement.executeUpdate();
      }
    }
  }
}