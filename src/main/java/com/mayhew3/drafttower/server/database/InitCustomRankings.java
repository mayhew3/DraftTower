package com.mayhew3.drafttower.server.database;

import java.sql.ResultSet;

public class InitCustomRankings extends DatabaseUtility {

  public static void main(String[] args) {
    initConnection();

    logger.info("Initializing Custom Rankings.");

    String sql = "select id from teams";
    ResultSet resultSet = executeQuery(sql);

    while (hasMoreElements(resultSet)) {
      int teamID = getInt(resultSet, "id");
      verifyRankingsDontExist(teamID);
      prepareTmpTable();
      insertRankingsForTeam(teamID);
    }

  }

  private static void prepareTmpTable() {
    executeUpdate("truncate table tmp_rankings");
    executeUpdate("alter table tmp_rankings auto_increment = 1");
  }

  private static void verifyRankingsDontExist(int teamID) {
    String sql = "select count(1) as existingRanks from customrankings where teamid = ?";
    ResultSet resultSet = prepareAndExecuteStatementFetch(sql, teamID);
    hasMoreElements(resultSet);
    if (getInt(resultSet, "existingRanks") > 0) {
      throw new RuntimeException("Cannot overwrite existing rankings for team id " + teamID);
    }
  }

  private static void insertRankingsForTeam(int teamID) {
    String sql = "insert into tmp_rankings (TeamID, PlayerID)\n" +
        "select ?, PlayerID\n" +
        "from projectionsView\n" +
        "where Source = 'CBSSports'\n" +
        "order by (case when draft is null then 1 else 0 end), draft, rank";
    prepareAndExecuteStatementUpdate(sql, teamID);

    sql = "insert into customrankings (TeamID, PlayerID, Rank)\n" +
        "select TeamID, PlayerID, Rank\n" +
        "from tmp_rankings";
    executeUpdate(sql);
  }
}
