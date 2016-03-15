package com.mayhew3.drafttower.server.database;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExistingPlayerUpdater {

  private SQLConnection connection;

  private static final Logger logger = Logger.getLogger(ExistingPlayerUpdater.class.getName());

  public ExistingPlayerUpdater(SQLConnection connection) {
    this.connection = connection;
  }

  public void updateDatabase() throws SQLException {
    splitNames();
  }

  private void splitNames() throws SQLException {
    logger.log(Level.INFO, "Splitting names.");

    String sql = "SELECT * FROM Players " +
        " WHERE NewPlayerString <> PlayerString" +
        " OR LastName IS NULL";

    ResultSet resultSet = connection.prepareAndExecuteStatementFetch(sql);

    List<Integer> failures = Lists.newArrayList();
    List<FailedPlayer> failedPlayers = Lists.newArrayList();

    int i = 1;
    while (resultSet.next()) {
      try {
        int id = resultSet.getInt("ID");
        String newPlayerString = resultSet.getString("NewPlayerString");
        String oldPlayerString = resultSet.getString("PlayerString");

        PlayerInfo existingPlayer = new PlayerInfo();
        existingPlayer.firstName = resultSet.getString("FirstName");
        existingPlayer.lastName = resultSet.getString("LastName");
        existingPlayer.MLBTeam = resultSet.getString("MLBTeam");
        existingPlayer.Position = resultSet.getString("Position");

        logger.log(Level.INFO, "Running on player '" + newPlayerString + "' (" + id + ")...");
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
      logger.log(Level.INFO, "Failed to fetch " + failures.size() + " rows from players table:");
      logger.log(Level.INFO, "Rows {" + Joiner.on(", ").join(failures) + "}");
    }

    if (!failedPlayers.isEmpty()) {
      logger.log(Level.INFO, "String parse failed on " + failedPlayers.size() + " rows from players table:");
      for (FailedPlayer failedPlayer : failedPlayers) {
        logger.log(Level.INFO, "ID " + failedPlayer.id + ": " + failedPlayer.message);
      }
    }
  }


  private void updatePlayerFields(int id, String oldPlayerString, String newPlayerString, PlayerInfo existingPlayer) throws FailedPlayer, SQLException {
    PlayerInfo changedPlayer = parseFromString(id, newPlayerString, existingPlayer);

    if (existingPlayer.lastName == null) {

      logger.log(Level.INFO, "Splitting '" + newPlayerString + "' into parts.");

      String sql = "UPDATE Players SET FirstName = ?, LastName = ?, MLBTeam = ?, Position = ?, UpdateTime = NOW() " +
                    "WHERE ID = ?";
      connection.prepareAndExecuteStatementUpdate(sql, changedPlayer.firstName, changedPlayer.lastName, changedPlayer.MLBTeam, changedPlayer.Position, id);

      logger.log(Level.INFO, "Updated. " + changedPlayer);

    } else {

      logger.log(Level.INFO, "Updating '" + oldPlayerString + "' (" + existingPlayer + ") to '" + newPlayerString + "'");

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
          throw new FailedPlayer(id, "User chose not to change player name from " + existingPlayer + " (" + oldPlayerString + ") to " + changedPlayer);
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
       connection.prepareAndExecuteStatementUpdate(updateSQL, objects);
      } catch (SQLException e) {
        throw new FailedPlayer(id, "Error updating player: " + changedPlayer);
      }


      logger.log(Level.INFO, "UPDATED: " + Joiner.on(", ").join(changedFields.keySet()));
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

  private static PlayerInfo parseFromString(int id, String playerString, PlayerInfo existingPlayer) throws FailedPlayer {
    PlayerInfo playerInfo = new PlayerInfo();

    List<String> parts = Lists.newArrayList(playerString.split(" "));
    int numParts = parts.size();

    if (numParts < 4) {
      throw new FailedPlayer(id, "Found player with fewer than 4 symbols: '" +
                                      playerString + "'");
    }

    playerInfo.MLBTeam = Iterables.getLast(parts);
    parts.remove(playerInfo.MLBTeam);

    playerInfo.Position = Iterables.getLast(parts);
    parts.remove(playerInfo.Position);

    if (playerInfo.MLBTeam.length() < 2 || playerInfo.MLBTeam.length() > 3) {
      throw new FailedPlayer(id, "Incorrect team name '" + playerInfo.MLBTeam + "', from player string '" + playerString + "'");
    }

    if (playerInfo.Position.length() < 1 || playerInfo.Position.length() > 2) {
      throw new FailedPlayer(id, "Incorrect position '" + playerInfo.Position + "', from player string '" + playerString + "'");
    }

    if (parts.size() > 2) {

      List<String> potentialLastNames = new ArrayList<>();
      List<String> potentialFirstNames = new ArrayList<>();

      Integer maxFirstName = parts.size() - 1;
      for (int firstNameSize = 1; firstNameSize <= maxFirstName; firstNameSize++) {
        List<String> lastNameStrings = parts.subList(firstNameSize, parts.size());

        Joiner joiner = Joiner.on(" ");
        String potentialLastName = joiner.join(lastNameStrings);
        List<String> firstNameStrings = parts.subList(0, firstNameSize);
        String potentialFirstName = joiner.join(firstNameStrings);

        if (Objects.equals(existingPlayer.lastName, potentialLastName)) {

          playerInfo.firstName = potentialFirstName;
          playerInfo.lastName = potentialLastName;

          return playerInfo;
        }

        potentialLastNames.add(potentialLastName);
        potentialFirstNames.add(potentialFirstName);
      }

      List<String> displayOptions = new ArrayList<>();
      for (int i = 0; i < potentialLastNames.size(); i++) {
        displayOptions.add(i+1 + ") '" + potentialLastNames.get(i) + "'");
      }
      Joiner commaJoiner = Joiner.on(", ");
      String selectedOption = InputGetter.grabInput("Potential last names: " + commaJoiner.join(displayOptions) + ", 0) None of these. ");
      Integer selectedIndex = Integer.valueOf(selectedOption);

      if (selectedIndex == null || selectedIndex == 0) {
        throw new FailedPlayer(id, "Full name had more than three parts, and no combination matched existing LastName field.");
      } else {
        String selectedLastName = potentialLastNames.get(selectedIndex - 1);
        String selectedFirstName = potentialFirstNames.get(selectedIndex - 1);

        playerInfo.lastName = selectedLastName;
        playerInfo.firstName = selectedFirstName;

        return playerInfo;
      }
    } else if (parts.size() < 2) {
      throw new FailedPlayer(id, "Found fewer than two parts to full name.");
    } else {
      playerInfo.firstName = parts.get(0);
      playerInfo.lastName = parts.get(1);

      return playerInfo;
    }
  }



}
