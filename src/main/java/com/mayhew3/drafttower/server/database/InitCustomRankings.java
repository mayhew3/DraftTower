package com.mayhew3.drafttower.server.database;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

public class InitCustomRankings {

  private Logger logger = Logger.getLogger(InitCustomRankings.class.getName());

  private SQLConnection connection;

  public static void main(String... args) throws URISyntaxException, SQLException, IOException {
    SQLConnection connection = new MySQLConnectionFactory().createConnection();
    InitCustomRankings initCustomRankings = new InitCustomRankings(connection);
    initCustomRankings.updateDatabase();
  }

  public InitCustomRankings(SQLConnection connection) {
    this.connection = connection;
  }

  public void updateDatabase() throws SQLException {
    logger.info("Initializing Custom Rankings.");

    connection.prepareAndExecuteStatementUpdate("truncate table customrankings");

    String sql = "select id from teams";
    ResultSet resultSet = connection.prepareAndExecuteStatementFetch(sql);

    while (resultSet.next()) {
      int teamID = resultSet.getInt("ID");
      verifyRankingsDontExist(teamID);
      prepareTmpTable();
      insertRankingsForTeam(teamID);
    }

  }

  private void prepareTmpTable() throws SQLException {
    connection.prepareAndExecuteStatementUpdate("truncate table tmp_rankings");
    connection.prepareAndExecuteStatementUpdate("alter table tmp_rankings auto_increment = 1");
  }

  private void verifyRankingsDontExist(int teamID) throws SQLException {
    String sql = "select count(1) as existingRanks from customrankings where teamid = ?";
    ResultSet resultSet = connection.prepareAndExecuteStatementFetch(sql, teamID);
    resultSet.next();
    if (resultSet.getInt("existingRanks") > 0) {
      throw new RuntimeException("Cannot overwrite existing rankings for team id " + teamID);
    }
  }

  private void insertRankingsForTeam(int teamID) throws SQLException {
    String sql = "insert into tmp_rankings (TeamID, PlayerID)\n" +
        "select ?, PlayerID \n" +
        "FROM \n" +
        "\t((SELECT pb.PlayerID, pb.FPTS\n" +
        "\tFROM projectionsBatting pb\n" +
        "    WHERE DataSource = 1)\n" +
        "    UNION\n" +
        "    (SELECT pp.PlayerID, pp.FPTS\n" +
        "    FROM projectionsPitching pp\n" +
        "    WHERE DataSource = 1)\n" +
        "    ORDER BY FPTS DESC) a";
    connection.prepareAndExecuteStatementUpdate(sql, teamID);

    sql = "insert into customrankings (TeamID, PlayerID, Rank)\n" +
        "select TeamID, PlayerID, Rank\n" +
        "from tmp_rankings";
    connection.prepareAndExecuteStatementUpdate(sql);
  }
}
