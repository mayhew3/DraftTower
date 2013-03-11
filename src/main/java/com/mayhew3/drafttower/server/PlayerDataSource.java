package com.mayhew3.drafttower.server;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayhew3.drafttower.server.ServerModule.TeamTokens;
import com.mayhew3.drafttower.shared.*;
import com.mayhew3.drafttower.shared.SharedModule.NumTeams;

import javax.servlet.ServletException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Looks up players in the database.
 */
@Singleton
public class PlayerDataSource {

  private static final Logger logger = Logger.getLogger(PlayerDataSource.class.getName());

  private final DataSource db;
  private final BeanFactory beanFactory;
  private final Map<String, Integer> teamTokens;
  private int numTeams;

  @Inject
  public PlayerDataSource(DataSource db,
      BeanFactory beanFactory,
      @TeamTokens Map<String, Integer> teamTokens,
      @NumTeams int numTeams) {
    this.db = db;
    this.beanFactory = beanFactory;
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
      resultSet = getResultSetForUnclaimedPlayerRows(request.getRowCount(), request.getRowStart());
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

        // TODO(m3)
        if (!request.getHideInjuries() && player.getPlayerId() % 5 == 0) {
          player.setInjury("busted wang");
        }

        players.add(player);
      }
    } catch (SQLException e) {
      throw new ServletException("Error getting next element of results.", e);
    } finally {
      try {
        close(resultSet);
      } catch (SQLException e) {
        throw new ServletException("Error closing DB resources.", e);
      }
    }

    response.setPlayers(players);
    response.setTotalPlayers(getTotalUnclaimedPlayerCount());

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
      try {
        close(resultSet);
      } catch (SQLException e) {
        throw new ServletException("Error closing DB resources.", e);
      }
    }
    return keepers;
  }

  private int getTotalUnclaimedPlayerCount() throws ServletException {
    String sql = "select count(1) as TotalPlayers " +
        "from UnclaimedDisplayPlayersWithCatsByQuality";

    ResultSet resultSet = null;
    try {
      resultSet = executeQuery(sql);
      resultSet.next();
      return resultSet.getInt("TotalPlayers");
    } catch (SQLException e) {
      throw new ServletException("Couldn't find number of rows in table.", e);
    } finally {
      try {
        close(resultSet);
      } catch (SQLException e) {
        throw new ServletException("Error closing DB resources.", e);
      }
    }
  }

  private ResultSet getResultSetForUnclaimedPlayerRows(int rowCount, int rowStart)
      throws SQLException {

    String sql = "select * " +
        "from UnclaimedDisplayPlayersWithCatsByQuality " +
        "order by total desc " +
        "limit " + rowStart + ", " + rowCount;

    return executeQuery(sql);
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

  public long getBestPlayerId(TableSpec tableSpec) throws SQLException {
    // TODO(m3): use tableSpec.
    String sql = "select PlayerID " +
        "from UnclaimedDisplayPlayersWithCatsByQuality";

    ResultSet resultSet = null;
    try {
      resultSet = executeQuery(sql);
      resultSet.next();
      return resultSet.getLong("PlayerID");
    } finally {
      close(resultSet);
    }
  }

  public void changePlayerRank(ChangePlayerRankRequest request) {
    // TODO(m3)
    logger.info("Change player rank for team " + teamTokens.get(request.getTeamToken())
        + " player " + request.getPlayerId() + " new rank " + request.getNewRank());
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
          ", " + draftPosition + ", 0)";

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
        status.getPicks().add(pick);
      }
    } finally {
      close(resultSet);
    }
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

  private void close(Statement statement) throws SQLException {
    Connection connection = statement.getConnection();
    statement.close();
    connection.close();
  }
}