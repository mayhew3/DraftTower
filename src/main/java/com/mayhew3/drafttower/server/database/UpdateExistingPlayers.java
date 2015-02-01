package com.mayhew3.drafttower.server.database;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class UpdateExistingPlayers extends DatabaseUtility {

  public static void main(String[] args) {
    initConnection();

    System.out.println("Splitting names.");

    String sql = "SELECT * FROM Players " +
        " WHERE NewPlayerString <> PlayerString" +
        " OR LastName IS NULL";

    ResultSet resultSet = prepareAndExecuteStatementFetch(sql);

    List<Integer> failures = Lists.newArrayList();
    List<FailedPlayer> failedPlayers = Lists.newArrayList();

    int i = 1;
    while (hasMoreElements(resultSet)) {
      try {
        int id = resultSet.getInt("ID");
        String newPlayerString = resultSet.getString("NewPlayerString");
        String oldPlayerString = resultSet.getString("PlayerString");

        PlayerInfo existingPlayer = new PlayerInfo();
        existingPlayer.firstName = getString(resultSet, "FirstName");
        existingPlayer.lastName = getString(resultSet, "LastName");
        existingPlayer.MLBTeam = getString(resultSet, "MLBTeam");
        existingPlayer.Position = getString(resultSet, "Position");

        System.out.println("Running on player '" + newPlayerString + "' (" + id + ")...");
        updatePlayerFields(id, oldPlayerString, newPlayerString, existingPlayer);
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


  private static void updatePlayerFields(int id, String oldPlayerString, String newPlayerString, PlayerInfo existingPlayer) throws FailedPlayer {
    PlayerInfo changedPlayer = parseFromString(id, oldPlayerString);

    if (existingPlayer.lastName == null) {

      System.out.println("Splitting '" + oldPlayerString + "' into parts.");

      String sql = "UPDATE Players SET FirstName = ?, LastName = ?, MLBTeam = ?, Position = ?, UpdateTime = NOW() " +
                    "WHERE ID = ?";
      prepareAndExecuteStatementUpdate(sql, changedPlayer.firstName, changedPlayer.lastName, changedPlayer.MLBTeam, changedPlayer.Position, id);

      System.out.println("Updated. " + changedPlayer);

    } else {

      System.out.println("Updating '" + oldPlayerString + "' to '" + newPlayerString + "'");

      Map<String, String> changedFields = Maps.newHashMap();

      if (!existingPlayer.firstName.equals(changedPlayer.firstName)) {
        changedFields.put("FirstName", changedPlayer.firstName);
      }
      if (!existingPlayer.lastName.equals(changedPlayer.lastName)) {
        changedFields.put("LastName", changedPlayer.lastName);
      }
      if (!existingPlayer.MLBTeam.equals(changedPlayer.MLBTeam)) {
        changedFields.put("MLBTeam", changedPlayer.MLBTeam);
      }
      if (!existingPlayer.Position.equals(changedPlayer.Position)) {
        changedFields.put("Position", changedPlayer.Position);
      }
      if (!oldPlayerString.equals(newPlayerString)) {
        changedFields.put("PlayerString", newPlayerString);
      }

      int numChanged = changedFields.keySet().size();
      if (changedFields.keySet().contains("Position") && areCompatible(existingPlayer.Position, changedPlayer.Position)) {
        numChanged--;
      }

      if ((changedFields.keySet().contains("FirstName") ||
          changedFields.keySet().contains("LastName")) && numChanged > 2) {
        Integer choice = new Integer(InputGetter.grabInput("PlayerID " + id + " changed name. Confirm overwrite? (0 for no, 1 for yes.)"));
        if (choice < 0 || choice > 1) {
          throw new IllegalArgumentException("Choice must be 0 or 1.");
        } else if (choice == 0) {
          throw new FailedPlayer(id, "User chose not to change player name from " + existingPlayer + " to " + changedPlayer);
        }
      }

      if (changedFields.isEmpty()) {
        throw new FailedPlayer(id, "Shouldn't have entered update method because player already matches: " + changedPlayer);
      }

      String updateSQL = "UPDATE Players SET ";

      List<String> clauses = Lists.newArrayList();
      List<Object> objects = Lists.newArrayList();
      for (String columnName : changedFields.keySet()) {
        clauses.add(columnName + " = ?");
        objects.add(changedFields.get(columnName));
      }

      updateSQL += Joiner.on(", ").join(clauses);
      updateSQL += ", UpdateTime = NOW()";
      updateSQL += " WHERE ID = ?";

      objects.add(id);

      try {
        prepareAndExecuteStatementUpdateWithException(updateSQL, objects);
      } catch (SQLException e) {
        throw new FailedPlayer(id, "Error updating player: " + changedPlayer);
      }


      System.out.println("UPDATED: " + Joiner.on(", ").join(changedFields.keySet()));
    }

  }

  private static boolean areCompatible(String position1, String position2) {
    List<Object> outFieldPositions = Lists.newArrayList((Object) "OF", "LF", "CF", "RF");
    List<Object> pitchingPositions = Lists.newArrayList((Object) "SP", "RP", "P");

    if (outFieldPositions.contains(position1) && outFieldPositions.contains(position2)) {
      return true;
    }
    if (pitchingPositions.contains(position1) && pitchingPositions.contains(position2)) {
      return true;
    }
    return position1.equals(position2);
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

}
