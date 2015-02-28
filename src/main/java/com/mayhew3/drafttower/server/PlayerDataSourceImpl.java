package com.mayhew3.drafttower.server;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayhew3.drafttower.shared.*;
import com.mayhew3.drafttower.shared.SharedModule.NumTeams;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

import static com.mayhew3.drafttower.shared.Position.REAL_POSITIONS;
import static java.util.logging.Level.*;

/**
 * Looks up players in the database.
 */
@Singleton
public class PlayerDataSourceImpl implements PlayerDataSource {

  private static final Logger logger = Logger.getLogger(PlayerDataSourceImpl.class.getName());

  private final DataSource db;
  private final BeanFactory beanFactory;
  private final TeamDataSource teamDataSource;
  private final int numTeams;

  @Inject
  public PlayerDataSourceImpl(DataSource db,
      BeanFactory beanFactory,
      TeamDataSource teamDataSource,
      @NumTeams int numTeams) {
    this.db = db;
    this.beanFactory = beanFactory;
    this.teamDataSource = teamDataSource;
    this.numTeams = numTeams;
  }

  @Override
  public List<Player> getPlayers(TeamId teamId, PlayerDataSet playerDataSet) throws DataSourceException {
    final List<Player> players = new ArrayList<>();
    try {
      getResultSetForPlayerRows(teamId, playerDataSet, new ResultSetCallback() {
        @Override
        public void onResultSet(ResultSet resultSet) throws SQLException, DataSourceException {
          while (resultSet.next()) {
            Player player = beanFactory.createPlayer().as();
            player.setPlayerId(resultSet.getInt("PlayerID"));
            player.setCBSId(resultSet.getInt("CBS_ID"));

            List<PlayerColumn> playerColumns = Lists.newArrayList(PlayerColumn.valuesForScoring());
            playerColumns.remove(PlayerColumn.NAME);
            PlayerColumn.NAME.set(player, resultSet.getString("LastName") + ", " + resultSet.getString("FirstName"));
            playerColumns.remove(PlayerColumn.PTS);

            for (PlayerColumn playerColumn : playerColumns) {
              if (playerColumn == PlayerColumn.WIZARD) {
                for (Position position : REAL_POSITIONS) {
                  String columnString = resultSet.getString(PlayerColumn.WIZARD.getColumnName() + position.getShortName());
                  if (columnString != null) {
                    PlayerColumn.setWizard(player, columnString, position);
                  }
                }
              } else if (playerColumn == PlayerColumn.ELIG) {
                String[] elig = resultSet.getString(playerColumn.getColumnName()).split(",");
                int indexOfDH = Arrays.binarySearch(elig, "DH");
                if (indexOfDH >= 0) {
                  System.arraycopy(elig, indexOfDH + 1, elig, indexOfDH, elig.length - (indexOfDH + 1));
                  elig[elig.length - 1] = "DH";
                }
                playerColumn.set(player, Joiner.on(",").join(elig));
              } else {
                String columnString = resultSet.getString(playerColumn.getColumnName());
                if (columnString != null) {
                  if (columnString.startsWith("0.")) {
                    columnString = columnString.substring(1);
                  }
                  playerColumn.set(player, columnString);
                }
              }
            }
            if (Scoring.POINTS) {
              // TODO m3: read points values from DB?
              if (player.getEligibility().contains("P")) {
                player.setPoints(String.format("%.1f",
                    parseFloatOrZero(player.getINN()) * 2.3f +
                        parseFloatOrZero(player.getHA()) * -0.5f +
                        parseFloatOrZero(player.getBBI()) * -1.5f +
                        parseFloatOrZero(player.getK()) * 2f +
                        parseFloatOrZero(player.getER()) * -1.5f +
                        parseFloatOrZero(player.getHRA()) * -2f +
                        parseFloatOrZero(player.getWL()) * 10f +
                        parseFloatOrZero(player.getS()) * 10f
                ));
              } else {
                player.setPoints(String.format("%.1f",
                    parseFloatOrZero(player.getAB()) * -2f +
                        parseFloatOrZero(player.getH()) * 6f +
                        parseFloatOrZero(player.get2B()) * 3f +
                        parseFloatOrZero(player.get3B()) * 5f +
                        parseFloatOrZero(player.getHR()) * 5f +
                        parseFloatOrZero(player.getRHR()) * 3f +
                        parseFloatOrZero(player.getRBI()) * 3f +
                        parseFloatOrZero(player.getSB()) * 2f +
                        parseFloatOrZero(player.getCS()) * -4f +
                        parseFloatOrZero(player.getBB()) * 3f
                ));
              }
            }

            String injury = resultSet.getString("Injury");
            if (injury != null) {
              player.setInjury(injury);
            }

            players.add(player);
          }
        }
      });
    } catch (SQLException e) {
      throw new DataSourceException("Error getting next element of results.", e);
    }
    return players;
  }

  private float parseFloatOrZero(String value) {
    if (value == null) {
      return 0f;
    }
    return Float.parseFloat(value);
  }

  @Override
  public ListMultimap<TeamDraftOrder, Integer> getAllKeepers() throws DataSourceException {
    final ListMultimap<TeamDraftOrder, Integer> keepers = ArrayListMultimap.create();

    String sql = "select TeamID, PlayerID from keepers";

    try {
      executeQuery(sql, new ResultSetCallback() {
        @Override
        public void onResultSet(ResultSet resultSet) throws SQLException, DataSourceException {
          while (resultSet.next()) {
            TeamId teamID = new TeamId(resultSet.getInt("TeamID"));
            int playerID = resultSet.getInt("PlayerID");
            keepers.put(teamDataSource.getDraftOrderByTeamId(teamID), playerID);
          }
        }
      });
    } catch (SQLException e) {
      throw new DataSourceException("Error retreiving keepers from database.", e);
    }
    return keepers;
  }

  // Player Queries

  private void getResultSetForPlayerRows(final TeamId teamID, PlayerDataSet playerDataSet, ResultSetCallback callback)
      throws SQLException, DataSourceException {

    String sql = "select * from ";
    sql = getFromJoins(teamID, sql, null, true, true);

    sql = addFilters(sql, playerDataSet);

    executeQuery(sql, callback);
  }


  // Unclaimed Player utility methods

  @SuppressWarnings("ConstantConditions")
  private String getFromJoins(TeamId teamID, String sql, String positionFilterString, boolean filterKeepers, boolean allWizardPositions) {
    String wizardFilterClause = "";
    String playerFilterClause = "";

    if (positionFilterString != null) {
      wizardFilterClause = " and Position " + positionFilterString + " ";
      playerFilterClause = " and pa.PlayerID IN (SELECT PlayerID FROM eligibilities WHERE Position " + positionFilterString + ") ";
    }


    String subselect = "(SELECT PlayerID, 'Pitcher' AS Role,\n" +
        " APP as G, NULL AS AB, \n" +
        (Scoring.CATEGORIES ? (
        "  NULL AS OBP,\n" +
        "  NULL AS SLG,\n" +
        "  NULL AS RHR,\n" +
        "  NULL AS RBI,\n" +
        "  NULL AS HR,\n" +
        "  NULL AS SBC,\n" +
        "  ROUND(INN, 0) AS INN, ROUND(ERA, 2) AS ERA, ROUND(WHIP, 3) AS WHIP, WL, K, S, "
        ) : (
        "  NULL AS H,\n" +
        "  NULL AS 2B,\n" +
        "  NULL AS 3B,\n" +
        "  NULL AS HR,\n" +
        "  NULL AS RHR,\n" +
        "  NULL AS RBI,\n" +
        "  NULL AS SB,\n" +
        "  NULL AS CS,\n" +
        "  NULL AS BB,\n" +
        "  ROUND(INN, 0) AS INN, HA, BBI, K, ER, HRA, WL, S, "
        )) +
        "Rank, Draft, DataSource, \n" +
        "  (select round(coalesce(max((Rating-1)*0.5), 0), 3)\n" +
        "   from wizardratings\n" +
        "   where projectionRow = projectionspitching.ID\n" +
        "   and batting = 0\n" +
        "  ) as Wizard" +
            (allWizardPositions ?
            ",\n" +
            "  (select round((Rating-1)*0.5, 3)\n" +
            "   from wizardratings\n" +
            "   where projectionRow = projectionspitching.ID\n" +
            "   and batting = 0\n" +
            "  ) as WizardP, " +
            getNullBattingWizardClauses()
            : "") +
        " FROM projectionspitching)\n" +
        " UNION\n" +
        " (SELECT PlayerID, 'Batter' AS Role,\n" +
        " G, AB, \n" +
        (Scoring.CATEGORIES ? (
        "  ROUND(OBP, 3) AS OBP, ROUND(SLG, 3) AS SLG, RHR, RBI, HR, SBC,\n" +
        "  NULL AS INN,\n" +
        "  NULL AS ERA,\n" +
        "  NULL AS WHIP,\n" +
        "  NULL AS WL,\n" +
        "  NULL AS K,\n" +
        "  NULL AS S,\n"
        ) : (
            "  H, 2B, 3B, HR, RHR, RBI, SB, CS, BB,\n" +
            "  NULL AS INN, " +
            "  NULL AS HA, " +
            "  NULL AS BBI," +
            "  NULL AS K," +
            "  NULL AS ER," +
            "  NULL AS HRA," +
            "  NULL AS WL," +
            "  NULL AS S, "
        )) +
        "  Rank, Draft, DataSource, \n" +
        "  (select round(coalesce(max(Rating), 0), 3) \n" +
        "   from wizardratings\n" +
        "   where projectionRow = projectionsbatting.ID\n" +
        "   and batting = 1 \n" +
        wizardFilterClause +
        "  ) as Wizard\n" +
        (allWizardPositions
            ? ",\n" +
            "  NULL as WizardP, \n" +
            getBattingWizardClauses()
            : "") +
        " FROM projectionsbatting)";

    sql +=
        "(SELECT p.PlayerString as Player, p.CBS_ID, p.FirstName, p.LastName, p.MLBTeam, p.Eligibility, \n" +
            " CASE Eligibility WHEN '' THEN 'DH' WHEN NULL THEN 'DH' ELSE Eligibility END as Position, \n" +
            "ds.name as Source, " +
            "cr.Rank as MyRank, " +
            " p.Injury,\n" +
            " pa.*\n" +
            "FROM (" + subselect + ") pa\n" +
            "INNER JOIN players p\n" +
            " ON pa.PlayerID = p.ID\n" +
            "INNER JOIN data_sources ds\n" +
            " ON pa.DataSource = ds.ID\n" +
            "INNER JOIN customrankings cr\n" +
            " ON cr.PlayerID = pa.PlayerID\n" +
            "WHERE cr.TeamID = " + teamID + " \n" +
            playerFilterClause;

    if (filterKeepers) {
      sql += " AND pa.PlayerID NOT IN (SELECT PlayerID FROM keepers) ";
    }

    sql += " ) p_all ";

    return sql;
  }

  private String getBattingWizardClauses() {
    StringBuilder builder = new StringBuilder();
    for (Position position : Position.BATTING_POSITIONS) {
      if (builder.length() > 0) {
        builder.append(", ");
      }
      builder.append("  (select round(Rating, 3)\n" +
              "   from wizardratings\n" +
              "   where projectionRow = projectionsbatting.ID\n" +
              "   and batting = 1 \n" +
              "   and Position = '")
          .append(position.getShortName())
          .append("'\n" +
              "  ) as Wizard")
          .append(position.getShortName())
          .append(" \n");
    }
    return builder.toString();
  }

  private String getNullBattingWizardClauses() {
    StringBuilder builder = new StringBuilder();
    for (Position position : Position.BATTING_POSITIONS) {
      if (builder.length() > 0) {
        builder.append(", ");
      }
      builder.append("  NULL as Wizard")
          .append(position.getShortName())
          .append(" \n");
    }
    return builder.toString();
  }

  private String addFilters(String sql, PlayerDataSet playerDataSet) {
    List<String> filters = new ArrayList<>();

    filters.add("(AB > 0 or INN > 0)");

    addDataSetFilter(filters, playerDataSet);

    if (filters.isEmpty()) {
      return sql;
    } else {
      return sql + " where " + Joiner.on(" and ").join(filters) + " ";
    }
  }

  private void addDataSetFilter(List<String> filters, PlayerDataSet playerDataSet) {
    String sourceFilter = playerDataSet.getDisplayName();
    if (sourceFilter != null) {
      filters.add("Source = '" + sourceFilter + "' ");
    }
  }

  private String addOrdering(TableSpec tableSpec, String sql) {
    PlayerColumn sortCol = tableSpec.getSortCol();
    String sortColumnName;
    String sortColumnDirection;
    if (sortCol != null) {
      sortColumnName = sortCol.getColumnName();
      sortColumnDirection = (tableSpec.isAscending() ? "asc " : "desc ");
    } else {
      throw new IllegalStateException("Expected tableSpec to always have non-null sort column.");
    }

    sql += " order by case when " + sortColumnName + " is null then 1 else 0 end, "
        + sortColumnName + " " + sortColumnDirection + " ";
    return sql;
  }

  // Player Rank

  @Override
  public void shiftInBetweenRanks(TeamId teamID, int lesserRank, int greaterRank, boolean increase) {
    String newRankForInbetween;
    if (increase) {
      newRankForInbetween = "Rank+1";
    } else {
      newRankForInbetween = "Rank-1";
    }

    String sql = "UPDATE customrankings SET Rank = " + newRankForInbetween +
        " WHERE TeamID = ? AND Rank BETWEEN ? AND ?";
    try {
      prepareStatementUpdate(sql, teamID, lesserRank, greaterRank);
    } catch (SQLException e) {
      logger.log(SEVERE, "Unable to shift ranks for intermediate players!", e);
    }
  }

  @Override
  public void updatePlayerRank(TeamId teamID, int newRank, long playerID) {
    String sql = "UPDATE customrankings SET Rank = ? WHERE TeamID = ? AND PlayerID = ?";
    try {
      prepareStatementUpdate(sql, newRank, teamID, playerID);
    } catch (SQLException e) {
      logger.log(SEVERE, "Unable to change rank for player!", e);
    }
  }

  @Override
  public void resetDraft() throws DataSourceException {
    String sql = "truncate table draftresults";

    try {
      executeUpdate(sql);
    } catch (SQLException e) {
      throw new DataSourceException(e);
    }
  }

  @Override
  public void populateQueueEntry(final QueueEntry queueEntry) throws DataSourceException {
    String sql = "select PlayerString,Eligibility " +
        "from players " +
        "where ID = " + queueEntry.getPlayerId();

    try {
      executeQuery(sql, new ResultSetCallback() {
        @Override
        public void onResultSet(ResultSet resultSet) throws SQLException, DataSourceException {
          resultSet.next();
          queueEntry.setPlayerName(resultSet.getString("PlayerString"));
          queueEntry.setEligibilities(
              RosterUtil.splitEligibilities(resultSet.getString("Eligibility")));
        }
      });
    } catch (SQLException e) {
      throw new DataSourceException(e);
    }
  }

  @Override
  public void populateDraftPick(final DraftPick draftPick) throws DataSourceException {
    String sql = "select FirstName,LastName,Eligibility " +
        "from players " +
        "where ID = " + draftPick.getPlayerId();

    try {
      executeQuery(sql, new ResultSetCallback() {
        @Override
        public void onResultSet(ResultSet resultSet) throws SQLException, DataSourceException {
          resultSet.next();
          draftPick.setPlayerName(
              resultSet.getString("FirstName") + " " + resultSet.getString("LastName"));
          draftPick.setEligibilities(
              RosterUtil.splitEligibilities(resultSet.getString("Eligibility")));
        }
      });
    } catch (SQLException e) {
      throw new DataSourceException(e);
    }
  }

  @Override
  public void postDraftPick(DraftPick draftPick, DraftStatus status) throws DataSourceException {
    int overallPick = status.getPicks().size();
    int round = (overallPick - 1) / numTeams + 1;
    int pick = ((overallPick - 1) % numTeams) + 1;

    long playerID = draftPick.getPlayerId();
    TeamId teamID = teamDataSource.getTeamIdByDraftOrder(new TeamDraftOrder(draftPick.getTeam()));

    String draftPosition = "'" + draftPick.getEligibilities().get(0) + "'";
    String sql = "INSERT INTO draftresults (Round, Pick, PlayerID, BackedOut, OverallPick, TeamID, DraftPos, Keeper) " +
        "VALUES (" + round + ", " + pick + ", " + playerID + ", 0, " + overallPick + ", " + teamID +
          ", " + draftPosition + ", " + (draftPick.isKeeper() ? "1" : "0") + ")";

    try {
      executeUpdate(sql);
    } catch (SQLException e) {
      throw new DataSourceException(e);
    }
  }

  @Override
  public void backOutLastDraftPick(int pickToRemove) throws DataSourceException {
    String sql = "UPDATE draftresults SET BackedOut = 1 WHERE OverallPick = " + pickToRemove;

    try {
      executeUpdate(sql);
    } catch (SQLException e) {
      throw new DataSourceException(e);
    }
  }

  @Override
  public void populateDraftStatus(final DraftStatus status) throws DataSourceException {
    String sql = "SELECT * from draftresultsload "
        + "ORDER BY Round, Pick";
    try {
      executeQuery(sql, new ResultSetCallback() {
        @Override
        public void onResultSet(ResultSet resultSet) throws SQLException, DataSourceException {
          while (resultSet.next()) {
            DraftPick pick = beanFactory.createDraftPick().as();
            pick.setPlayerId(resultSet.getInt("PlayerID"));
            pick.setPlayerName(resultSet.getString("PlayerName"));
            pick.setEligibilities(
                RosterUtil.splitEligibilities(resultSet.getString("Eligibility")));
            pick.setTeam(resultSet.getInt("DraftOrder"));
            pick.setKeeper(resultSet.getBoolean("Keeper"));
            status.getPicks().add(pick);
          }
        }
      });
    } catch (SQLException e) {
      throw new DataSourceException(e);
    }
  }

  @Override
  public void copyTableSpecToCustom(final TeamId teamID, TableSpec tableSpec) throws DataSourceException {
    PlayerColumn sortCol = tableSpec.getSortCol();
    if (sortCol == PlayerColumn.MYRANK) {
      logger.log(SEVERE, "Cannot set MyRank to MyRank column.");
      return;
    }

    logger.log(INFO, "Request from Team " + teamID + " to copy player ranks from " + tableSpec.getPlayerDataSet().getDisplayName()
        + ", " + sortCol.getShortName() + ".");

    try {
      prepareTmpTable(teamID);

      logger.log(FINE, "Cleared temp table for " + teamID);

      if (sortCol == PlayerColumn.PTS) {
        insertTempPointsRankings(teamID, tableSpec);
      } else {
        insertTempRankings(teamID, tableSpec);
      }

      logger.log(FINE, "Executed big insert for " + teamID);

      String sql = "select min(rank) as lower_bound, max(rank) as upper_bound \n" +
          "from tmp_rankings \n" +
          "where teamID = " + teamID;
      final int[] lowerBound = new int[1];
      final int[] upperBound = new int[1];
      executeQuery(sql, new ResultSetCallback() {
        @Override
        public void onResultSet(ResultSet resultSet) throws SQLException, DataSourceException {
          resultSet.next();

          logger.log(FINE, "Executed bounds query for " + teamID);

          lowerBound[0] = resultSet.getInt("lower_bound");
          upperBound[0] = resultSet.getInt("upper_bound");

          logger.log(FINE, "Got bounds off of result set.");
        }
      });


      int offset = lowerBound[0] - 1;

      sql = "update customrankings \n" +
          "set Rank = " + (upperBound[0] - offset + 1) + "\n" +
          "where teamid = " + teamID;
      executeUpdate(sql);

      logger.log(FINE, "Executed base update for " + teamID);

      sql = "update customrankings cr\n" +
          "inner join tmp_rankings tr\n" +
          " on (cr.PlayerID = tr.PlayerID and cr.TeamID = tr.TeamID)\n" +
          "set cr.Rank = tr.Rank - " + offset + " \n" +
          "where tr.teamID = " + teamID;
      executeUpdate(sql);
    } catch (SQLException e) {
      throw new DataSourceException(e);
    }

    logger.log(FINE, "Executed big update for " + teamID);
  }

  private void insertTempRankings(TeamId teamID, TableSpec tableSpec) throws SQLException {
    String sql = "INSERT INTO tmp_rankings (TeamID, PlayerID) \n" +
        " SELECT " + teamID + ", PlayerID \n" +
        " FROM ";
    sql = getFromJoins(teamID, sql, null, false, false);

    List<String> filters = new ArrayList<>();
    addDataSetFilter(filters, tableSpec.getPlayerDataSet());

    if (!filters.isEmpty()) {
      sql += " where " + Joiner.on(" and ").join(filters) + " ";
    }

    sql = addOrdering(tableSpec, sql);

    executeUpdate(sql);
  }

  private void insertTempPointsRankings(TeamId teamID, TableSpec tableSpec) throws SQLException, DataSourceException {
    StringBuilder sql = new StringBuilder("INSERT INTO tmp_rankings (TeamID, PlayerID) \n" +
        " VALUES ");

    List<Player> playersByPts = Ordering.from(PlayerColumn.PTS.getComparator(tableSpec.isAscending()))
        .sortedCopy(getPlayers(teamID, tableSpec.getPlayerDataSet()));
    boolean needsComma = false;
    for (Player player : playersByPts) {
      if (needsComma) {
        sql.append(",");
      }
      sql.append("(").append(teamID).append(",").append(player.getPlayerId()).append((")"));
      needsComma = true;
    }

    executeUpdate(sql.toString());
  }

  private void prepareTmpTable(TeamId teamID) throws SQLException {
    executeUpdate("delete from tmp_rankings where teamID = " + teamID);
  }

  @Override
  public GraphsData getGraphsData(TeamDraftOrder myTeam) throws DataSourceException {
    final TeamId teamId = teamDataSource.getTeamIdByDraftOrder(myTeam);
    String sql;
    if (Scoring.CATEGORIES) {
      sql = "select * from teamscoringwithzeroes where source = 'CBSSports'";
    } else {
      sql = "select TeamID, sum(p_all.Wizard) as pitching, sum(p_all.Wizard) as batting from ";
      sql = getFromJoins(teamId, sql, null, false, false);
      sql += " inner join draftresults on p_all.PlayerID = draftresults.PlayerID group by draftresults.TeamID";
    }

    GraphsData graphsData = beanFactory.createGraphsData().as();
    final Map<PlayerColumn, Float> myValues = new HashMap<>();
    graphsData.setMyValues(myValues);
    final Map<PlayerColumn, Float> avgValues = new HashMap<>();
    graphsData.setAvgValues(avgValues);
    final Map<String, Float> teamPitchingValues = new HashMap<>();
    graphsData.setTeamPitchingValues(teamPitchingValues);
    final Map<String, Float> teamBattingValues = new HashMap<>();
    graphsData.setTeamBattingValues(teamBattingValues);

    try {
      executeQuery(sql, new ResultSetCallback() {
        @Override
        public void onResultSet(ResultSet resultSet) throws SQLException, DataSourceException {
          while (resultSet.next()) {
            int resultTeam = resultSet.getInt("TeamID");
            if (Scoring.CATEGORIES) {
              for (PlayerColumn graphStat : GraphsData.GRAPH_STATS) {
                float value = resultSet.getFloat(graphStat.getColumnName());
                if (resultTeam == teamId.get()) {
                  myValues.put(graphStat, value);
                }
                if (!avgValues.containsKey(graphStat)) {
                  avgValues.put(graphStat, 0f);
                }
                avgValues.put(graphStat, avgValues.get(graphStat) + (value / numTeams));
              }
            } else {
              teamPitchingValues.put(Integer.toString(resultTeam),
                  resultSet.getFloat("pitching"));
              teamBattingValues.put(Integer.toString(resultTeam),
                  resultSet.getFloat("batting"));
            }
          }
        }
      });
    } catch (SQLException e) {
      throw new DataSourceException(e);
    }

    return graphsData;
  }

  // DB utility methods

  private void executeQuery(String sql, ResultSetCallback callback) throws SQLException, DataSourceException {
    try (Connection connection = db.getConnection()) {
      try (Statement statement = connection.createStatement()) {
        try (ResultSet resultSet = statement.executeQuery(sql)) {
          callback.onResultSet(resultSet);
        }
      }
    }
  }

  private void executeUpdate(String sql) throws SQLException {
    try (Connection connection = db.getConnection()) {
      try (Statement statement = connection.createStatement()) {
        statement.executeUpdate(sql);
      }
    }
  }

  protected void prepareStatementUpdate(String sql, Object... params) throws SQLException {
    try (Connection connection = db.getConnection()) {
      try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
        int i = 1;
        for (Object param : params) {
          if (param instanceof String) {
            preparedStatement.setString(i, (String) param);
          } else if (param instanceof Integer) {
            preparedStatement.setInt(i, (Integer) param);
          } else if (param instanceof Long) {
            preparedStatement.setLong(i, (Long) param);
          } else if (param instanceof IntWrapper) {
            preparedStatement.setInt(i, ((IntWrapper) param).get());
          } else {
            throw new RuntimeException("Unknown type of param: " + param.getClass());
          }
          i++;
        }
        preparedStatement.executeUpdate();
      }
    }
  }

}
