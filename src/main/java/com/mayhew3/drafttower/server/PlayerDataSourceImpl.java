package com.mayhew3.drafttower.server;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import com.mayhew3.drafttower.shared.*;
import com.mayhew3.drafttower.shared.SharedModule.NumTeams;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

import static com.mayhew3.drafttower.shared.Position.RS;
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
  private final RosterUtil rosterUtil;
  private final int numTeams;

  @Inject
  public PlayerDataSourceImpl(DataSource db,
      BeanFactory beanFactory,
      TeamDataSource teamDataSource,
      RosterUtil rosterUtil,
      @NumTeams int numTeams) {
    this.db = db;
    this.beanFactory = beanFactory;
    this.teamDataSource = teamDataSource;
    this.rosterUtil = rosterUtil;
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
            playerColumns.remove(PlayerColumn.WIZARD);

            for (PlayerColumn playerColumn : playerColumns) {
              if (playerColumn == PlayerColumn.ELIG) {
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

            String injury = resultSet.getString("Injury");
            if (injury != null) {
              player.setInjury(injury);
            }

            player.setFavorite(resultSet.getBoolean("Favorite"));

            players.add(player);
          }
        }
      });
    } catch (SQLException e) {
      throw new DataSourceException("Error getting next element of results.", e);
    }
    return players;
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
    sql = getFromJoins(teamID, sql, true);

    sql = addFilters(sql, playerDataSet);

    executeQuery(sql, callback);
  }


  // Unclaimed Player utility methods

  private String getFromJoins(TeamId teamID, String sql, boolean filterKeepers) {
    String playerFilterClause = "";

    String subselect = "(SELECT PlayerID, 'Pitcher' AS Role,\n" +
        " APP as G, NULL AS AB, \n" +
        "  NULL AS OBP,\n" +
        "  NULL AS SLG,\n" +
        "  NULL AS RBI,\n" +
        "  NULL AS HR,\n" +
        "  ROUND(ERA, 2) AS ERA, ROUND(WHIP, 3) AS WHIP,\n" +
        "  ROUND(INN, 0) AS INN, GS, K, S,\n" +
        (Scoring.CATEGORIES ? (
            "  NULL AS RHR,\n" +
            "  NULL AS SBC,\n" +
            "  WL, "
        ) : (
            "  NULL AS H,\n" +
            "  NULL AS R,\n" +
            "  NULL AS KO,\n" +
            "  NULL AS SB,\n" +
            "  NULL AS BB,\n" +
            "  NULL AS BA,\n" +
            "  W, L, HA, HRA, BBI, "
        )) +
        "  FPTS, Rank, Draft, DataSource \n" +
        " FROM projectionspitching)\n" +
        " UNION\n" +
        " (SELECT PlayerID, 'Batter' AS Role,\n" +
        " G, AB, \n" +
        "  ROUND(OBP, 3) AS OBP, ROUND(SLG, 3) AS SLG, RBI, HR,\n" +
        "  NULL AS GS,\n" +
        "  NULL AS INN,\n" +
        "  NULL AS ERA,\n" +
        "  NULL AS WHIP,\n" +
        "  NULL AS K,\n" +
        "  NULL AS S,\n" +
        (Scoring.CATEGORIES ? (
            "  RHR, SBC,\n" +
            "  NULL AS WL,\n"
        ) : (
            "  H, R, KO, SB, BB,\n" +
            "  ROUND(H / AB, 3) AS BA,\n" +
            "  NULL AS W," +
            "  NULL AS L," +
            "  NULL AS HA, " +
            "  NULL AS HRA, " +
            "  NULL AS BBI, "
        )) +
        "  FPTS, Rank, Draft, DataSource \n" +
        " FROM projectionsbatting)";

    sql +=
        "(SELECT p.PlayerString as Player, p.CBS_ID, p.FirstName, p.LastName, p.MLBTeam, p.Eligibility, \n" +
            " CASE Eligibility WHEN '' THEN 'DH' WHEN NULL THEN 'DH' ELSE Eligibility END as Position, \n" +
            "ds.name as Source, " +
            (teamID != null
                ? "cr.Rank as MyRank, " +
                    " f.Favorite,\n"
                : "Rank as MyRank, " +
                    " FALSE as Favorite,\n") +
            " p.Injury,\n" +
            " pa.*\n" +
            "FROM (" + subselect + ") pa\n" +
            "INNER JOIN players p\n" +
            " ON pa.PlayerID = p.id\n" +
            "INNER JOIN data_sources ds\n" +
            " ON pa.DataSource = ds.ID\n";
    if (teamID != null) {
      sql +=
          "INNER JOIN customrankings cr\n" +
              " ON cr.PlayerID = pa.PlayerID\n" +
              "LEFT JOIN favorites f\n" +
              " ON f.PlayerID = pa.PlayerID AND f.TeamID = " + teamID + "\n" +
              "WHERE cr.TeamID = " + teamID + " \n" +
              playerFilterClause;
    }

    if (filterKeepers) {
      sql += " AND pa.PlayerID NOT IN (SELECT PlayerID FROM keepers) ";
    }

    sql += " ) p_all ";

    return sql;
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
  public void addFavorite(TeamId teamID, long playerID) {
    String sql = "INSERT INTO favorites (TeamID, PlayerID, Favorite) VALUES (?, ?, TRUE)";
    try {
      prepareStatementUpdate(sql, teamID, playerID);
    } catch (SQLException e) {
      logger.log(SEVERE, "Unable to add favorite player!", e);
    }
  }

  @Override
  public void removeFavorite(TeamId teamID, long playerID) {
    String sql = "DELETE FROM favorites WHERE TeamID = ? AND PlayerID = ?";
    try {
      prepareStatementUpdate(sql, teamID, playerID);
    } catch (SQLException e) {
      logger.log(SEVERE, "Unable to add favorite player!", e);
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
        "where id = " + queueEntry.getPlayerId();

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
        "where id = " + draftPick.getPlayerId();

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
  public void postDraftPick(final DraftPick draftPick, DraftStatus status) throws DataSourceException {
    int overallPick = status.getPicks().size();
    int round = (overallPick - 1) / numTeams + 1;
    int pick = ((overallPick - 1) % numTeams) + 1;

    long playerID = draftPick.getPlayerId();
    TeamId teamID = teamDataSource.getTeamIdByDraftOrder(new TeamDraftOrder(draftPick.getTeam()));

    String insertSql = "INSERT INTO draftresults (Round, Pick, PlayerID, BackedOut, OverallPick, TeamID, Keeper) " +
        "VALUES (" + round + ", " + pick + ", " + playerID + ", 0, " + overallPick + ", " + teamID +
            ", " + (draftPick.isKeeper() ? "1" : "0") + ")";

    try {
      executeUpdate(insertSql);
    } catch (SQLException e) {
      throw new DataSourceException(e);
    }

    String updateSql = "UPDATE draftresults SET DraftPos = CASE PlayerID ";
    Multimap<Position, DraftPick> roster = rosterUtil.constructRoster(
        Lists.newArrayList(Iterables.filter(status.getPicks(), new Predicate<DraftPick>() {
          @Override
          public boolean apply(DraftPick input) {
            return input.getTeam() == draftPick.getTeam();
          }
        })));
    Multimap<DraftPick, Position> pickPositions = ArrayListMultimap.create();
    Multimaps.invertFrom(roster, pickPositions);
    String playerIds = "";
    for (DraftPick rosterPick : pickPositions.keySet()) {
      Collection<Position> positions = pickPositions.get(rosterPick);
      updateSql += "WHEN " + rosterPick.getPlayerId() + " THEN '" +
          Iterables.getFirst(positions, RS).getShortName() + "' ";
      if (playerIds.length() > 0) {
        playerIds += ",";
      }
      playerIds += rosterPick.getPlayerId();
    }
    updateSql += " END WHERE PlayerID in (" + playerIds + ")";
    try {
      executeUpdate(updateSql);
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

      insertTempRankings(teamID, tableSpec);

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
    sql = getFromJoins(teamID, sql, false);

    List<String> filters = new ArrayList<>();
    addDataSetFilter(filters, tableSpec.getPlayerDataSet());

    if (!filters.isEmpty()) {
      sql += " where " + Joiner.on(" and ").join(filters) + " ";
    }

    sql = addOrdering(tableSpec, sql);

    executeUpdate(sql);
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
      sql = "select TeamID, " +
          " sum(case when p_all.Role = 'Pitcher' then p_all.FPTS else 0 end) as pitching, " +
          " sum(case when p_all.Role = 'Batter' then p_all.FPTS else 0 end) as batting, " +
          " sum(p_all.FPTS) as total " +
          " from ";
      sql = getFromJoins(teamId, sql, false);
      sql += " inner join draftresults on p_all.PlayerID = draftresults.PlayerID ";
      sql += " where Source = 'CBSSports' and BackedOut = 0 AND DraftPos <> 'RS' ";
      sql +=    "group by draftresults.TeamID";
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
    final Map<String, Float> teamTotals = new HashMap<>();
    graphsData.setTeamTotals(teamTotals);

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
              String teamId = Integer.toString(teamDataSource.getDraftOrderByTeamId(new TeamId(resultTeam)).get());
              teamPitchingValues.put(teamId, resultSet.getFloat("pitching"));
              teamBattingValues.put(teamId, resultSet.getFloat("batting"));
              teamTotals.put(teamId, resultSet.getFloat("total"));
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
