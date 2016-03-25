package com.mayhew3.drafttower.server.database;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mayhew3.drafttower.server.database.dataobject.CbsID;
import com.mayhew3.drafttower.server.database.dataobject.FieldValue;
import com.mayhew3.drafttower.server.database.dataobject.Player;

import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerStringSplitter {

  private SQLConnection connection;

  private static final Logger logger = Logger.getLogger(PlayerStringSplitter.class.getName());

  public static void main(String... args) throws URISyntaxException, SQLException {
    PlayerStringSplitter playerStringSplitter = new PlayerStringSplitter(new MySQLConnectionFactory().createConnection());
    playerStringSplitter.updateDatabase();
  }

  public PlayerStringSplitter(SQLConnection connection) {
    this.connection = connection;
  }

  public void updateDatabase() throws SQLException {
    insertNewPlayers();
    splitNames();
  }

  private void insertNewPlayers() throws SQLException {
    String sql = "INSERT INTO players (PlayerString, CBS_ID, CreateTime, UpdateTime) " +
        "SELECT cbs.PlayerString, cbs.CBS_ID, NOW(), NOW() " +
        "FROM cbsids cbs " +
        "LEFT OUTER JOIN players p " +
        "  ON cbs.CBS_ID = p.CBS_ID " +
        "WHERE p.CBS_ID IS NULL;";
    connection.prepareAndExecuteStatementUpdate(sql);

    // todo: print and validate number of rows inserted.
  }

  private void splitNames() throws SQLException {
    logger.log(Level.INFO, "Splitting names.");

    String sql = "SELECT p.* " +
        "FROM players p " +
        "INNER JOIN cbsids cbs " +
        " ON p.cbs_id = cbs.cbs_id " +
        "WHERE p.PlayerString <> cbs.PlayerString " +
        "OR p.NewPlayerString IS NOT NULL " +
        "OR p.LastName IS NULL;";

    ResultSet resultSet = connection.prepareAndExecuteStatementFetch(sql);

    List<Integer> failures = Lists.newArrayList();
    List<FailedPlayer> failedPlayers = Lists.newArrayList();

    int i = 1;
    while (resultSet.next()) {
      try {
        Player player = new Player();
        player.initializeFromDBObject(resultSet);

        logger.log(Level.INFO, "Running on player '" + player + "' (" + player.id.getValue() + ")...");
        updatePlayerFields(player);
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


  private void updatePlayerFields(Player player) throws FailedPlayer, SQLException {
    String sql = "SELECT * FROM cbsids WHERE cbs_id = ?";
    ResultSet resultSet = connection.prepareAndExecuteStatementFetch(sql, player.cbs_id.getValue());

    resultSet.next();

    CbsID cbsID = new CbsID();
    cbsID.initializeFromDBObject(resultSet);

    Integer id = cbsID.cbs_id.getValue();

    String newPlayerString = cbsID.playerString.getValue();
    String oldPlayerString = player.playerString.getValue();

    PlayerInfo changedPlayer = parseFromString(id, newPlayerString, player.lastName.getValue());

    player.firstName.changeValue(changedPlayer.firstName);
    player.lastName.changeValue(changedPlayer.lastName);
    player.mlbTeam.changeValue(changedPlayer.MLBTeam);
    player.position.changeValue(changedPlayer.Position);

    // first-time split, don't need to validate it's the same player.
    if (!newPlayerString.equals(oldPlayerString)) {
      validateChangesToExistingPlayer(player, id, newPlayerString, oldPlayerString);
    }

    player.playerString.changeValue(newPlayerString);

    if (player.hasChanged()) {
      player.updateTime.changeValue(new Date());
    }

    player.commit(connection);
  }

  private void validateChangesToExistingPlayer(Player player, Integer id, String newPlayerString, String oldPlayerString) throws SQLException, FailedPlayer {
    logger.log(Level.INFO, "Validating change of '" + oldPlayerString + "' (" + player + ") to '" + newPlayerString + "'");

    List<FieldValue> changedFields = player.getChangedFields();

    int numChanged = changedFields.size();
    if (player.position.isChanged() && areCompatible(player.position.getChangedValue(), player.position.getOriginalValue())) {
      numChanged--;
    }

    if ((player.firstName.isChanged() || player.lastName.isChanged()) && numChanged > 2) {
      if (player.matchPending.getValue() == 0 && player.newPlayerString.getValue() != null) {
        player.newPlayerString.nullValue();
        logger.log(Level.INFO, "Match approved: " + oldPlayerString + " -> " + newPlayerString);
      } else {
        player.discardAllChanges();

        player.newPlayerString.changeValue(newPlayerString);
        player.matchPending.changeValue(1);

        player.commit(connection);
        throw new FailedPlayer(id, "Too many differences for " + oldPlayerString + " -> " + newPlayerString);
      }
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

  private static PlayerInfo parseFromString(int id, String playerString, String previousLastName) throws FailedPlayer {
    List<String> validPositions = Lists.newArrayList(
        "C",
        "1B",
        "2B",
        "3B",
        "SS",
        "OF",
        "LF",
        "CF",
        "RF",
        "SP",
        "RP",
        "P"
    );

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

    if (!validPositions.contains(playerInfo.Position)) {
      String[] multiPositions = playerInfo.Position.split("-");
      if (multiPositions.length < 2 || !validPositions.contains(multiPositions[0])) {
        throw new FailedPlayer(id, "Incorrect position '" + playerInfo.Position + "', from player string '" + playerString + "'");
      } else {
        playerInfo.Position = multiPositions[0];
      }
    }

    if (parts.size() > 2) {
      return parseExtraNames(id, playerString, previousLastName, playerInfo, parts);
    } else if (parts.size() < 2) {
      throw new FailedPlayer(id, "Found fewer than two parts to full name.");
    } else {
      playerInfo.firstName = parts.get(0);
      playerInfo.lastName = parts.get(1);

      return playerInfo;
    }
  }

  private static PlayerInfo parseExtraNames(int id, String playerString, String previousLastName, PlayerInfo playerInfo, List<String> parts) throws FailedPlayer {
    // todo: check DB for existing multi-part names on other players

    List<String> potentialLastNames = new ArrayList<>();
    List<String> potentialFirstNames = new ArrayList<>();

    Integer maxFirstName = parts.size() - 1;
    for (int firstNameSize = 1; firstNameSize <= maxFirstName; firstNameSize++) {
      List<String> lastNameStrings = parts.subList(firstNameSize, parts.size());

      Joiner joiner = Joiner.on(" ");
      String potentialLastName = joiner.join(lastNameStrings);
      List<String> firstNameStrings = parts.subList(0, firstNameSize);
      String potentialFirstName = joiner.join(firstNameStrings);

      if (Objects.equals(previousLastName, potentialLastName)) {

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
    String selectedOption = InputGetter.grabInput("'" + playerString + "'  Potential last names: " + commaJoiner.join(displayOptions) + ", 0) None of these. ");
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
  }


}
