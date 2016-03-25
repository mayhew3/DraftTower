package com.mayhew3.drafttower.server.database;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

public class SplitPlayerNames extends DatabaseUtility {
  private static String tableName;

  public static void main(String[] args) {
    initConnection();

    System.out.println("Splitting names.");

//    tableName = "players";
//    tableName = "cbsids";
//    tableName = "cbs_draftaverages";
    tableName = "tmp_cbsbatting";
//    tableName = "tmp_cbspitching";

    boolean ignoreProcessed = true;

    String sql = "SELECT ID, PlayerString FROM " + tableName;

    //noinspection ConstantConditions
    if (ignoreProcessed) {
      sql += " WHERE MLBTeam IS NULL OR Position IS NULL";
    }

    ResultSet resultSet = executeQuery(sql);

    List<Integer> failures = Lists.newArrayList();
    List<FailedPlayer> failedPlayers = Lists.newArrayList();

    int i = 1;
    while (hasMoreElements(resultSet)) {
      try {
        int id = resultSet.getInt("ID");
        String playerString = resultSet.getString("PlayerString");

        System.out.println("Running on player '" + playerString + "' (" + id + ")...");
        updatePlayerFields(id, playerString);
      } catch (SQLException e) {
        failures.add(i);
      } catch (FailedPlayer fp) {
        failedPlayers.add(fp);
      } finally {
        i++;
      }
    }

    if (!failures.isEmpty()) {
      System.out.println("Failed to fetch " + failures.size() + " rows from players table:");
      System.out.println("Rows {" + Joiner.on(", ").join(failures) + "}");
    }

    if (!failedPlayers.isEmpty()) {
      System.out.println("String parse failed on " + failedPlayers.size() + " rows from players table:");
      for (FailedPlayer failedPlayer : failedPlayers) {
        System.out.println("ID " + failedPlayer.id + ": " + failedPlayer.message);
      }
    }
  }


  private static void updatePlayerFields(int id, String playerString) throws FailedPlayer {
    PlayerInfo playerInfo = parseFromString(id, playerString);

    System.out.println("'" + playerInfo.firstName + "', '" + playerInfo.lastName + "', '" + playerInfo.MLBTeam + "', '" + playerInfo.Position + "'");

    List<String> updatedFields = Lists.newArrayList();


    // todo: merge all these to one SQL update statement, might speed it up.

    if (updateFieldOnPlayerIfNeeded(id, "FirstName", playerInfo.firstName)) {
      updatedFields.add("FirstName");
    }

    if (updateFieldOnPlayerIfNeeded(id, "LastName", playerInfo.lastName)) {
      updatedFields.add("LastName");
    }

    if (updateFieldOnPlayerIfNeeded(id, "MLBTeam", playerInfo.MLBTeam)) {
      updatedFields.add("MLBTeam");
    }

    if (updateFieldOnPlayerIfNeeded(id, "Position", playerInfo.Position)) {
      updatedFields.add("Position");
    }

    if (!updatedFields.isEmpty()) {
      logger.log(Level.INFO, "UPDATED: " + Joiner.on(", ").join(updatedFields));
    }

    logger.log(Level.INFO, " ");
  }

  private static PlayerInfo parseFromString(int id, String playerString) throws FailedPlayer {
    PlayerInfo playerInfo = new PlayerInfo();

    String[] commaParts = playerString.split(", ");
    if (commaParts.length != 2) {
      throw new FailedPlayer(id, "Found player without exactly one comma.");
    }

    playerInfo.lastName = commaParts[0];

    String remainingString = commaParts[1];

    List<String> spaceParts = Lists.newArrayList(remainingString.split(" "));
    int numParts = spaceParts.size();


    if (numParts < 3) {
      throw new FailedPlayer(id, "Found player with fewer than 3 symbols after the comma: '" +
                                      remainingString + "', Player " + playerString);
    }

    playerInfo.MLBTeam = Iterables.getLast(spaceParts);
    spaceParts.remove(playerInfo.MLBTeam);

    playerInfo.Position = Iterables.getLast(spaceParts);
    spaceParts.remove(playerInfo.Position);

    if (playerInfo.MLBTeam.length() < 2) {
      throw new FailedPlayer(id, "Incorrect team name '" + playerInfo.MLBTeam + "', from remainder string '" + remainingString + "'");
    }

    if (playerInfo.Position.length() < 1) {
      throw new FailedPlayer(id, "Incorrect position '" + playerInfo.Position + "', from remainder string '" + remainingString + "'");
    }


    if (spaceParts.size() < 1) {
      throw new FailedPlayer(id, "Found no parts remaining in the first name piece.");
    }

    Joiner joiner = Joiner.on(" ");
    playerInfo.firstName = joiner.join(spaceParts);
    return playerInfo;
  }

  private static boolean updateFieldOnPlayerIfNeeded(int id, String columnName, String value) {
    ResultSet resultSet = executeQuery("SELECT " + columnName + " FROM " + tableName + " WHERE ID = " + id);

    if (hasMoreElements(resultSet)) {
      String existingValue = getString(resultSet, columnName);
      if (!Objects.equals(value, existingValue)) {
        String sql = "UPDATE " + tableName + " SET " + columnName + " = ? WHERE ID = ?";
        prepareAndExecuteStatementUpdate(sql, value, id);
        return true;
      } else {
        return false;
      }
    } else {
      throw new RuntimeException("Unable to find player with ID " + id);
    }
  }

}
