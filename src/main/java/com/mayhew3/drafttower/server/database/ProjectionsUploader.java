package com.mayhew3.drafttower.server.database;

import com.google.common.collect.Lists;
import org.joda.time.LocalDate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
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

  public static List<String> pitcherColumns = Lists.newArrayList(
      "APP",
      "BBI",
      "BS",
      "CG",
      "ER",
      "GS",
      "HA",
      "HRA",
      "K",
      "L",
      "QS",
      "OUTS",
      "S",
      "SO",
      "W"
  );

  public ProjectionsUploader(SQLConnection connection) {
    this.connection = connection;
  }

  public void updateDatabase() throws IOException, SQLException {
    LocalDate localDate = new LocalDate(2016, 3, 6);

    updatePlayers(PlayerType.BATTER, localDate);
    updatePlayers(PlayerType.PITCHER, localDate);
  }

  private void updatePlayers(PlayerType playerType, LocalDate localDate) throws IOException, SQLException {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMdd");

    String pathname = "database/" + localDate.getYear() + "/" + playerType.name + "Projections" + simpleDateFormat.format(localDate.toDate()) + ".csv";

    File file = new File(pathname);
    FileReader fileReader = new FileReader(file);

    List<String> columns = playerType == PlayerType.BATTER ? batterColumns : pitcherColumns;

    PlayerListParser playerListParser = new PlayerListParser(playerType, fileReader, localDate, columns);
    playerListParser.uploadPlayersToDatabase(connection);
  }
}
