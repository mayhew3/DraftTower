package com.mayhew3.drafttower.server.database;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mayhew3.drafttower.server.database.dataobject.*;
import org.joda.time.LocalDate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EligibilityParser {
  private Reader reader;

  private LocalDate statDate;

  private String playerHeader = "Player";
  private List<String> statColumns;
  private Map<String, Integer> columnNumbers;

  private static final Logger logger = Logger.getLogger(EligibilityParser.class.getName());

  public EligibilityParser(Reader reader, LocalDate statDate, List<String> statColumns) {
    this.reader = reader;
    this.statDate = statDate;
    this.statColumns = statColumns;
  }

  public void uploadPlayersToDatabase(SQLConnection connection) throws IOException, SQLException {
    BufferedReader bufferedReader = new BufferedReader(reader);

    findHeaderRow(bufferedReader);

    String line;

    int i = 1;

    while ((line = bufferedReader.readLine()) != null) {

      List<String> fieldValues = splitRowIntoColumns(line);

      if (fieldValues.size() > 1) {

        TmpEligibility player = new TmpEligibility();

        player.initializeForInsert();

        String playerString = getColumnValue(playerHeader, fieldValues);
        String playerModified = playerString.replace(" | ", " ");
        player.player.changeValueFromString(playerModified);

        String eligibleWithQuotes = getColumnValue("Eligible", fieldValues);
        String eligible = eligibleWithQuotes.replace("\"", "");
        player.eligible.changeValueFromString(eligible);

        player.statDate.changeValue(statDate.toDate());

        player.commit(connection);

        logger.log(Level.INFO, "- Player " + i + " '" + player.player.getValue() + "' uploaded.");

        i++;
      } else {
        logger.log(Level.INFO, "Row skipped: " + line);
      }
    }
  }

  private String getColumnValue(String columnName, List<String> fieldValues) {
    Integer columnNumber = columnNumbers.get(columnName);
    return fieldValues.get(columnNumber);
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



}
