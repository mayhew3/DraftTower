package com.mayhew3.drafttower.server.database;

import com.google.common.collect.Lists;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class ProjectionsUploader {
  private SQLConnection connection;

  public static List<String> batterColumns = Lists.newArrayList(
      "AB",
      "R",
      "1B",
      "2B",
      "3B",
      "HR",
      "RBI",
      "BB",
      "KO",
      "SB",
      "CS"
  );

  public ProjectionsUploader(SQLConnection connection) {
    this.connection = connection;
  }

  public void updateDatabase() throws IOException {
    File file = new File("database/2016/batterProjections0306.csv");
    FileReader fileReader = new FileReader(file);

    PlayerListParser playerListParser = new PlayerListParser(PlayerType.BATTER, fileReader, batterColumns);
    playerListParser.pullPlayersIntoList();
  }
}
