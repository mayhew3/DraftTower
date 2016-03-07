package com.mayhew3.drafttower.server.database;

import com.google.common.collect.Lists;
import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RemotePlayerList {
  private String url;
  private PlayerType playerType;

  private List<DailyPlayer> players;

  public RemotePlayerList(String url, PlayerType playerType) {
    this.url = url;
    this.playerType = playerType;
    this.players = new ArrayList<>();
  }

  public List<DailyPlayer> getPlayers() {
    return players;
  }

  public void pullPlayersIntoList(DateTime statDate) throws IOException {
    InputStream in = new URL(url).openStream();
    InputStreamReader in1 = new InputStreamReader(in);
    BufferedReader bufferedReader = new BufferedReader(in1);

    List<String> fieldNames = null;

    String line;
    Integer rowNum = 0;

    while ((line = bufferedReader.readLine()) != null) {
      rowNum++;


      // todo: find header row, instead of assuming row 1
      if (rowNum > 1) {
        String[] allFields = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

        if (rowNum == 2) {
          fieldNames = Lists.newArrayList(allFields);
        } else {
          ArrayList<String> fieldValues = Lists.newArrayList(allFields);

          String playerName = fieldValues.get(0).replace("\"", "").trim();
          DailyPlayer batter = findPlayerWithName(players, playerName);

          if (batter == null) {
            batter = playerType == PlayerType.BATTER ? new DailyBatter(statDate) : new DailyPitcher(statDate);
            batter.addFields(fieldNames, fieldValues);
            players.add(batter);
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
