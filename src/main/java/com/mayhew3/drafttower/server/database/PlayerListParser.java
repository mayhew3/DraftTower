package com.mayhew3.drafttower.server.database;

import com.google.common.collect.Lists;
import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class PlayerListParser {
  private PlayerType playerType;
  private Reader reader;

  private List<DailyPlayer> players;

  public PlayerListParser(PlayerType playerType, Reader reader) {
    this.playerType = playerType;
    this.reader = reader;
    this.players = new ArrayList<>();
  }

  public List<DailyPlayer> getPlayers() {
    return players;
  }

  public void pullPlayersIntoList(DateTime statDate) throws IOException {
    BufferedReader bufferedReader = new BufferedReader(reader);

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
