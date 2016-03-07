package com.mayhew3.drafttower.server.database;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mayhew3.drafttower.server.database.player.FieldValue;
import com.mayhew3.drafttower.server.database.player.Player;
import com.mayhew3.drafttower.server.database.player.TmpProjectionBatter;
import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlayerListParser {
  private PlayerType playerType;
  private Reader reader;

  private List<Player> players;

  private String playerHeader = "Player";
  private List<String> statColumns;
  private Map<String, Integer> columnNumbers;

  public PlayerListParser(PlayerType playerType, Reader reader, List<String> statColumns) {
    this.playerType = playerType;
    this.reader = reader;
    this.statColumns = statColumns;
    this.players = new ArrayList<>();
  }

  public List<Player> getPlayers() {
    return players;
  }

  public void pullPlayersIntoList() throws IOException {
    BufferedReader bufferedReader = new BufferedReader(reader);

    findHeaderRow(bufferedReader);

    String line;
    while ((line = bufferedReader.readLine()) != null) {

      List<String> fieldValues = splitRowIntoColumns(line);

      String playerName = getPlayerName(fieldValues);
      Player player = findPlayerWithName(players, playerName);

      if (player == null) {
        if (playerType == PlayerType.BATTER) {
          player = new TmpProjectionBatter();
        } else {
          throw new RuntimeException("Pitchers not supported yet!");
        }

        player.initializeForInsert();

        populateValues(fieldValues, player);

        players.add(player);
      } else {
        throw new RuntimeException("Huh?");
      }
    }
  }

  private String getPlayerName(List<String> fieldValues) {
    Integer playerNameIndex = columnNumbers.get(playerHeader);
    return fieldValues.get(playerNameIndex).replace("\"", "").trim();
  }

  private void populateValues(List<String> fieldValues, Player player) {
    populateValue(fieldValues, player, playerHeader);
    for (String columnName : statColumns) {
      populateValue(fieldValues, player, columnName);
    }
  }

  private void populateValue(List<String> fieldValues, Player player, String columnName) {
    Integer columnNumber = columnNumbers.get(columnName);
    String statValue = fieldValues.get(columnNumber);

    FieldValue fieldValue = player.getFieldWithName(columnName);
    fieldValue.changeValueFromString(statValue);
  }

  private BufferedReader findHeaderRow(BufferedReader bufferedReader) throws IOException {
    List<String> bestHeaders = new ArrayList<>();

    String line;
    while ((line = bufferedReader.readLine()) != null) {
      List<String> allFields = splitRowIntoColumns(line);

      if (hasAllStats(allFields)) {
        initializeColumnNumbers(allFields);
        return bufferedReader;
      }

      if (allFields.size() > bestHeaders.size()) {
        bestHeaders = allFields;
      }
    }

    throw new RuntimeException("No header row found. Closest match: '" + bestHeaders + "'");
  }

  private void initializeColumnNumbers(List<String> allFields) {
    columnNumbers = Maps.newHashMap();
    for (int i = 0; i < allFields.size(); i++) {
      String columnHeader = allFields.get(i);
      if (playerHeader.equals(columnHeader) || statColumns.contains(columnHeader)) {
        columnNumbers.put(columnHeader, i);
      }
    }
  }

  private List<String> splitRowIntoColumns(String line) {
    return Lists.newArrayList(line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)"));
  }

  private Boolean hasAllStats(List<String> headerColumns) {
    if (!headerColumns.contains(playerHeader)) {
      return false;
    }
    for (String statColumn : statColumns) {
      if (!headerColumns.contains(statColumn)) {
        return false;
      }
    }
    return true;
  }



  @Nullable
  private static Player findPlayerWithName(List<Player> players, @NotNull String playerName) {
    for (Player player : players) {
      String existingPlayerName = player.getFieldWithName("Player") == null ? null : player.getFieldWithName("Player").getFieldName();

      if (playerName.equals(existingPlayerName)) {
        return player;
      }
    }
    return null;
  }

}
