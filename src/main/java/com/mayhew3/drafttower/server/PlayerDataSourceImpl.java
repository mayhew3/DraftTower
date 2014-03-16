package com.mayhew3.drafttower.server;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayhew3.drafttower.server.BindingAnnotations.TeamTokens;
import com.mayhew3.drafttower.shared.*;
import com.mayhew3.drafttower.shared.SharedModule.NumTeams;

import javax.servlet.ServletException;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static com.mayhew3.drafttower.shared.Position.*;
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
  private final Map<String, TeamDraftOrder> teamTokens;
  private final int numTeams;

  private final Map<String, List<Player>> cache = new ConcurrentHashMap<>();

  @Inject
  public PlayerDataSourceImpl(DataSource db,
      BeanFactory beanFactory,
      TeamDataSource teamDataSource,
      @TeamTokens Map<String, TeamDraftOrder> teamTokens,
      @NumTeams int numTeams) {
    this.db = db;
    this.beanFactory = beanFactory;
    this.teamDataSource = teamDataSource;
    this.teamTokens = teamTokens;
    this.numTeams = numTeams;
  }

  @Override
  public UnclaimedPlayerListResponse lookupUnclaimedPlayers(UnclaimedPlayerListRequest request)
      throws ServletException {
    Stopwatch stopwatch = new Stopwatch().start();
    UnclaimedPlayerListResponse response = beanFactory.createUnclaimedPlayerListResponse().as();

    List<Player> players;
    if (cache.containsKey(getKey(request.getTableSpec()))) {
      players = cache.get(getKey(request.getTableSpec()));
    } else {
      players = new ArrayList<>();

      ResultSet resultSet = null;
      try {
        TeamId team = teamDataSource.getTeamIdByDraftOrder(teamTokens.get(request.getTeamToken()));
        resultSet = getResultSetForUnclaimedPlayerRows(request, team);
        while (resultSet.next()) {
          Player player = beanFactory.createPlayer().as();
          player.setPlayerId(resultSet.getInt("PlayerID"));
          player.setCBSId(resultSet.getInt("CBS_ID"));

          List<PlayerColumn> playerColumns = Lists.newArrayList(PlayerColumn.values());
          playerColumns.remove(PlayerColumn.NAME);
          PlayerColumn.NAME.set(player, resultSet.getString("LastName") + ", " + resultSet.getString("FirstName"));

          for (PlayerColumn playerColumn : playerColumns) {
            if (playerColumn == PlayerColumn.WIZARD) {
              for (Position position : REAL_POSITIONS) {
                String columnString = resultSet.getString(PlayerColumn.WIZARD.getColumnName() + position.getShortName());
                if (columnString != null) {
                  PlayerColumn.setWizard(player, columnString, position);
                }
              }
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

          players.add(player);
        }
        cache.put(getKey(request.getTableSpec()), players);
      } catch (SQLException e) {
        throw new ServletException("Error getting next element of results.", e);
      } finally {
        close(resultSet);
      }
    }

    response.setPlayers(players);

    stopwatch.stop();
    logger.info("Player table request took " + stopwatch.elapsedMillis() + "ms");
    return response;
  }

  @Override
  public ListMultimap<TeamDraftOrder, Integer> getAllKeepers() throws ServletException {
    ListMultimap<TeamDraftOrder, Integer> keepers = ArrayListMultimap.create();

    String sql = "select TeamID, PlayerID from Keepers";

    ResultSet resultSet = null;
    try {
      resultSet = executeQuery(sql);
      while (resultSet.next()) {
        TeamId teamID = new TeamId(resultSet.getInt("TeamID"));
        int playerID = resultSet.getInt("PlayerID");
        keepers.put(teamDataSource.getDraftOrderByTeamId(teamID), playerID);
      }
    } catch (SQLException e) {
      throw new ServletException("Error retreiving keepers from database.", e);
    } finally {
      close(resultSet);
    }
    return keepers;
  }



  // Unclaimed Player Queries

  private ResultSet getResultSetForUnclaimedPlayerRows(UnclaimedPlayerListRequest request, final TeamId teamID)
      throws SQLException {

    TableSpec tableSpec = request.getTableSpec();

    String sql = "select * from ";
    sql = getFromJoins(teamID, sql, null, true, true);

    sql = addFilters(request, sql);

    sql = addOrdering(tableSpec, sql);

    return executeQuery(sql);
  }

  @Override
  public long getBestPlayerId(PlayerDataSet wizardTable, TeamDraftOrder teamDraftOrder, Set<Position> openPositions) throws SQLException {
    TeamId teamId = teamDataSource.getTeamIdByDraftOrder(teamDraftOrder);

    String sql = "select PlayerID, Eligibility from ";
    sql = getFromJoins(teamId, sql, createFilterStringFromPositions(openPositions), true, false);

    List<String> filters = new ArrayList<>();
    addDataSetFilter(filters, wizardTable);

    if (!filters.isEmpty()) {
      sql += " where " + Joiner.on(" and ").join(filters) + " ";
    }

    if (wizardTable == null) {
      sql += " order by MyRank asc";
    } else {
      String wizardColumnName = PlayerColumn.WIZARD.getColumnName();
      sql += " order by case when " + wizardColumnName + " is null then 1 else 0 end, " + wizardColumnName + " desc ";
    }

    ResultSet resultSet = null;
    try {
      resultSet = executeQuery(sql);
      Long firstReserve = null;
      while (resultSet.next()) {
        if (firstReserve == null) {
          firstReserve = resultSet.getLong("PlayerID");
          if (openPositions.isEmpty()) {
            return firstReserve;
          }
        }
        List<String> eligibility = splitEligibilities(resultSet.getString("Eligibility"));
        if (!eligibility.contains("P") && openPositions.contains(DH)) {
          return resultSet.getLong("PlayerID");
        }
        for (String position : eligibility) {
          if (openPositions.contains(Position.fromShortName(position))) {
            return resultSet.getLong("PlayerID");
          }
        }
      }
      //noinspection ConstantConditions
      return firstReserve;
    } finally {
      close(resultSet);
    }
  }


  // Unclaimed Player utility methods

  private String getFromJoins(TeamId teamID, String sql, String positionFilterString, boolean filterClaimed, boolean allWizardPositions) {
    String wizardFilterClause = "";
    String playerFilterClause = "";

    if (positionFilterString != null) {
      wizardFilterClause = " and Position " + positionFilterString + " ";
      playerFilterClause = " and pa.PlayerID IN (SELECT PlayerID FROM Eligibilities WHERE Position " + positionFilterString + ") ";
    }


    String subselect = "(SELECT PlayerID, 'Pitcher' AS Role,\n" +
        " APP as G, NULL AS AB, \n" +
        "  NULL AS OBP,\n" +
        "  NULL AS SLG,\n" +
        "  NULL AS RHR,\n" +
        "  NULL AS RBI,\n" +
        "  NULL AS HR,\n" +
        "  NULL AS SBC,\n" +
        "  ROUND(INN, 0) AS INN, ROUND(ERA, 2) AS ERA, ROUND(WHIP, 3) AS WHIP, WL, K, S, Rank, Draft, DataSource, \n" +
        "  (select round(coalesce(max(Rating), 0), 3)\n" +
        "   from wizardRatings\n" +
        "   where projectionRow = projectionsPitching.ID\n" +
        "   and batting = 0\n" +
        "  ) as Wizard" +
            (allWizardPositions ?
            ",\n" +
            "  (select round(Rating, 3)\n" +
            "   from wizardRatings\n" +
            "   where projectionRow = projectionsPitching.ID\n" +
            "   and batting = 0\n" +
            "  ) as WizardP, " +
            getNullBattingWizardClauses()
            : "") +
        " FROM projectionsPitching)\n" +
        " UNION\n" +
        " (SELECT PlayerID, 'Batter' AS Role,\n" +
        " G, AB, \n" +
        "  ROUND(OBP, 3) AS OBP, ROUND(SLG, 3) AS SLG, RHR, RBI, HR, SBC,\n" +
        "  NULL AS INN,\n" +
        "  NULL AS ERA,\n" +
        "  NULL AS WHIP,\n" +
        "  NULL AS WL,\n" +
        "  NULL AS K,\n" +
        "  NULL AS S,\n" +
        "  Rank, Draft, DataSource, \n" +
        "  (select round(coalesce(max(Rating), 0), 3) \n" +
        "   from wizardRatings\n" +
        "   where projectionRow = projectionsBatting.ID\n" +
        "   and batting = 1 \n" +
        wizardFilterClause +
        "  ) as Wizard\n" +
        (allWizardPositions
            ? ",\n" +
            "  NULL as WizardP, \n" +
            getBattingWizardClauses()
            : "") +
        " FROM projectionsBatting)";

    sql +=
        "(SELECT p.PlayerString as Player, p.CBS_ID, p.FirstName, p.LastName, p.MLBTeam, p.Eligibility, \n" +
            " CASE Eligibility WHEN '' THEN 'DH' WHEN NULL THEN 'DH' ELSE Eligibility END as Position, \n" +
            "ds.name as Source, " +
            "cr.Rank as MyRank, " +
            " p.Injury,\n" +
            " pa.*\n" +
            "FROM (" + subselect + ") pa\n" +
            "INNER JOIN Players p\n" +
            " ON pa.PlayerID = p.ID\n" +
            "INNER JOIN data_sources ds\n" +
            " ON pa.DataSource = ds.ID\n" +
            "INNER JOIN customRankings cr\n" +
            " ON cr.PlayerID = pa.PlayerID\n" +
            "WHERE cr.TeamID = " + teamID + " \n" +
            playerFilterClause;

    if (filterClaimed) {
      sql += " AND pa.PlayerID NOT IN (SELECT PlayerID FROM DraftResults WHERE BackedOut = 0)\n" +
          " AND pa.PlayerID NOT IN (SELECT PlayerID FROM Keepers) ";
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
              "   from wizardRatings\n" +
              "   where projectionRow = projectionsBatting.ID\n" +
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

  private String addFilters(UnclaimedPlayerListRequest request, String sql) {
    List<String> filters = new ArrayList<>();

    filters.add("(AB > 0 or INN > 0)");

    addTableSpecFilter(filters, request.getTableSpec());

    if (filters.isEmpty()) {
      return sql;
    } else {
      return sql + " where " + Joiner.on(" and ").join(filters) + " ";
    }
  }

  private void addTableSpecFilter(List<String> filters, TableSpec tableSpec) {
    String sourceFilter = tableSpec.getPlayerDataSet().getDisplayName();
    if (sourceFilter != null) {
      filters.add("Source = '" + sourceFilter + "' ");
    }
  }

  private void addDataSetFilter(List<String> filters, PlayerDataSet playerDataSet) {
    if (playerDataSet == null) {
      filters.add("Source = '" + PlayerDataSet.CBSSPORTS.getDisplayName() + "' ");
    } else {
      filters.add("Source = '" + playerDataSet.getDisplayName() + "' ");
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


  // Open Positions

  private String createFilterStringFromPositions(Set<Position> openPositions) {
    // needs no filter if all positions are open, {P, DH} are open, or all positions are full (reserve time!)
    if (openPositions.isEmpty() || hasAllOpenPositions(openPositions)) {
      return null;
    } else {
      String joined = Joiner.on(',').join(Iterables.transform(openPositions, new Function<Position, String>() {
        @Override
        public String apply(Position input) {
          return "'" + input.getShortName() + "'";
        }
      }));
      return " in (" + joined + ") ";
    }
  }

  private boolean hasAllOpenPositions(Set<Position> openPositions) {
    return openPositions.size() == Position.REAL_POSITIONS.size()
        || (openPositions.contains(DH) && openPositions.contains(P));
  }




  // Player Rank

  @Override
  public void changePlayerRank(ChangePlayerRankRequest request) throws ServletException {
    try {
      if (teamTokens.containsKey(request.getTeamToken())) {
        TeamId teamID = teamDataSource.getTeamIdByDraftOrder(teamTokens.get(request.getTeamToken()));
        long playerId = request.getPlayerId();
        int prevRank = request.getPrevRank();
        int newRank = request.getNewRank();

        logger.info("Change player rank for team " + teamID
            + " player " + playerId + " from rank " + prevRank + " to new rank " + newRank);

        shiftInBetweenRanks(teamID, prevRank, newRank);
        updatePlayerRank(teamID, newRank, playerId);
      }
    } catch (SQLException e) {
      throw new ServletException(e);
    }
  }

  private void shiftInBetweenRanks(TeamId teamID, int prevRank, int newRank) {
    String newRankForInbetween = "Rank-1";
    int lesserRank = prevRank+1;
    int greaterRank = newRank;

    if (prevRank > newRank) {
      newRankForInbetween = "Rank+1";
      lesserRank = newRank;
      greaterRank = prevRank-1;
    }

    String sql = "UPDATE CustomRankings SET Rank = " + newRankForInbetween +
        " WHERE TeamID = ? AND Rank BETWEEN ? AND ?";
    Statement statement = null;
    try {
      statement = prepareStatementUpdate(sql, teamID, lesserRank, greaterRank);
    } catch (SQLException e) {
      logger.log(SEVERE, "Unable to shift ranks for intermediate players!", e);
    } finally {
      close(statement);
    }
  }

  private void updatePlayerRank(TeamId teamID, int newRank, long playerID) {
    String sql = "UPDATE CustomRankings SET Rank = ? WHERE TeamID = ? AND PlayerID = ?";
    Statement statement = null;
    try {
      statement = prepareStatementUpdate(sql, newRank, teamID, playerID);
    } catch (SQLException e) {
      logger.log(SEVERE, "Unable to change rank for player!", e);
    } finally {
      close(statement);
    }
  }



  @Override
  public void populateQueueEntry(QueueEntry queueEntry) throws SQLException {
    String sql = "select PlayerString,Eligibility " +
        "from Players " +
        "where ID = " + queueEntry.getPlayerId();

    ResultSet resultSet = executeQuery(sql);
    try {
      resultSet.next();
      queueEntry.setPlayerName(resultSet.getString("PlayerString"));
      queueEntry.setEligibilities(
          splitEligibilities(resultSet.getString("Eligibility")));
    } finally {
      close(resultSet);
    }
  }

  @Override
  public void populateDraftPick(DraftPick draftPick) throws SQLException {
    String sql = "select FirstName,LastName,Eligibility " +
        "from AllPlayers " +
        "where ID = " + draftPick.getPlayerId();

    ResultSet resultSet = executeQuery(sql);
    try {
      resultSet.next();
      draftPick.setPlayerName(
          resultSet.getString("FirstName") + " " + resultSet.getString("LastName"));
      draftPick.setEligibilities(
          splitEligibilities(resultSet.getString("Eligibility")));
    } finally {
      close(resultSet);
    }
  }

  @Override
  public void postDraftPick(DraftPick draftPick, DraftStatus status) throws SQLException {
    int overallPick = status.getPicks().size();
    int round = (overallPick - 1) / numTeams + 1;
    int pick = ((overallPick-1) % numTeams) + 1;

    long playerID = draftPick.getPlayerId();
    TeamId teamID = teamDataSource.getTeamIdByDraftOrder(new TeamDraftOrder(draftPick.getTeam()));

    String draftPosition = "'" + draftPick.getEligibilities().get(0) + "'";
    String sql = "INSERT INTO DraftResults (Round, Pick, PlayerID, BackedOut, OverallPick, TeamID, DraftPos, Keeper) " +
        "VALUES (" + round + ", " + pick + ", " + playerID + ", 0, " + overallPick + ", " + teamID +
          ", " + draftPosition + ", " + (draftPick.isKeeper() ? "1" : "0") + ")";

    Statement statement = null;
    try {
      statement = executeUpdate(sql);
    } finally {
      close(statement);
    }
  }

  @Override
  public void backOutLastDraftPick(int pickToRemove) throws SQLException {
    String sql = "UPDATE DraftResults SET BackedOut = 1 WHERE OverallPick = " + pickToRemove;

    Statement statement = null;
    try {
      statement = executeUpdate(sql);
    } finally {
      close(statement);
    }
  }

  @Override
  public void populateDraftStatus(DraftStatus status) throws SQLException {
    String sql = "SELECT * from DraftResultsLoad "
        + "ORDER BY Round, Pick";
    ResultSet resultSet = executeQuery(sql);
    try {
      while (resultSet.next()) {
        DraftPick pick = beanFactory.createDraftPick().as();
        pick.setPlayerId(resultSet.getInt("PlayerID"));
        pick.setPlayerName(resultSet.getString("PlayerName"));
        pick.setEligibilities(
            splitEligibilities(resultSet.getString("Eligibility")));
        pick.setTeam(resultSet.getInt("DraftOrder"));
        pick.setKeeper(resultSet.getBoolean("Keeper"));
        status.getPicks().add(pick);
      }
    } finally {
      close(resultSet);
    }
  }

  @Override
  public void copyTableSpecToCustom(CopyAllPlayerRanksRequest request) throws SQLException {
    final TeamId teamID = teamDataSource.getTeamIdByDraftOrder(teamTokens.get(request.getTeamToken()));
    TableSpec tableSpec = request.getTableSpec();

    if (tableSpec.getSortCol() == PlayerColumn.MYRANK) {
      logger.log(SEVERE, "Cannot set MyRank to MyRank column.");
      return;
    }

    logger.log(INFO, "Request from Team " + teamID + " to copy player ranks from " + tableSpec.getPlayerDataSet().getDisplayName()
        + ", " + tableSpec.getSortCol().getShortName() + ".");

    prepareTmpTable(teamID);

    logger.log(FINE, "Cleared temp table for " + teamID);

    String sql = "INSERT INTO tmp_rankings (TeamID, PlayerID) \n" +
        " SELECT " + teamID + ", PlayerID \n" +
        " FROM ";
    sql = getFromJoins(teamID, sql, null, false, false);

    List<String> filters = new ArrayList<>();
    addTableSpecFilter(filters, tableSpec);

    if (!filters.isEmpty()) {
      sql += " where " + Joiner.on(" and ").join(filters) + " ";
    }

    sql = addOrdering(tableSpec, sql);

    Statement statement = executeUpdate(sql);
    close(statement);

    logger.log(FINE, "Executed big insert for " + teamID);

    sql = "select min(rank) as lower_bound, max(rank) as upper_bound \n" +
        "from tmp_rankings \n" +
        "where teamID = " + teamID;
    ResultSet resultSet = executeQuery(sql);
    resultSet.next();

    logger.log(FINE, "Executed bounds query for " + teamID);

    int lowerBound = resultSet.getInt("lower_bound");
    int upperBound = resultSet.getInt("upper_bound");

    logger.log(FINE, "Got bounds off of result set.");

    close(resultSet);

    logger.log(FINE, "Closed bounds connections.");

    int offset = lowerBound - 1;

    sql = "update customRankings \n" +
        "set Rank = " + (upperBound - offset + 1) + "\n" +
        "where teamid = " + teamID;
    statement = executeUpdate(sql);
    close(statement);

    logger.log(FINE, "Executed base update for " + teamID);

    sql = "update customRankings cr\n" +
        "inner join tmp_rankings tr\n" +
        " on (cr.PlayerID = tr.PlayerID and cr.TeamID = tr.TeamID)\n" +
        "set cr.Rank = tr.Rank - " + offset + " \n" +
        "where tr.teamID = " + teamID;
    statement = executeUpdate(sql);
    close(statement);

    logger.log(FINE, "Executed big update for " + teamID);
  }


  private void prepareTmpTable(TeamId teamID) throws SQLException {
    executeUpdate("delete from tmp_rankings where teamID = " + teamID);
  }


  @Override
  public GraphsData getGraphsData(TeamDraftOrder teamDraftOrder) throws SQLException {
    TeamId teamId = teamDataSource.getTeamIdByDraftOrder(teamDraftOrder);
    String sql = "select * from teamscoringwithzeroes";

    GraphsData graphsData = beanFactory.createGraphsData().as();
    Map<PlayerColumn, Float> myValues = new HashMap<>();
    graphsData.setMyValues(myValues);
    Map<PlayerColumn, Float> avgValues = new HashMap<>();
    graphsData.setAvgValues(avgValues);

    ResultSet resultSet = executeQuery(sql);
    try {
      while (resultSet.next()) {
        int resultTeam = resultSet.getInt("TeamID");
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
      }
    } finally {
      close(resultSet);
    }

    return graphsData;
  }

  private static List<String> splitEligibilities(String eligibility) {
    return eligibility.isEmpty()
        ? Lists.newArrayList("DH")
        : Lists.newArrayList(eligibility.split(","));
  }



  // DB utility methods

  private ResultSet executeQuery(String sql) throws SQLException {
    Statement statement = db.getConnection().createStatement();
    return statement.executeQuery(sql);
  }

  private Statement executeUpdate(String sql) throws SQLException {
    Statement statement = db.getConnection().createStatement();
    statement.executeUpdate(sql);
    return statement;
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
      } else {
        throw new RuntimeException("Unknown type of param: " + param.getClass());
      }
      i++;
    }
    return preparedStatement;
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
      logger.log(SEVERE, "Unable to close SQL connection after use.", e);
    }
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

  private static String getKey(TableSpec tableSpec) {
    return tableSpec.getPlayerDataSet().ordinal() + ""
        + tableSpec.getSortCol().ordinal() + ""
        + (tableSpec.isAscending() ? "a" : "d");
  }
}