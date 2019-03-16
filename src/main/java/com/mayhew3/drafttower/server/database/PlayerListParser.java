package com.mayhew3.drafttower.server.database;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mayhew3.drafttower.server.database.dataobject.*;
import org.joda.time.LocalDate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

class PlayerListParser {
  private Reader reader;
  private TmpStatTableFactory factory;

  private LocalDate statDate;

  private String playerHeader = "Player";
  private List<String> statColumns;
  private Map<String, Integer> columnNumbers;

  private SQLConnection connection;

  private static final Logger logger = Logger.getLogger(PlayerListParser.class.getName());

  PlayerListParser(Reader reader, TmpStatTableFactory factory, LocalDate statDate, List<String> statColumns, SQLConnection connection) {
    this.reader = reader;
    this.factory = factory;
    this.statDate = statDate;
    this.statColumns = statColumns;
    this.connection = connection;
  }

  private void deleteExistingStatsForDate() throws SQLException {
    TmpStatTable tmpStatTable = factory.createTmpStatTable();
    String sql = "DELETE FROM " + tmpStatTable.getTableName() + " " +
        "WHERE StatDate = ? ";

    connection.prepareAndExecuteStatementUpdate(sql, new java.sql.Date(statDate.toDate().getTime()));
  }

  void uploadPlayersToDatabase() throws IOException, SQLException {
    deleteExistingStatsForDate();

    BufferedReader bufferedReader = new BufferedReader(reader);
    findHeaderRow(bufferedReader);

    String line;
    int i = 1;

    while ((line = bufferedReader.readLine()) != null) {
      List<String> fieldValues = splitRowIntoColumns(line);

      if (fieldValues.size() > 1) {
        TmpStatTable player = factory.createTmpStatTable();
        player.initializeForInsert();

        populateValues(fieldValues, player);
        player.statDate.changeValue(statDate.toDate());

        player.commit(connection);
        logger.log(Level.INFO, "- Player " + i + " '" + player.player.getValue() + "' uploaded.");
        i++;
      } else {
        logger.log(Level.INFO, "Row skipped: " + line);
      }
    }
  }

  private void populateValues(List<String> fieldValues, DataObject dataObject) {
    populateValue(fieldValues, dataObject, playerHeader);
    for (String columnName : statColumns) {
      populateValue(fieldValues, dataObject, columnName);
    }
  }

  private void populateValue(List<String> fieldValues, DataObject dataObject, String columnName) {
    Integer columnNumber = columnNumbers.get(columnName);
    String statValue = fieldValues.get(columnNumber);

    String modifiedValue = transformSpecialValue(columnName, statValue);
    modifiedValue = modifiedValue.trim();

    FieldValue fieldValue = dataObject.getFieldWithName(columnName);
    fieldValue.changeValueFromString(modifiedValue);
  }

  private String transformSpecialValue(String columnName, String statValue) {
    if ("Eligible".equals(columnName)) {
      return statValue.replace("\"", "");
    } else if (playerHeader.equals(columnName)) {
      String replaced = statValue
          .replace("\"", "")
          .replace(" | ", " ")
          .replace("*", "");
      List<String> split = Lists.newArrayList(replaced.split(" "));
      String lastBit = split.get(split.size() - 1);
      if (lastBit.startsWith("$")) {
        split.remove(lastBit);
        return Joiner.on(" ").join(split).trim();
      } else {
        return replaced;
      }
    } else {
      return statValue;
    }
  }

  private void findHeaderRow(BufferedReader bufferedReader) throws IOException {
    List<String> bestHeaders = new ArrayList<>();

    String line;
    while ((line = bufferedReader.readLine()) != null) {
      List<String> allFields = splitRowIntoColumns(line);

      if (hasAllStats(allFields)) {
        initializeColumnNumbers(allFields);
        return;
      }

      if (allFields.size() > bestHeaders.size()) {
        bestHeaders = allFields;
      }
    }

    throw new RuntimeException("No header row found. Closest match: '" + bestHeaders + "'. Looking for '" + statColumns + "'");
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



}
