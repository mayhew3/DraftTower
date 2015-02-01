package com.mayhew3.drafttower.server.database;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PlayerMatcher extends DatabaseUtility {
  private PlayerInfo playerToMatch;
  private String sourceTable;
  private String idColumnInSourceTable;
  private String targetTable;
  private String idColumnInTargetTable;

  public PlayerMatcher(PlayerInfo playerInfo, String sourceTable, String idColumnInSourceTable, String targetTable, String idColumnInTargetTable) {
    if (!DatabaseUtility.hasConnection()) {
      throw new RuntimeException("Can't match without DB connection.");
    }
    playerToMatch = playerInfo;
    this.sourceTable = sourceTable;
    this.idColumnInSourceTable = idColumnInSourceTable;
    this.targetTable = targetTable;
    this.idColumnInTargetTable = idColumnInTargetTable;
  }

  public Integer getIDForPlayer() throws SQLException {
    ResultSet confidentMatches = getConfidentMatchesWithException();
    if (confidentMatches.next()) {
      return getInt(confidentMatches, idColumnInTargetTable);
    } else {
      ResultSet matchesOnNameOnly = getAlreadyPairedPlayersWithName();
    }
    return null;
  }

  private ResultSet getMatchesOnNameOnly() {
    return DatabaseUtility.prepareAndExecuteStatementFetch("SELECT * FROM " + targetTable + " WHERE FirstName = ? AND LastName = ? AND " + getDuplicateClause(),
        Lists.newArrayList((Object) playerToMatch.firstName, playerToMatch.lastName));
  }

  private ResultSet getAlreadyPairedPlayersWithName() {
    return DatabaseUtility.prepareAndExecuteStatementFetch("SELECT * FROM " + targetTable + " WHERE FirstName = ? AND LastName = ? " +
            "AND ID IN (SELECT " + idColumnInSourceTable + " FROM " + sourceTable + " WHERE " + idColumnInSourceTable + " IS NOT NULL)",
        Lists.newArrayList((Object) playerToMatch.firstName, playerToMatch.lastName));
  }

  private ResultSet getConfidentMatches() {
    if (playerToMatch.MLBTeam == null && playerToMatch.Position == null) {
      // not enough to go on.
      return DatabaseUtility.executeQuery("SELECT * FROM " + targetTable + " WHERE FALSE");
    }

    String sql = "SELECT * FROM " + targetTable + " WHERE FirstName = ? AND LastName = ?";
    List<Object> params = Lists.newArrayList((Object) playerToMatch.firstName, playerToMatch.lastName);

    sql = addTeamClause(sql, params);
    sql = addPositionClause(sql, params);
    sql = addDuplicateClause(sql);

    return DatabaseUtility.prepareAndExecuteStatementFetch(sql, params);
  }

  private ResultSet getConfidentMatchesWithException() throws SQLException {
    if (playerToMatch.MLBTeam == null && playerToMatch.Position == null) {
      // not enough to go on.
      return DatabaseUtility.executeQuery("SELECT * FROM " + targetTable + " WHERE FALSE");
    }

    String sql = "SELECT * FROM " + targetTable + " WHERE FirstName = ? AND LastName = ?";
    List<Object> params = Lists.newArrayList((Object) playerToMatch.firstName, playerToMatch.lastName);

    sql = addTeamClause(sql, params);
    sql = addPositionClause(sql, params);
    sql = addDuplicateClause(sql);

    return DatabaseUtility.prepareAndExecuteStatementFetchWithException(sql, params);
  }

  private String addTeamClause(String sql, List<Object> params) {
    if (playerToMatch.MLBTeam != null) {
      params.add(playerToMatch.MLBTeam);
      return sql + " AND MLBTeam = ?";
    } else {
      return sql;
    }
  }

  private String addPositionClause(String sql, List<Object> params) {
    if (playerToMatch.Position != null) {
      switch (playerToMatch.Position) {
        case "OF": {
          List<Object> positionsMatchForQuery = Lists.newArrayList((Object) "OF", "LF", "CF", "RF");
          String listOfWildcards = getListOfWildcards(positionsMatchForQuery.size());

          params.addAll(positionsMatchForQuery);
          return sql + " AND Position IN (" + listOfWildcards + ")";
        }
        case "P": {
          List<Object> positionsMatchForQuery = Lists.newArrayList((Object) "SP", "RP", "P");
          String listOfWildcards = getListOfWildcards(positionsMatchForQuery.size());

          params.addAll(positionsMatchForQuery);
          return sql + " AND Position IN (" + listOfWildcards + ")";
        }
        default:
          params.add(playerToMatch.Position);
          return sql + " AND Position = ?";
      }
    } else {
      return sql;
    }
  }

  private String getDuplicateClause() {
    return "ID NOT IN (SELECT " + idColumnInSourceTable + " FROM " + sourceTable + " WHERE " + idColumnInSourceTable + " IS NOT NULL)";
  }
  private String addDuplicateClause(String sql) {
    return sql + " AND ID NOT IN (SELECT " + idColumnInSourceTable + " FROM " + sourceTable + " WHERE " + idColumnInSourceTable + " IS NOT NULL)";
  }

  private String getListOfWildcards(int size) {
    List<String> questions = Lists.newArrayList();
    for (int i = 0; i < size; i++) {
      questions.add("?");
    }
    return Joiner.on(",").join(questions);
  }

}
