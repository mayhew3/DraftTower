package com.mayhew3.drafttower.server.database;

import com.google.common.collect.Lists;
import org.joda.time.LocalDate;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

public class ProjectionsUploader {
  private SQLConnection connection;
  private LocalDate statsDate;

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

  public ProjectionsUploader(SQLConnection connection, LocalDate statsDate) {
    this.connection = connection;
    this.statsDate = statsDate;
  }

  public void updateDatabase() throws IOException, SQLException {
    updatePlayers(PlayerType.BATTER, statsDate);
    updatePlayers(PlayerType.PITCHER, statsDate);
  }

  private void updatePlayers(PlayerType playerType, LocalDate statsDate) throws IOException, SQLException {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMdd");

    String pathname = "database/" + statsDate.getYear() + "/" + playerType.name + "Projections" + simpleDateFormat.format(statsDate.toDate()) + ".csv";

    File file = new File(pathname);
    FileReader fileReader = new FileReader(file);

    List<String> columns = playerType == PlayerType.BATTER ? batterColumns : pitcherColumns;

    PlayerListParser playerListParser = new PlayerListParser(playerType, fileReader, statsDate, columns);
    playerListParser.uploadPlayersToDatabase(connection);
  }
}
