package com.mayhew3.drafttower.server;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayhew3.drafttower.server.ServerModule.TeamTokens;
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

import static com.mayhew3.drafttower.shared.Position.UNF;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

/**
 * Looks up players in the database.
 */
@Singleton
public class PlayerDataSource {

  private static final Logger logger = Logger.getLogger(PlayerDataSource.class.getName());

  private final DataSource db;
  private final BeanFactory beanFactory;
  private final DraftStatus draftStatus;
  private final Map<String, Integer> teamTokens;
  private int numTeams;

  @Inject
  public PlayerDataSource(DataSource db,
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

  private int getTotalUnclaimedPlayerCount(UnclaimedPlayerListRequest request, final int team) throws ServletException {
    TableSpec tableSpec = request.getTableSpec();
    PlayerDataSet playerDataSet = tableSpec.getPlayerDataSet();

    String sql = "select count(1) as TotalPlayers " +
        "from " + playerDataSet.getTableName() + " ";

    sql = addFilters(request, team, sql);

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
    PlayerDataSet playerDataSet = tableSpec.getPlayerDataSet();

    String sql = "select * from " + playerDataSet.getTableName() + " ";

    sql = addFilters(request, team, sql);

    PlayerColumn sortCol = tableSpec.getSortCol();
    if (sortCol != null) {
      sql += "order by case when " + sortCol.getColumnName() + " is null then 1 else 0 end, "
          + sortCol.getColumnName() + " " + (tableSpec.isAscending() ? "asc " : "desc ");
    } else {
      sql += "order by " + playerDataSet.getStartingSort() + " ";
    }
    sql += "limit " + request.getRowStart() + ", " + request.getRowCount();

    return executeQuery(sql);
  }

  private String addFilters(UnclaimedPlayerListRequest request, final int team, String sql) {
    Position positionFilter = request.getPositionFilter();
    List<String> filters = Lists.newArrayList();
    if (positionFilter != null) {
      if (positionFilter == UNF) {
        ArrayList<DraftPick> picks = Lists.newArrayList(draftStatus.getPicks());
        Set<Position> openPositions = RosterUtil.getOpenPositions(
            Lists.newArrayList(Iterables.filter(picks,
                new Predicate<DraftPick>() {
                  @Override
                  public boolean apply(DraftPick input) {
                    return input.getTeam() == team;
                  }
                })));
        String filter = "Position in (";
        filter += Joiner.on(',').join(Iterables.transform(openPositions, new Function<Position, String>() {
          @Override
          public String apply(Position input) {
            return "'" + input.getShortName() + "'";
          }
        }));
        filter += ") ";
        filters.add(filter);
      } else {
        filters.add("Position = '" + positionFilter.getShortName() + "' ");
      }
    }

    String searchQuery = request.getSearchQuery();
    if (!Strings.isNullOrEmpty(searchQuery)) {
      String sanitizedQuery = request.getSearchQuery().replaceAll("[^\\w]", "");
      filters.add("(FirstName like '%" + sanitizedQuery +"%' or LastName like '%" + sanitizedQuery + "%') ");
    }

    if (request.getHideInjuries()) {
      filters.add("Injury IS NULL");
    }

    addTableSpecFilters(team, filters, request.getTableSpec());

    if (filters.isEmpty()) {
      return sql;
    } else {
      return sql + "where " + Joiner.on(" and ").join(filters) + " ";
    }
  }

  private void addTableSpecFilters(int team, List<String> filters, TableSpec tableSpec) {
    PlayerDataSet playerDataSet = tableSpec.getPlayerDataSet();

    String sourceFilter = playerDataSet.getSourceFilter();
    if (sourceFilter != null) {
      filters.add("Source = '" + sourceFilter + "' ");
      filters.add("Drafted = 0");
      filters.add("Keeper = 0");
    }


    if (playerDataSet.equals(PlayerDataSet.CUSTOM)) {
      filters.add("Drafted = 0");
      filters.add("Keeper = 0");
      filters.add("TeamID = " + team);
    }
  }

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

  public long getBestPlayerId(TableSpec tableSpec, final Integer team, Set<Position> openPositions) throws SQLException {
    PlayerDataSet playerDataSet = tableSpec.getPlayerDataSet();

    String sql = "select PlayerID, Eligibility from " + playerDataSet.getTableName() + " ";

    List<String> filters = Lists.newArrayList();
    addTableSpecFilters(team, filters, tableSpec);

    if (!filters.isEmpty()) {
      sql += " where " + Joiner.on(" and ").join(filters);
    }

    ResultSet resultSet = null;
    try {
      resultSet = executeQuery(sql);
      Long firstReserve = null;
      while (resultSet.next()) {
        if (firstReserve == null) {
          firstReserve = resultSet.getLong("PlayerID");
        }
        List<String> eligibility = splitEligibilities(resultSet.getString("Eligibility"));
        for (String position : eligibility) {
          if (openPositions.contains(Position.fromShortName(position))) {
            return resultSet.getLong("PlayerID");
          }
        }
      }
      return firstReserve;
    } finally {
      close(resultSet);
    }
  }

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

  public void backOutLastDraftPick(int pickToRemove) throws SQLException {
    String sql = "UPDATE DraftResults SET BackedOut = 1 WHERE OverallPick = " + pickToRemove;

    Statement statement = null;
    try {
      statement = executeUpdate(sql);
    } finally {
      close(statement);
    }
  }

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