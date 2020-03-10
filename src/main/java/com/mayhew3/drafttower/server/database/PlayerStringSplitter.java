package com.mayhew3.drafttower.server.database;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mayhew3.drafttower.server.database.dataobject.CbsID;
import com.mayhew3.drafttower.server.database.dataobject.FieldValue;
import com.mayhew3.drafttower.server.database.dataobject.Player;
import org.joda.time.LocalDate;

import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlayerStringSplitter implements DraftDataStep {

  private SQLConnection connection;

  private LocalDate statDate;

  private static final Logger logger = Logger.getLogger(PlayerStringSplitter.class.getName());

  public static void main(String... args) throws URISyntaxException, SQLException {
    PlayerStringSplitter playerStringSplitter = new PlayerStringSplitter(new MySQLConnectionFactory().createConnection(), DraftPrepRunner.statsDate);
    playerStringSplitter.updateDatabase();
  }

  public PlayerStringSplitter(SQLConnection connection, LocalDate statDate) {
    this.connection = connection;
    this.statDate = statDate;
  }

  @Override
  public void updateDatabase() throws SQLException {
    insertNewPlayers();
    splitNames();
  }

  @Override
  public String getStepName() {
    return "PlayerStringSplitter";
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

    CbsID cbsID = getCBSID(player.cbs_id.getValue());

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

      Player existing = existingWithChangedName(newPlayerString, cbsID.cbs_id.getValue());
      if (existing != null) {
        CbsID existingCBSID = getCBSID(existing.cbs_id.getValue());
        Integer cbsYear = existingCBSID.year.getValue();
        int statYear = statDate.getYear();
        if (cbsYear != null && statYear == cbsYear) {
          throw new RuntimeException("Found existing player with same name and current CBS year: " + newPlayerString);
        } else {
          removeCBSIDAndPlayer(existingCBSID, existing);
        }
      }
    }

    player.commit(connection);
  }

  private void removeCBSIDAndPlayer(CbsID cbsID, Player player) throws SQLException {
    String sql = "DELETE FROM " + player.getTableName() + " WHERE ID = ? ";
    connection.prepareAndExecuteStatementUpdate(sql, player.id.getValue());

    sql = "DELETE FROM " + cbsID.getTableName() + " WHERE ID = ? ";
    connection.prepareAndExecuteStatementUpdate(sql, cbsID.id.getValue());
  }

  private CbsID getCBSID(Integer cbsId) throws SQLException {
    String sql = "SELECT * FROM cbsids WHERE cbs_id = ?";
    ResultSet resultSet = connection.prepareAndExecuteStatementFetch(sql, cbsId);

    resultSet.next();

    CbsID cbsID = new CbsID();
    cbsID.initializeFromDBObject(resultSet);

    return cbsID;
  }

  private Player existingWithChangedName(String changedName, int cbs_id) throws SQLException {
    String sql = "SELECT * " +
        "FROM Players " +
        "WHERE PlayerString = ? " +
        "AND CBS_ID <> ? ";
    ResultSet resultSet = connection.prepareAndExecuteStatementFetch(sql, changedName, cbs_id);
    if (resultSet.next()) {
      Player player = new Player();
      player.initializeFromDBObject(resultSet);
      return player;
    } else {
      return null;
    }
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

  private boolean areCompatible(String position1, String position2) {
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

  private PlayerInfo parseFromString(int id, String playerString, String previousLastName) throws FailedPlayer, SQLException {
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
        "P",
        "DH"
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

  private PlayerInfo parseExtraNames(int id, String playerString, String previousLastName, PlayerInfo playerInfo, List<String> parts) throws FailedPlayer, SQLException {
    // todo: check DB for existing multi-part names on other players

    List<NameInfo> potentialNames = new ArrayList<>();

    int maxFirstName = parts.size() - 1;
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

      NameInfo nameInfo = new NameInfo();
      nameInfo.firstName = potentialFirstName;
      nameInfo.lastName = potentialLastName;
      nameInfo.firstNameMatches = findNumberOfPlayersWithFirstName(potentialFirstName);
      nameInfo.lastNameMatches = findNumberOfPlayersWithLastName(potentialLastName);
      potentialNames.add(nameInfo);
    }

    NameInfo obviousMatch = findObviousMatch(potentialNames);
    if (obviousMatch != null) {
      playerInfo.firstName = obviousMatch.firstName;
      playerInfo.lastName = obviousMatch.lastName;

      return playerInfo;
    }

    List<String> displayOptions = new ArrayList<>();
    int i = 1;
    for (NameInfo potentialName : potentialNames) {
      displayOptions.add(i + ") '" + potentialName.lastName + "'");
    }
    Joiner commaJoiner = Joiner.on(", ");
    String selectedOption = InputGetter.grabInput("'" + playerString + "'  Potential last names: " + commaJoiner.join(displayOptions) + ", 0) None of these. ");
    Integer selectedIndex = Integer.valueOf(selectedOption);

    if (selectedIndex == 0) {
      throw new FailedPlayer(id, "Full name had more than three parts, and no combination matched existing LastName field.");
    } else {
      NameInfo selectedName = potentialNames.get(selectedIndex - 1);

      playerInfo.lastName = selectedName.lastName;
      playerInfo.firstName = selectedName.firstName;

      return playerInfo;
    }
  }

  private NameInfo findObviousMatch(List<NameInfo> nameInfos) {
    NameInfo lastNameMatch = lastNameMatchesMultipleAndNoOthersDo(nameInfos);
    NameInfo firstNameMatch = firstNameMatchesMultipleAndNoOthersDo(nameInfos);
    if (lastNameMatch != null && lastNameMatch.equals(firstNameMatch)) {
      return lastNameMatch;
    } else {
      return null;
    }
  }

  private NameInfo lastNameMatchesMultipleAndNoOthersDo(List<NameInfo> nameInfos) {
    List<NameInfo> multipleMatches = new ArrayList<>();
    List<NameInfo> singleMatches = new ArrayList<>();
    for (NameInfo nameInfo : nameInfos) {
      if (nameInfo.lastNameMatches == 1) {
        singleMatches.add(nameInfo);
      } else if (nameInfo.lastNameMatches > 1) {
        multipleMatches.add(nameInfo);
      }
    }
    if (multipleMatches.size() == 1 && singleMatches.size() == 0) {
      return multipleMatches.get(0);
    } else {
      return null;
    }
  }

  private NameInfo firstNameMatchesMultipleAndNoOthersDo(List<NameInfo> nameInfos) {
    List<NameInfo> multipleMatches = new ArrayList<>();
    List<NameInfo> singleMatches = new ArrayList<>();
    for (NameInfo nameInfo : nameInfos) {
      if (nameInfo.firstNameMatches == 1) {
        singleMatches.add(nameInfo);
      } else if (nameInfo.firstNameMatches > 1) {
        multipleMatches.add(nameInfo);
      }
    }
    if (multipleMatches.size() == 1 && singleMatches.size() == 0) {
      return multipleMatches.get(0);
    } else {
      return null;
    }
  }

  public static class NameInfo {
    String firstName;
    String lastName;
    int firstNameMatches;
    int lastNameMatches;
  }

  private int findNumberOfPlayersWithLastName(String lastName) throws SQLException {
    String sql = "SELECT COUNT(1) AS nameCount FROM Players WHERE LastName = ? ";
    ResultSet resultSet = connection.prepareAndExecuteStatementFetch(sql, lastName);
    resultSet.next();
    return resultSet.getInt("nameCount");
  }

  private int findNumberOfPlayersWithFirstName(String firstName) throws SQLException {
    String sql = "SELECT COUNT(1) AS nameCount FROM Players WHERE FirstName = ? ";
    ResultSet resultSet = connection.prepareAndExecuteStatementFetch(sql, firstName);
    resultSet.next();
    return resultSet.getInt("nameCount");
  }


}
