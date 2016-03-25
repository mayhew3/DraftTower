package com.mayhew3.drafttower.server.database;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.deploy.util.StringUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static com.mayhew3.drafttower.server.database.InputGetter.grabInput;

public class AddSourceData extends DatabaseUtility {

  private static boolean promptForMatches = false;

  public static void main(String[] args) {
    initConnection();

    debug("Adding source data.");

    DataSourceProfile guru_batters = new DataSourceProfile("guru_batters_id", "guru_batters", "nameFirst", "nameLast", "teamID", "pos", "Rank", "ASC");
    DataSourceProfile guru_pitchers = new DataSourceProfile("guru_pitchers_id", "guru_pitchers", "nameFirst", "nameLast", "teamID", "pos", "mGURU", "DESC");
    DataSourceProfile cbs_ids = new DataSourceProfile("id", "cbsids", "FirstName", "LastName", "MLBTeam", "Position", "ID", "DESC");
    DataSourceProfile cbs_draftaverages = new DataSourceProfile("id", "cbs_draftaverages", "FirstName", "LastName", "MLBTeam", "Position", "ID", "ASC");
    DataSourceProfile tmp_cbsbatting = new DataSourceProfile("id", "tmp_cbsbatting", "FirstName", "LastName", "MLBTeam", "Position", "ID", "ASC");
    DataSourceProfile tmp_cbspitching = new DataSourceProfile("id", "tmp_cbspitching", "FirstName", "LastName", "MLBTeam", "Position", "ID", "ASC");
    DataSourceProfile rotoBatting = new DataSourceProfile("id", "tmp_rotowirebatting", "First_Name", "Last_Name", "Team", "Pos", "AB", "DESC");
    DataSourceProfile rotoPitching = new DataSourceProfile("id", "tmp_rotowirepitching", "First_Name", "Last_Name", "Team", "Pos", "IP", "DESC");

    guru_batters.populatePlayerIDs();
  }


  private static void debug(String str) {
    System.out.println(str);
  }


  private static class DataSourceProfile {
    public String tableName;

    public String idCol;

    public String firstNameCol;
    public String lastNameCol;
    public String mlbTeamCol;
    public String positionCol;

    public String rankCol;
    public String rankOrder;

    private DataSourceProfile(String idCol, String tableName, String firstNameCol, String lastNameCol, String mlbTeamCol,
                              String positionCol, String rankCol, String rankOrder) {
      this.idCol = idCol;
      this.tableName = tableName;
      this.firstNameCol = firstNameCol;
      this.lastNameCol = lastNameCol;
      this.mlbTeamCol = mlbTeamCol;
      this.positionCol = positionCol;
      this.rankCol = rankCol;
      this.rankOrder = rankOrder;
    }

    public void populatePlayerIDs() {
      createPlayerIDIfDoesntExist();
      fixTeamNames();
      verifyRankColumn();

      int playerID;

      int found = 0;
      int notFound = 0;

      List<FailedPlayer> failedPlayers = Lists.newArrayList();

      PreparedStatement updateStatement = getPreparedStatement("UPDATE " + tableName + " SET PlayerID = ? WHERE " + idCol + " = ?");

      String conditionClause = "FROM players WHERE FirstName = ? AND LastName = ? AND MLBTeam = ? AND Position = ?";
      PreparedStatement matchingCountStatement = getPreparedStatement("SELECT COUNT(*) AS numMatching " + conditionClause);

      PreparedStatement allPlayersStatement = getPreparedStatement("SELECT * FROM " + tableName + " WHERE PlayerID IS NULL ORDER BY ? " + rankOrder);
      ResultSet allPlayers = executePreparedStatementWithParams(allPlayersStatement, rankCol);

      while (hasMoreElements(allPlayers)) {
        try {
          int id = getInt(allPlayers, idCol);

          SourcePlayer sourcePlayer = new SourcePlayer(
              getString(allPlayers, firstNameCol),
              getString(allPlayers, lastNameCol),
              getString(allPlayers, mlbTeamCol),
              getString(allPlayers, positionCol),
              tableName,
              getString(allPlayers, "PlayerString"));


          ResultSet matchingCount = executePreparedStatementWithParams(matchingCountStatement, sourcePlayer.getPartsAsList());

          hasMoreElements(matchingCount);

          int numMatching = getInt(matchingCount, "numMatching");
          if (numMatching > 1) {
            throw new FailedPlayer(id, "Found multiple players matching: " + sourcePlayer.getPrintablePlayer());
          } else if (numMatching == 1) {
            PreparedStatement playerDetailStmt = getPreparedStatement("SELECT ID, PlayerString " + conditionClause);
            ResultSet matchingPlayers = executePreparedStatementWithParams(playerDetailStmt, sourcePlayer.getPartsAsList());

            hasMoreElements(matchingPlayers);
            playerID = getInt(matchingPlayers, "ID");
            debug("Found exact match between " + sourcePlayer.getPrintablePlayer() + " and '" + getString(matchingPlayers, "PlayerString") + "'");
            found++;
          } else {
            debug("Couldn't find exact match for: " + sourcePlayer.getPrintablePlayer());
            playerID = findPossibleAlternatives(sourcePlayer);
            if (playerID > 0) {
              found++;
            } else {
              debug("Couldn't find alternative match for player.");
              notFound++;
            }
          }

          if (playerID > 0) {
            executePreparedUpdateWithParamsWithoutClose(updateStatement, playerID, id);
          }
        } catch (FailedPlayer fp) {
          failedPlayers.add(fp);
        }

      }


      try {
        updateStatement.close();
      } catch (SQLException e) {
        throw new RuntimeException("Error closing update statement.");
      }

      debug("Found matches for " + found + " players, but not for the remaining " + notFound + ".");
    }

    private void verifyRankColumn() {
      ResultSet resultSet = executeQuery("SELECT COUNT(1) AS NullRank FROM " + tableName + " WHERE " + rankCol + " IS NULL");
      hasMoreElements(resultSet);
      if (getInt(resultSet, "NullRank") > 0) {
        throw new RuntimeException("Must be able to reliably sort of rank column " + rankCol + ", but null values found.");
      }
    }

    private int findConfidentMatch(SourcePlayer sourcePlayer) {
      ResultSet confidentMatches = sourcePlayer.getConfidentMatches();
      // returned null if don't have enough info for a confident match.
      if (confidentMatches != null) {
//        debug("Looking for other confident matches...");
        int foundPlayer = getUserChoiceIfMultiple(confidentMatches);
        if (foundPlayer > 0) {
          return foundPlayer;
        } else {
          debug("No confident match found.");
        }
      }
      return -1;
    }

    private int findSmartMapMatch(SourcePlayer sourcePlayer) throws FailedPlayer {
      String sql = "SELECT p.ID AS PlayerID " +
          " FROM playeridmap pim " +
          " INNER JOIN players p " +
          "   ON p.CBS_ID = pim.CBSID " +
          " WHERE pim.FIRSTNAME = ? AND pim.LASTNAME = ? AND pim.TEAM = ? ";
      List<Object> params = Lists.newArrayList((Object)sourcePlayer.firstName, sourcePlayer.lastName, sourcePlayer.mlbTeam);
      sql = sourcePlayer.addPositionClause(sql, params, "pim.POS");

      ResultSet resultSet = prepareAndExecuteStatementFetch(sql, params);
      int playerID = -1;
      if (hasMoreElements(resultSet)) {
        playerID = getInt(resultSet, "PlayerID");
        debug("Found match through smart map, ID: " + playerID);
      }
      if (hasMoreElements(resultSet)) {
        throw new FailedPlayer(playerID, "Found multiple matches for player " + sourcePlayer.getPrintablePlayer());
      }

      return playerID;
    }

    private int findNameOnlyMatch(SourcePlayer sourcePlayer) {
      if (promptForMatches) {
        ResultSet matchesOnNameOnly = sourcePlayer.getMatchesOnNameOnly();
        debug("Looking for players matching on Name only...");
        ResultSet alreadyPaired = sourcePlayer.getAlreadyPairedPlayersWithName();
        int foundPlayer = getUserChoice(matchesOnNameOnly, alreadyPaired);
        if (foundPlayer > 0) {
          return foundPlayer;
        } else {
          debug("No matches found.");
        }
      }
      return -1;
    }

    private int findGusScrapeMatch(SourcePlayer sourcePlayer) throws FailedPlayer {
      String sql = "SELECT p.ID AS PlayerID " +
          "FROM players p " +
          "INNER JOIN cbsids_2013 cbs " +
          "  ON p.CBS_ID = cbs.CBS_ID " +
          "WHERE cbs.PlayerString = ?";

      String fakePlayerString = sourcePlayer.lastName + ", " + sourcePlayer.firstName + " " +
                                sourcePlayer.position + " " + sourcePlayer.mlbTeam;

      ResultSet resultSet = prepareAndExecuteStatementFetch(sql, fakePlayerString);

      int playerID = -1;
      if (hasMoreElements(resultSet)) {
        playerID = getInt(resultSet, "PlayerID");
      }
      if (hasMoreElements(resultSet)) {
        throw new FailedPlayer(playerID, "Found multiple matches for player " + sourcePlayer.getPrintablePlayer());
      }

      return playerID;
    }

    private int findPossibleAlternatives(SourcePlayer sourcePlayer) throws FailedPlayer {

      int confidentMatch = findConfidentMatch(sourcePlayer);
      if (confidentMatch > -1) {
        return confidentMatch;
      }

      int smartMapMatch = findSmartMapMatch(sourcePlayer);
      if (smartMapMatch > -1) {
        return smartMapMatch;
      }

      int gusScrapeMatch = findGusScrapeMatch(sourcePlayer);
      if (gusScrapeMatch > -1) {
        return gusScrapeMatch;
      }

      return findNameOnlyMatch(sourcePlayer);
    }

    private int getUserChoice(ResultSet resultSet, ResultSet alreadyPaired) {
      int i = 1;
      Map<Integer, Integer> possiblePlayersMap = Maps.newHashMap();
      Map<Integer, String> possiblePlayerStrings = Maps.newHashMap();
      while (hasMoreElements(resultSet)) {
        String playerString = getString(resultSet, "PlayerString");
        debug(i + ": " + playerString);
        possiblePlayerStrings.put(i, playerString);
        possiblePlayersMap.put(i, getInt(resultSet, "ID"));
        i++;
      }
      if (i > 1) {
        boolean hasDupe = false;
        while (hasMoreElements(alreadyPaired)) {
          debug(" - Player with same name already paired: " + getString(alreadyPaired, "PlayerString"));
          hasDupe = true;
        }
        if (i == 2 && !hasDupe) {
          debug("Found single match: " + possiblePlayerStrings.get(1));
          return possiblePlayersMap.get(1);
        }
        int chosen = new Integer(grabInput("Use any of these? (0 for no): "));
        if (chosen < 0 || chosen > i-1) {
          throw new IllegalArgumentException("Number must be between 0 and " + (i-1));
        } else if (chosen > 0) {
          return possiblePlayersMap.get(chosen);
        }
      }
      return -1;
    }

    private int getUserChoiceIfMultiple(ResultSet resultSet) {
      int i = 1;
      Map<Integer, Integer> possiblePlayersMap = Maps.newHashMap();
      Map<Integer, String> possiblePlayerStrings = Maps.newHashMap();
      while (hasMoreElements(resultSet)) {
        possiblePlayerStrings.put(i, getString(resultSet, "PlayerString"));
        possiblePlayersMap.put(i, getInt(resultSet, "ID"));
        i++;
      }
      if (i > 2) {
        if (promptForMatches) {
          int chosen = new Integer(grabInput("Use any of these? (0 for no)"));
          if (chosen < 0 || chosen > i-1) {
            throw new IllegalArgumentException("Number must be between 0 and " + (i-1));
          } else if (chosen > 0) {
            return possiblePlayersMap.get(chosen);
          }
        } else {
          return -1;
        }
      } else if (i > 1) {
        debug("Found single match: " + possiblePlayerStrings.get(1));
        return possiblePlayersMap.get(1);
      }
      return -1;
    }

    public void fixTeamNames() {
      List<String> systemTeamNames = Lists.newArrayList();

      ResultSet systemTeamResults = executeQuery("SELECT MLBTeam FROM players GROUP BY MLBTeam ORDER BY MLBTeam");
      while (hasMoreElements(systemTeamResults)) {
        systemTeamNames.add(getString(systemTeamResults, "MLBTeam"));
      }

      if (systemTeamNames.size() != 30) {
        throw new RuntimeException("Expected 30 team names in the system.");
      }

      ResultSet sourceTeamResults = executeQuery("SELECT " + mlbTeamCol + " FROM " + tableName +
                                                " WHERE " + mlbTeamCol + " IS NOT NULL GROUP BY " + mlbTeamCol);
      while (hasMoreElements(sourceTeamResults)) {
        String teamName = getString(sourceTeamResults, mlbTeamCol);
        if (!systemTeamNames.contains(teamName)) {
          String trimmed = StringUtils.trimWhitespace(teamName);
          if (!systemTeamNames.contains(trimmed)) {
            String replacementName = grabInput("Team not found: " + teamName + "... Replace with (N/A to skip)? ");
            if (!"N/A".equals(replacementName)) {
              if (!systemTeamNames.contains(replacementName)) {
                throw new RuntimeException("Input team name not found either. Exiting.");
              } else {
                executeUpdate("UPDATE " + tableName + " SET " + mlbTeamCol + " = '" + replacementName + "' WHERE " + mlbTeamCol + " = '" + teamName + "'");
              }
            }
          } else {
            executeUpdate("UPDATE " + tableName + " SET " + mlbTeamCol + " = '" + trimmed + "' WHERE " + mlbTeamCol + " = '" + teamName + "'");
          }
        }
      }
    }

    public void createPlayerIDIfDoesntExist() {
      if (!columnExists(tableName, "PlayerID")) {
        executeUpdate("ALTER TABLE " + tableName + " ADD COLUMN PlayerID INT(11) NULL");
      }
    }

    private static class SourcePlayer {
      public String firstName;
      public String lastName;
      public String mlbTeam;
      public String position;

      public String playerString;

      private String tableName;

      private SourcePlayer(String firstName, String lastName, String mlbTeam, String position, String tableName, String playerString) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.mlbTeam = mlbTeam;
        this.position = position;
        this.tableName = tableName;
        this.playerString = playerString;
      }

      private String getPrintablePlayer() {
        return Joiner.on(", ").join(firstName, lastName, getTeamAsString(), getPositionAsString());
      }

      private String getPositionAsString() {
        return position == null ? "" : position;
      }

      private String getTeamAsString() {
        return mlbTeam == null ? "" : mlbTeam;
      }

      private List<Object> getPartsAsList() {
        return Lists.newArrayList((Object) firstName, lastName, getTeamAsString(), getPositionAsString());
      }

      private ResultSet getMatchesOnNameOnly() {
        return prepareAndExecuteStatementFetch("SELECT * FROM players WHERE FirstName = ? AND LastName = ? AND " + getDuplicateClause(),
            Lists.newArrayList((Object) firstName, lastName));
      }

      private ResultSet getAlreadyPairedPlayersWithName() {
        return prepareAndExecuteStatementFetch("SELECT * FROM players WHERE FirstName = ? AND LastName = ? AND ID IN (SELECT PlayerID FROM " + tableName + " WHERE PlayerID IS NOT NULL)",
            Lists.newArrayList((Object) firstName, lastName));
      }



      private ResultSet getConfidentMatches() {
        if (mlbTeam == null && position == null) {
          // not enough to go on.
          return null;
        }

        String sql = "SELECT * FROM players WHERE FirstName = ? AND LastName = ?";
        PreparedStatement preparedStatement = addClausesToNameOnlyQuery(sql);

        return executePreparedStatementAlreadyHavingParameters(preparedStatement);
      }


      private PreparedStatement addClausesToNameOnlyQuery(String sql) {
        List<Object> params = Lists.newArrayList((Object) firstName, lastName);

        sql = addTeamClause(sql, params, "MLBTeam");
        sql = addPositionClause(sql, params, "Position");
        sql = addDuplicateClause(sql);

        return prepareStatement(sql, params);
      }

      private String addTeamClause(String sql, List<Object> params, final String mlbColumnName) {
        if (mlbTeam != null) {
          params.add(mlbTeam);
          return sql + " AND " + mlbColumnName + " = ?";
        } else {
          return sql;
        }
      }

      protected String addPositionClause(String sql, List<Object> params, final String posColumnName) {
        if (position != null) {
          List<Object> outFieldPositions = Lists.newArrayList((Object) "OF", "LF", "CF", "RF");
          List<Object> pitchingPositions = Lists.newArrayList((Object) "SP", "RP", "P");

          if (outFieldPositions.contains(position)) {
            String listOfWildcards = getListOfWildcards(outFieldPositions.size());

            params.addAll(outFieldPositions);
            return sql + " AND " + posColumnName + " IN (" + listOfWildcards + ")";
          } else if (pitchingPositions.contains(position)) {
            String listOfWildcards = getListOfWildcards(pitchingPositions.size());

            params.addAll(pitchingPositions);
            return sql + " AND " + posColumnName + " IN (" + listOfWildcards + ")";
          } else {
            params.add(position);
            return sql + " AND " + posColumnName + " = ?";
          }
        } else {
          return sql;
        }
      }

      private String getDuplicateClause() {
        return "ID NOT IN (SELECT PlayerID FROM " + tableName + " WHERE PlayerID IS NOT NULL)";
      }
      private String addDuplicateClause(String sql) {
        return sql + " AND ID NOT IN (SELECT PlayerID FROM " + tableName + " WHERE PlayerID IS NOT NULL)";
      }

      private String getListOfWildcards(int size) {
        List<String> questions = Lists.newArrayList();
        for (int i = 0; i < size; i++) {
          questions.add("?");
        }
        return Joiner.on(",").join(questions);
      }

    }
  }
}
