package com.mayhew3.drafttower.server;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayhew3.drafttower.server.ServerModule.TeamTokens;
import com.mayhew3.drafttower.shared.*;

import javax.servlet.ServletException;
import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

/**
 * Looks up unclaimed players in the database.
 */
@Singleton
public class UnclaimedPlayerDataSource {

  private final DataSource db;
  private final BeanFactory beanFactory;
  private final Map<String, Integer> teamTokens;

  @Inject
  public UnclaimedPlayerDataSource(DataSource db,
      BeanFactory beanFactory,
      @TeamTokens Map<String, Integer> teamTokens) {
    this.db = db;
    this.beanFactory = beanFactory;
    this.teamTokens = teamTokens;
  }

  public UnclaimedPlayerListResponse lookup(UnclaimedPlayerListRequest request) throws ServletException {
    UnclaimedPlayerListResponse response = beanFactory.createUnclaimedPlayerListResponse().as();

    Integer team = teamTokens.get(request.getTeamToken());

    List<Player> players = Lists.newArrayList();

    ResultSet resultSet = getResultSetForRows(request.getRowCount(), request.getRowStart());
    try {
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

        player.setColumnValues(columnMap
            .build());

        players.add(player);
      }
    } catch (SQLException e) {
      throw new ServletException("Error getting next element of results.", e);
    }

    response.setPlayers(players);
    response.setTotalPlayers(getTotalPlayerCount());

    return response;
  }

  private int getTotalPlayerCount() throws ServletException {
    String sql = "select count(1) as TotalPlayers " +
        "from UnclaimedDisplayPlayersWithCatsByQuality " +
        "where Year = 2012";

    ResultSet resultSet = executeQuery(sql);

    try {
      resultSet.next();
      return resultSet.getInt("TotalPlayers");
    } catch (SQLException e) {
      throw new ServletException("Couldn't find number of rows in table.", e);
    }
  }

  private ResultSet getResultSetForRows(int rowCount, int rowStart) throws ServletException {

    String sql = "select * " +
        "from UnclaimedDisplayPlayersWithCatsByQuality " +
        "where Year = 2012 " +
        "order by total desc " +
        "limit " + rowStart + ", " + rowCount;

    return executeQuery(sql);
  }

  private ResultSet executeQuery(String sql) throws ServletException {
    try {
      Statement statement = db.getConnection().createStatement();

      return statement.executeQuery(sql);

    } catch (SQLException e) {
      throw new ServletException("Error executing SQL query.", e);
    }
  }


}