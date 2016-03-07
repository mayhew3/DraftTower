package com.mayhew3.drafttower.server.database;

import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class DailyPlayersDownloader {

  public static void main(String... args) throws IOException, ParseException, SQLException {
    DatabaseConnection connection = new DatabaseConnection();

    DailyBatter.prepareStatement(connection);
    DailyPitcher.prepareStatement(connection);

    DateTime start = new DateTime(2015, 4, 5, 0, 0).withTimeAtStartOfDay();
    DateTime end = new DateTime(2015, 4, 7, 0, 0).withTimeAtStartOfDay();

    Interval interval = new Interval(start, end);

    DateTime iteratorDay = start;

    while (interval.contains(iteratorDay)) {
     /* SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
      String dateFormatted = simpleDateFormat.format(iteratorDay.toDate());

      debug("Downloading Batters on " + dateFormatted);

      List<DailyPlayer> dailyPlayers = new ArrayList<>();

      // save BATTERS
      for (int i = 1; i < 5; i++) {
        String urlString = "http://uncharted.baseball.cbssports.com/print/csv/stats/stats-main/all:C:1B:2B:3B:SS:OF:DH/select:p:" + dateFormatted + ":" + dateFormatted + "/AllNonCalculated1";

        InputStream inputStream = new URL(urlString).openStream();

        PlayerListParser playerListParser = new PlayerListParser(PlayerType.BATTER, new InputStreamReader(inputStream));
        playerListParser.pullPlayersIntoList(iteratorDay);
        dailyPlayers = playerListParser.getPlayers();
      }

      for (DailyPlayer dailyPlayer : dailyPlayers) {
        dailyPlayer.updateDatabase(connection);
      }


      System.out.println("Downloading Pitchers on " + simpleDateFormat.format(iteratorDay.toDate()));

      dailyPlayers = new ArrayList<>();

      // save PITCHERS
      for (int i = 1; i < 4; i++) {
        String urlString = "http://uncharted.baseball.cbssports.com/print/csv/stats/stats-main/all:P/select:p:" + dateFormatted + ":" + dateFormatted + "/AllNonCalculated1";

        InputStream inputStream = new URL(urlString).openStream();

        PlayerListParser playerListParser = new PlayerListParser(PlayerType.PITCHER, new InputStreamReader(inputStream));
        playerListParser.pullPlayersIntoList(iteratorDay);
        dailyPlayers = playerListParser.getPlayers();
      }


      for (DailyPlayer dailyPlayer : dailyPlayers) {
        dailyPlayer.updateDatabase(connection);
      }

*/
      iteratorDay = iteratorDay.plusDays(1);
    }

  }


  protected static void debug(Object object) {
    System.out.println(object);
  }

}
