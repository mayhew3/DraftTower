package com.mayhew3.drafttower.server.database;

import com.google.common.collect.Lists;
import org.joda.time.DateMidnight;
import org.joda.time.Interval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class DailyPlayersUploader extends DatabaseUtility {

  private enum PlayerType {BATTER, PITCHER}

  public static void main(String[] args) throws IOException, ParseException, SQLException {
    DatabaseUtility utility = new DailyPlayersUploader();

    DailyBatter.prepareStatement(utility);
    DailyPitcher.prepareStatement(utility);

    DateMidnight start = new DateMidnight(2013, 9, 30);
    DateMidnight end = new DateMidnight(2013, 10, 1);

    Interval interval = new Interval(start, end);

    DateMidnight iteratorDay = start;

    while (interval.contains(iteratorDay)) {
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
      String dateFormatted = simpleDateFormat.format(iteratorDay.toDate());

      logger.log(Level.INFO, "Downloading Batters on " + dateFormatted);

      List<DailyPlayer> dailyPlayers = new ArrayList<>();

      // save BATTERS
      for (int i = 1; i < 5; i++) {
        File file = new File("resources/Dailies/Batters_" + dateFormatted + "_Stats" + i + ".csv");

        pullBattersIntoList(file, iteratorDay, dailyPlayers);
      }

      for (DailyPlayer dailyPlayer : dailyPlayers) {
        dailyPlayer.updateDatabase(utility);
      }


      logger.log(Level.INFO, "Downloading Pitchers on " + simpleDateFormat.format(iteratorDay.toDate()));

      dailyPlayers = new ArrayList<>();

      // save PITCHERS
      for (int i = 1; i < 4; i++) {
        File file = new File("resources/Dailies/Pitchers_" + dateFormatted + "_Stats" + i + ".csv");

        pullPitchersIntoList(file, iteratorDay, dailyPlayers);
      }


      for (DailyPlayer dailyPlayer : dailyPlayers) {
        dailyPlayer.updateDatabase(utility);
      }


      iteratorDay = iteratorDay.plusDays(1);
    }

  }


  private static void pullBattersIntoList(File file, DateMidnight statDate, List<DailyPlayer> batters) throws IOException, ParseException, SQLException {
    pullPlayersIntoList(file, statDate, batters, PlayerType.BATTER);
  }

  private static void pullPitchersIntoList(File file, DateMidnight statDate, List<DailyPlayer> batters) throws IOException, ParseException, SQLException {
    pullPlayersIntoList(file, statDate, batters, PlayerType.PITCHER);
  }

  private static void pullPlayersIntoList(File file, DateMidnight statDate, List<DailyPlayer> batters, PlayerType type) throws IOException {
    FileReader fileReader = new FileReader(file);
    BufferedReader bufferedReader = new BufferedReader(fileReader);

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


}
