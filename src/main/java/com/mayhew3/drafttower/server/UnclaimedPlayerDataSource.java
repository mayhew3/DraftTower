package com.mayhew3.drafttower.server;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayhew3.drafttower.shared.*;

import javax.servlet.ServletException;
import java.sql.*;
import java.util.List;

import static com.mayhew3.drafttower.shared.Position.P;
import static com.mayhew3.drafttower.shared.Position.RP;

/**
 * Looks up unclaimed players in the database.
 */
@Singleton
public class UnclaimedPlayerDataSource {
  Connection _connection;

  private final BeanFactory beanFactory;

  @Inject
  public UnclaimedPlayerDataSource(BeanFactory beanFactory) throws ServletException {
    initConnection();

    this.beanFactory = beanFactory;
  }

  public UnclaimedPlayerListResponse lookup(UnclaimedPlayerListRequest request) throws ServletException {
    UnclaimedPlayerListResponse response = beanFactory.createUnclaimedPlayerListResponse().as();

    List<Player> players = Lists.newArrayList();

    ResultSet resultSet = getResultSetForRows(request.getRowCount(), request.getRowStart());
    try {
      while (resultSet.next()) {
        Player player = beanFactory.createPlayer().as();
        player.setPlayerId(resultSet.getInt("PlayerID"));
        player.setColumnValues(ImmutableMap.<PlayerColumn, String>builder()
            .put(PlayerColumn.NAME, resultSet.getString("LastName") + ", " + resultSet.getString("FirstName"))
            .put(PlayerColumn.POS, resultSet.getString("Position"))
            .put(PlayerColumn.ELIG, resultSet.getString("Eligibility"))
            .put(PlayerColumn.INN, resultSet.getString("INN"))
            .put(PlayerColumn.K, resultSet.getString("K"))
            .put(PlayerColumn.ERA, resultSet.getString("ERA"))
            .put(PlayerColumn.WHIP, resultSet.getString("WHIP"))
            .put(PlayerColumn.WL, resultSet.getString("WL"))
            .put(PlayerColumn.S, resultSet.getString("S"))
            .put(PlayerColumn.RANK, resultSet.getString("Rank"))
            .put(PlayerColumn.RATING, resultSet.getString("Total"))
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


  private void initConnection() throws ServletException {
    try {
      Class.forName("com.mysql.jdbc.Driver");
      _connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/uncharted", "root", "m3mysql");
    } catch (ClassNotFoundException e) {
      throw new ServletException("JDBC Driver not found.", e);
    } catch (SQLException e) {
      throw new ServletException("Could not connect to database.", e);
    }
  }

  private int getTotalPlayerCount() throws ServletException {
    String sql = "select count(1) as TotalPlayers " +
        "from UnclaimedDisplayPlayersWithCatsByQuality " +
        "where Year = 2012 " +
        "and Eligibility = 'P'";

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
        "and Eligibility = 'P' " +
        "order by total desc " +
        "limit " + rowStart + ", " + rowCount;

    return executeQuery(sql);
  }

  private ResultSet executeQuery(String sql) throws ServletException {
    try {
      Statement statement = _connection.createStatement();

      return statement.executeQuery(sql);

    } catch (SQLException e) {
      throw new ServletException("Error executing SQL query.", e);
    }
  }


}