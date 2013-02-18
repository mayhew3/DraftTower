package com.mayhew3.drafttower.server;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayhew3.drafttower.server.ServerModule.TeamTokens;
import com.mayhew3.drafttower.shared.*;

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

  @Inject
  public PlayerDataSource(DataSource db,
      BeanFactory beanFactory,
      @TeamTokens Map<String, Integer> teamTokens) {
    this.db = db;
    this.beanFactory = beanFactory;
    this.teamTokens = teamTokens;
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
          Lists.newArrayList(resultSet.getString("Eligibility").split(",")));
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
          Lists.newArrayList(resultSet.getString("Eligibility").split(",")));
    } finally {
      close(resultSet);
    }
  }

  public long getBestPlayerId() throws SQLException {
    String sql = "select ID " +
        "from AllPlayers " +
        "where FirstName = 'Joakim' and LastName = 'Soria'";

    ResultSet resultSet = null;
    try {
      resultSet = executeQuery(sql);
      resultSet.next();
      return resultSet.getLong("ID");
    } finally {
      close(resultSet);
    }
  }

  public void changePlayerRank(ChangePlayerRankRequest request) {
    // TODO(m3)
    logger.info("Change player rank for team " + teamTokens.get(request.getTeamToken())
        + " player " + request.getPlayerId() + " new rank " + request.getNewRank());
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