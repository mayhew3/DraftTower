package com.mayhew3.drafttower.server;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayhew3.drafttower.server.BindingAnnotations.TeamTokens;
import com.mayhew3.drafttower.shared.*;
import com.mayhew3.drafttower.shared.SharedModule.NumTeams;

import javax.servlet.ServletException;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
  private final DraftStatus draftStatus;
  private final Map<String, Integer> teamTokens;
  private int numTeams;

  @Inject
  public PlayerDataSourceImpl(DataSource db,
      BeanFactory beanFactory,
      DraftStatus draftStatus,
      @TeamTokens Map<String, Integer> teamTokens,
      @NumTeams int numTeams) {
    this.db = db;
    this.beanFactory = beanFactory;
    this.draftStatus = draftStatus;
    this.teamTokens = teamTokens;
    this.numTeams = numTeams;
  }

  @Override
  public UnclaimedPlayerListResponse lookupUnclaimedPlayers(UnclaimedPlayerListRequest request)
      throws ServletException {
    UnclaimedPlayerListResponse response = beanFactory.createUnclaimedPlayerListResponse().as();

    Integer team = teamTokens.get(request.getTeamToken());

    List<Player> players = Lists.newArrayList();

    ResultSet resultSet = null;
    try {
      resultSet = getResultSetForUnclaimedPlayerRows(request, team);
      while (resultSet.next()) {
        Player player = beanFactory.createPlayer().as();
        player.setPlayerId(resultSet.getInt("PlayerID"));
        ImmutableMap.Builder<PlayerColumn, String> columnMap = ImmutableMap.builder();

        PlayerColumn[] playerColumns = PlayerColumn.values();
        for (PlayerColumn playerColumn : playerColumns) {
          String columnString = resultSet.getString(playerColumn.getColumnName());
          if (columnString != null) {
            columnMap.put(playerColumn, columnString);
          }
        }

        player.setColumnValues(columnMap.build());

        String injury = resultSet.getString("Injury");
        if (injury != null) {
          player.setInjury(injury);
        }

        players.add(player);
      }
    } catch (SQLException e) {
      throw new ServletException("Error getting next element of results.", e);
    } finally {
      close(resultSet);
    }

    response.setPlayers(players);
    response.setTotalPlayers(getTotalUnclaimedPlayerCount(request, team));

    return response;
  }

  @Override
  public ListMultimap<Integer, Integer> getAllKeepers() throws ServletException {
    ListMultimap<Integer, Integer> keepers = ArrayListMultimap.create();

    String sql = "select TeamID, PlayerID from Keepers";

    ResultSet resultSet = null;
    try {
      resultSet = executeQuery(sql);
      while (resultSet.next()) {
        int teamID = resultSet.getInt("TeamID");
        int playerID = resultSet.getInt("PlayerID");
        keepers.put(teamID, playerID);
      }
    } catch (SQLException e) {
      throw new ServletException("Error retreiving keepers from database.", e);
    } finally {
      close(resultSet);
    }
    return keepers;
  }



  // Unclaimed Player Queries

  private int getTotalUnclaimedPlayerCount(UnclaimedPlayerListRequest request, final int team) throws ServletException {

    String sql = "select count(1) as TotalPlayers from ";
    sql = getFromJoins(team, sql, getPositionFilterClause(request, team), true);

    sql = addFilters(request, sql);

    ResultSet resultSet = null;
    try {
      resultSet = executeQuery(sql);
      resultSet.next();
      return resultSet.getInt("TotalPlayers");
    } catch (SQLException e) {
      throw new ServletException("Couldn't find number of rows in table.", e);
    } finally {
      close(resultSet);
    }
  }

  private ResultSet getResultSetForUnclaimedPlayerRows(UnclaimedPlayerListRequest request, final int team)
      throws SQLException {

    TableSpec tableSpec = request.getTableSpec();

    String sql = "select * from ";
    sql = getFromJoins(team, sql, getPositionFilterClause(request, team), true);

    sql = addFilters(request, sql);

    sql = addOrdering(tableSpec, sql);
    sql += " limit " + request.getRowStart() + ", " + request.getRowCount();

    return executeQuery(sql);
  }

  @Override
  public long getBestPlayerId(final Integer team, Set<Position> openPositions) throws SQLException {

    String sql = "select PlayerID, Eligibility from ";
    sql = getFromJoins(team, sql, createFilterStringFromPositions(openPositions), true);

    List<String> filters = Lists.newArrayList();
    if (!filters.isEmpty()) {
      sql += " where " + Joiner.on(" and ").join(filters) + " ";
    }

    sql += "order by MyRank asc;";

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

  private String getFromJoins(int team, String sql, String positionFilterString, boolean filterClaimed) {
    String wizardFilterClause = "";
    String playerFilterClause = "";

    if (positionFilterString != null) {
      wizardFilterClause = " and Position " + positionFilterString + " ";
      playerFilterClause = " and pa.PlayerID IN (SELECT PlayerID FROM Eligibilities WHERE Position " + positionFilterString + ") ";
    }


    String subselect = "(SELECT PlayerID, 'Pitcher' AS Role,\n" +
        "  NULL AS OBP,\n" +
        "  NULL AS SLG,\n" +
        "  NULL AS RHR,\n" +
        "  NULL AS RBI,\n" +
        "  NULL AS HR,\n" +
        "  NULL AS SBC,\n" +
        "  ROUND(INN, 1) AS INN, ROUND(ERA, 2) AS ERA, ROUND(WHIP, 3) AS WHIP, WL, K, S, Rank, DataSource, \n" +
        "  (select coalesce(max(Rating), 0)\n" +
        "   from wizardRatings\n" +
        "   where projectionRow = projectionsPitching.ID\n" +
        "   and batting = 0\n" +
        "  ) as Wizard " +
        " FROM projectionsPitching)\n" +
        " UNION\n" +
        " (SELECT PlayerID, 'Batter' AS Role,\n" +
        "  ROUND(OBP, 3) AS OBP, ROUND(SLG, 3) AS SLG, RHR, RBI, HR, SBC,\n" +
        "  NULL AS INN,\n" +
        "  NULL AS ERA,\n" +
        "  NULL AS WHIP,\n" +
        "  NULL AS WL,\n" +
        "  NULL AS K,\n" +
        "  NULL AS S,\n" +
        "  Rank, DataSource, \n" +
        "  (select coalesce(max(Rating), 0)\n" +
        "   from wizardRatings\n" +
        "   where projectionRow = projectionsBatting.ID\n" +
        "   and batting = 1 \n" +
        wizardFilterClause +
        "  ) as Wizard \n" +
        " FROM projectionsBatting)";

    sql +=
        "(SELECT p.PlayerString as Player, p.FirstName, p.LastName, p.MLBTeam, p.Eligibility, \n" +
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
            "WHERE cr.TeamID = " + team + " \n" +
            playerFilterClause;

    if (filterClaimed) {
      sql += " AND pa.PlayerID NOT IN (SELECT PlayerID FROM DraftResults WHERE BackedOut = 0)\n" +
          " AND pa.PlayerID NOT IN (SELECT PlayerID FROM Keepers) ";
    }

    sql += " ) p_all ";

    return sql;
  }


  private String addFilters(UnclaimedPlayerListRequest request, String sql) {
    List<String> filters = Lists.newArrayList();

    String searchQuery = request.getSearchQuery();
    if (!Strings.isNullOrEmpty(searchQuery)) {
      String sanitizedQuery = request.getSearchQuery().replaceAll("[^\\w]", "");
      filters.add("(FirstName like '%" + sanitizedQuery +"%' or LastName like '%" + sanitizedQuery + "%') ");
    }

    if (request.getHideInjuries()) {
      filters.add("Injury IS NULL");
    }

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

  private String getPositionFilterClause(UnclaimedPlayerListRequest request, int team) {
    Position positionFilter = request.getPositionFilter();

    if (positionFilter == null) {
      return null;
    } else if (positionFilter == UNF) {
      Set<Position> openPositions = getOpenPositions(team);
      return createFilterStringFromPositions(openPositions);
    } else {
      return " = '" + positionFilter.getShortName() + "' ";
    }
  }

  private Set<Position> getOpenPositions(final int team) {
    ArrayList<DraftPick> picks = Lists.newArrayList(draftStatus.getPicks());
    return RosterUtil.getOpenPositions(
        Lists.newArrayList(Iterables.filter(picks,
            new Predicate<DraftPick>() {
              @Override
              public boolean apply(DraftPick input) {
                return input.getTeam() == team;
              }
            })));
  }

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
    Set<Position> allPositions = Sets.newHashSet(Position.values());
    allPositions.remove(UNF);
    allPositions.remove(RS);
    return openPositions.size() == allPositions.size()
        || (openPositions.contains(DH) && openPositions.contains(P));
  }




  // Player Rank

  @Override
  public void changePlayerRank(ChangePlayerRankRequest request) {
    int teamID = teamTokens.get(request.getTeamToken());
    long playerId = request.getPlayerId();
    int prevRank = request.getPrevRank();
    int newRank = request.getNewRank();

    logger.info("Change player rank for team " + teamID
        + " player " + playerId + " from rank " + prevRank + " to new rank " + newRank);

    shiftInBetweenRanks(teamID, prevRank, newRank);
    updatePlayerRank(teamID, newRank, playerId);
  }

  private void shiftInBetweenRanks(int teamID, int prevRank, int newRank) {
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

  private void updatePlayerRank(int teamID, int newRank, long playerID) {
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
    String sql = "select Player,Eligibility " +
        "from UnclaimedDisplayPlayersWithCatsByQuality " +
        "where PlayerID = " + queueEntry.getPlayerId();

    ResultSet resultSet = executeQuery(sql);
    try {
      resultSet.next();
      queueEntry.setPlayerName(resultSet.getString("Player"));
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
    int teamID = draftPick.getTeam();

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
    final int team = teamTokens.get(request.getTeamToken());
    TableSpec tableSpec = request.getTableSpec();

    if (tableSpec.getSortCol() == PlayerColumn.MYRANK) {
      logger.log(SEVERE, "Cannot set MyRank to MyRank column.");
      return;
    }

    logger.log(INFO, "Request from Team " + team + " to copy player ranks from " + tableSpec.getPlayerDataSet().getDisplayName()
        + ", " + tableSpec.getSortCol().getShortName() + ".");

    prepareTmpTable(team);

    logger.log(FINE, "Cleared temp table for " + team);

    String sql = "INSERT INTO tmp_rankings (TeamID, PlayerID) \n" +
        " SELECT " + team + ", PlayerID \n" +
        " FROM ";
    sql = getFromJoins(team, sql, null, false);

    List<String> filters = Lists.newArrayList();
    addTableSpecFilter(filters, tableSpec);

    if (!filters.isEmpty()) {
      sql += " where " + Joiner.on(" and ").join(filters) + " ";
    }

    sql = addOrdering(tableSpec, sql);

    Statement statement  = executeUpdate(sql);
    close(statement);

    logger.log(FINE, "Executed big insert for " + team);

    sql = "select min(rank) as lower_bound, max(rank) as upper_bound \n" +
        "from tmp_rankings \n" +
        "where teamID = " + team;
    ResultSet resultSet = executeQuery(sql);
    resultSet.next();

    logger.log(FINE, "Executed bounds query for " + team);

    int lowerBound = resultSet.getInt("lower_bound");
    int upperBound = resultSet.getInt("upper_bound");

    logger.log(FINE, "Got bounds off of result set.");

    close(resultSet);

    logger.log(FINE, "Closed bounds connections.");

    int offset = lowerBound - 1;

    sql = "update customRankings \n" +
        "set Rank = " + (upperBound - offset + 1) + "\n" +
        "where teamid = " + team;
    statement = executeUpdate(sql);
    close(statement);

    logger.log(FINE, "Executed base update for " + team);

    sql = "update customRankings cr\n" +
        "inner join tmp_rankings tr\n" +
        " on (cr.PlayerID = tr.PlayerID and cr.TeamID = tr.TeamID)\n" +
        "set cr.Rank = tr.Rank - " + offset + " \n" +
        "where tr.teamID = " + team;
    statement = executeUpdate(sql);
    close(statement);

    logger.log(FINE, "Executed big update for " + team);
  }


  private void prepareTmpTable(int teamID) throws SQLException {
    executeUpdate("delete from tmp_rankings where teamID = " + teamID);
  }


  @Override
  public GraphsData getGraphsData(int team) throws SQLException {
    String sql = "select * from teamscoringwithzeroes";

    GraphsData graphsData = beanFactory.createGraphsData().as();
    Map<PlayerColumn, Float> myValues = Maps.newHashMap();
    graphsData.setMyValues(myValues);
    Map<PlayerColumn, Float> avgValues = Maps.newHashMap();
    graphsData.setAvgValues(avgValues);

    ResultSet resultSet = executeQuery(sql);
    try {
      while (resultSet.next()) {
        int resultTeam = resultSet.getInt("TeamID");
        for (PlayerColumn graphStat : GraphsData.GRAPH_STATS) {
          float value = resultSet.getFloat(graphStat.getColumnName());
          if (resultTeam == team) {
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
}