package com.mayhew3.drafttower.server.database;

import com.google.common.collect.Lists;
import org.joda.time.DateMidnight;
import org.joda.time.Interval;

import java.io.*;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DailyPlayersUploader {

  private enum PlayerType {BATTER, PITCHER}

  private static DatabaseConnection _utility;

  public static void main(String[] args) throws IOException, ParseException, SQLException {
    _utility = new DatabaseConnection();

    DailyBatter.prepareStatement(_utility);
    DailyPitcher.prepareStatement(_utility);

    DateMidnight start = new DateMidnight(2015, 4, 5);
    DateMidnight end = new DateMidnight(2015, 4, 7);

    Interval interval = new Interval(start, end);

    DateMidnight iteratorDay = start;

    while (interval.contains(iteratorDay)) {
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
      String dateFormatted = simpleDateFormat.format(iteratorDay.toDate());

      debug("Downloading Batters on " + dateFormatted);

      List<DailyPlayer> dailyPlayers = new ArrayList<>();

      // save BATTERS
      for (int i = 1; i < 5; i++) {
        URL url = new URL("http://uncharted.baseball.cbssports.com/print/csv/stats/stats-main/all:C:1B:2B:3B:SS:OF:DH/select:p:" + dateFormatted + ":" + dateFormatted + "/AllNonCalculated1");
        InputStream in = url.openStream();
        InputStreamReader in1 = new InputStreamReader(in);
        BufferedReader bufferedReader = new BufferedReader(in1);

        pullBattersIntoList(bufferedReader, iteratorDay, dailyPlayers);
      }

      for (DailyPlayer dailyPlayer : dailyPlayers) {
        dailyPlayer.updateDatabase(_utility);
      }


      System.out.println("Downloading Pitchers on " + simpleDateFormat.format(iteratorDay.toDate()));

      dailyPlayers = new ArrayList<>();

      // save PITCHERS
      for (int i = 1; i < 4; i++) {
        URL url = new URL("http://uncharted.baseball.cbssports.com/print/csv/stats/stats-main/all:P/select:p:" + dateFormatted + ":" + dateFormatted + "/AllNonCalculated1");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream()));

        pullPitchersIntoList(bufferedReader, iteratorDay, dailyPlayers);
      }


      for (DailyPlayer dailyPlayer : dailyPlayers) {
        dailyPlayer.updateDatabase(_utility);
      }


      iteratorDay = iteratorDay.plusDays(1);
    }

  }


  protected static void debug(Object object) {
    System.out.println(object);
  }


  private static void pullBattersIntoList(BufferedReader bufferedReader, DateMidnight statDate, List<DailyPlayer> batters) throws IOException, ParseException, SQLException {
    pullPlayersIntoList(bufferedReader, statDate, batters, PlayerType.BATTER);
  }

  private static void pullPitchersIntoList(BufferedReader bufferedReader, DateMidnight statDate, List<DailyPlayer> batters) throws IOException, ParseException, SQLException {
    pullPlayersIntoList(bufferedReader, statDate, batters, PlayerType.PITCHER);
  }

  private static void pullPlayersIntoList(BufferedReader bufferedReader, DateMidnight statDate, List<DailyPlayer> batters, PlayerType type) throws IOException {
    List<String> fieldNames = null;

    String line;
    Integer rowNum = 0;

    while ((line = bufferedReader.readLine()) != null) {
      rowNum++;

      if (rowNum > 1) {
        String[] allFields = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

        if (rowNum == 2) {
          fieldNames = Lists.newArrayList(allFields);
        } else {
          ArrayList<String> fieldValues = Lists.newArrayList(allFields);

          String playerName = fieldValues.get(0).replace("\"", "").trim();
          DailyPlayer batter = findPlayerWithName(batters, playerName);

          if (batter == null) {
            batter = type == PlayerType.BATTER ? new DailyBatter(statDate) : new DailyPitcher(statDate);
            batter.addFields(fieldNames, fieldValues);
            batters.add(batter);
          } else {
            batter.addFields(fieldNames, fieldValues);
          }
        }
      }
    }
  }


  private static DailyPlayer findPlayerWithName(List<DailyPlayer> players, String playerName) {
    for (DailyPlayer player : players) {
      if (playerName.equals(player.getPlayerName())) {
        return player;
      }
    }
    return null;
  }

  private static List<PlayerField> combineListsIntoMap(List<String> fieldNames, List<String> values) {
    List<PlayerField> fields = new ArrayList<>();
    int i = 0;
    for (String fieldName : fieldNames) {
      String value = values.get(i);

      value = value.replace("\"", "");

      PlayerField playerField = null;
      Integer integerValue = getIntegerValue(value);
      if (integerValue != null) {
        playerField = new PlayerField(fieldName, integerValue);
      } else {

        playerField = new PlayerField(fieldName, value);
      }
      fields.add(playerField);

      i++;
    }
    return fields;
  }

  private static Integer getIntegerValue(String value) {
    try {
      Integer integer = Integer.valueOf(value);
      return integer;
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private static void updateDatabase(List<PlayerField> fields, Date statDate,
                                     PreparedStatement selectPrepared,
                                     PreparedStatement updatePrepared,
                                     PreparedStatement insertPrepared) throws SQLException {
    String playerString = (String) getValueForField(fields, "Player");

    ResultSet resultSet = _utility.executePreparedStatementWithParams(selectPrepared, playerString, statDate);
    _utility.hasMoreElements(resultSet);
    Integer existingRows = resultSet.getInt("ExistingRows");


    if (existingRows == 0) {
      List<Object> values = getValuesList(fields);
      _utility.executePreparedUpdateWithParamsWithoutClose(insertPrepared, values);
    } else {
      removeFieldWithName(fields, "StatDate");
      List<Object> values = getValuesList(fields);
      values.add(playerString);
      values.add(statDate);
      _utility.executePreparedUpdateWithParamsWithoutClose(updatePrepared, values);
    }
  }

  private static Object getValueForField(List<PlayerField> fields, String fieldName) {
    for (PlayerField field : fields) {
      if (field.fieldName.equals(fieldName)) {
        return field.fieldValue;
      }
    }
    return null;
  }


  private static void removeFieldWithName(List<PlayerField> fields, String fieldName) {
    for (PlayerField field : fields) {
      if (field.fieldName.equals(fieldName)) {
        fields.remove(field);
        return;
      }
    }
  }



  private static List<Object> getValuesList(List<PlayerField> fields) {
    List<Object> objects = new ArrayList<>();
    for (PlayerField field : fields) {
      objects.add(field.fieldValue);
    }
    return objects;
  }

}
