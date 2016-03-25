package com.mayhew3.drafttower.server.database;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class PopulatePlayerTable extends DatabaseUtility {

  public static void main(String[] args) {
    initConnection();

    logger.log(Level.INFO, "Updating Player table with from players CBS batting and pitching, examining every row for a match.");

    List<String> tableNames = Lists.newArrayList("tmp_cbsbatting", "tmp_cbspitching");
    List<FailedPlayer> failedPlayers = Lists.newArrayList();

    PreparedStatement exactMatchSQL = getPreparedStatement("SELECT COUNT(1) AS NumMatches FROM players " +
                "WHERE FirstName = ? AND LastName = ? AND MLBTeam = ? AND Position = ? AND PlayerString = ?");

    for (String tableName : tableNames) {

      String sql = "SELECT * FROM " + tableName + " WHERE MLBTeam IS NOT NULL AND Position IS NOT NULL";

      ResultSet resultSet = executeQuery(sql);

      while (hasMoreElements(resultSet)) {
        try {
          PlayerInfo playerInfo = new PlayerInfo();

          int db_id = getInt(resultSet, "ID");
          playerInfo.firstName = getString(resultSet, "FirstName");
          playerInfo.lastName = getString(resultSet, "LastName");
          playerInfo.MLBTeam = getString(resultSet, "MLBTeam");
          playerInfo.Position = getString(resultSet, "Position");
          String playerString = getString(resultSet, "PlayerString");

          logger.log(Level.INFO, "Running on player :" + playerInfo);


          ResultSet exactMatches = executePreparedStatementWithParams(exactMatchSQL,
              playerInfo.firstName, playerInfo.lastName, playerInfo.MLBTeam, playerInfo.Position, playerString);
          hasMoreElements(exactMatches);

          // if exact match found, no update required
          if (getInt(exactMatches, "NumMatches") < 1) {
            String nameMatchSQL = "SELECT COUNT(1) AS NumMatches FROM players WHERE FirstName = ? AND LastName = ?";
            ResultSet nameMatches = prepareAndExecuteStatementFetch(nameMatchSQL, playerInfo.firstName, playerInfo.lastName);
            hasMoreElements(nameMatches);

            int numNameMatches = getInt(nameMatches, "NumMatches");
            if (numNameMatches == 1) {
              updatePlayerFields(playerInfo, playerString);
            } else if (numNameMatches < 1) {
              if (userSaysYes(playerInfo)) {
                addPlayer(playerInfo, playerString);
              } else {
                throw new FailedPlayer(db_id, "User said this player already exists: " + playerInfo);
              }
            } else {
              throw new FailedPlayer(db_id, "Multiple matches found for Player: " + playerInfo);
            }
          } else {
            logger.log(Level.INFO, "Found exact match, no need for update, for player: " + playerInfo);
          }


        } catch (FailedPlayer fp) {
          failedPlayers.add(fp);
        }
      }
    }


    if (!failedPlayers.isEmpty()) {
      System.out.println("String parse failed on " + failedPlayers.size() + " rows from players table:");
      for (FailedPlayer failedPlayer : failedPlayers) {
        System.out.println("ID " + failedPlayer.id + ": " + failedPlayer.message);
      }
    }
  }

  private static boolean userSaysYes(PlayerInfo playerInfo) {
    System.out.println("Existing Players with same Last Name as " + playerInfo + ":");

    String sql = "SELECT PlayerString FROM players WHERE LastName = ?";
    ResultSet resultSet = prepareAndExecuteStatementFetch(sql, playerInfo.lastName);

    boolean hasElements = false;
    while (hasMoreElements(resultSet)) {
      hasElements = true;
      System.out.println(" - " + getString(resultSet, "PlayerString"));
    }

    if (!hasElements) {
      return true;
    }

    String s = InputGetter.grabInput("Should really add new player? (0 for no, 1 for yes) ");
    return "1".equals(s);
  }

  private static void addPlayer(PlayerInfo playerInfo, String playerString) throws FailedPlayer {
    logger.log(Level.INFO, "Adding Player: " + playerInfo);

    String sql = "INSERT INTO players (FirstName, LastName, MLBTeam, Position, PlayerString) " +
                "VALUES (?, ?, ?, ?, ?)";
    prepareAndExecuteStatementUpdate(sql, playerInfo.firstName, playerInfo.lastName, playerInfo.MLBTeam, playerInfo.Position, playerString);

    logger.log(Level.INFO, "Added.");
  }

  private static void updatePlayerFields(PlayerInfo playerInfo, String playerString) throws FailedPlayer {

    logger.log(Level.INFO, "Updating fields on Player: " + playerInfo);

    String sql = "SELECT ID, MLBTeam, Position, PlayerString FROM players WHERE FirstName = ? AND LastName = ?";
    ResultSet singleRow = prepareAndExecuteStatementFetch(sql, playerInfo.firstName, playerInfo.lastName);

    if (!hasMoreElements(singleRow)) {
      throw new FailedPlayer(-1, "Shouldn't have entered here if there wasn't exactly one row that matched the name: " + playerInfo);
    }

    Integer id = getInt(singleRow, "ID");
    String existingMLBTeam = getString(singleRow, "MLBTeam");
    String existingPosition = getString(singleRow, "Position");
    String existingPlayerString = getString(singleRow, "PlayerString");

    Map<String, String> changedFields = Maps.newHashMap();

    if (!existingMLBTeam.equals(playerInfo.MLBTeam)) {
      changedFields.put("MLBTeam", playerInfo.MLBTeam);
    }
    if (!existingPosition.equals(playerInfo.Position)) {
      changedFields.put("Position", playerInfo.Position);
    }
    if (!existingPlayerString.equals(playerString)) {
      changedFields.put("PlayerString", playerString);
    }

    if (changedFields.isEmpty()) {
      throw new FailedPlayer(id, "Shouldn't have entered update method because single player match already has same MLBTeam and Position: " + playerInfo);
    }

    String updateSQL = "UPDATE players SET ";

    List<String> clauses = Lists.newArrayList();
    List<Object> objects = Lists.newArrayList();
    for (String columnName : changedFields.keySet()) {
      clauses.add(columnName + " = ?");
      objects.add(changedFields.get(columnName));
    }

    updateSQL += Joiner.on(", ").join(clauses);
    updateSQL += " WHERE ID = ?";

    objects.add(id);

    try {
      prepareAndExecuteStatementUpdateWithException(updateSQL, objects);
    } catch (SQLException e) {
      throw new FailedPlayer(id, "Error updating player: " + playerInfo);
    }


    logger.log(Level.INFO, "UPDATED: " + Joiner.on(", ").join(changedFields.keySet()));

  }

}
