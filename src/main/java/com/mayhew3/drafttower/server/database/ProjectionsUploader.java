package com.mayhew3.drafttower.server.database;

import com.google.common.collect.Lists;
import com.mayhew3.drafttower.server.database.dataobject.TmpProjectionBatterFactory;
import com.mayhew3.drafttower.server.database.dataobject.TmpProjectionPitcherFactory;
import com.mayhew3.drafttower.server.database.dataobject.TmpStatTableFactory;
import org.joda.time.LocalDate;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
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

  public static void main(String... args) throws URISyntaxException, SQLException, IOException {
    LocalDate statsDate = DraftPrepRunner.statsDate;
    SQLConnection connection = new MySQLConnectionFactory().createConnection();
    ProjectionsUploader projectionsUploader = new ProjectionsUploader(connection, statsDate);
    projectionsUploader.updateDatabase();
  }

  public ProjectionsUploader(SQLConnection connection, LocalDate statsDate) {
    this.connection = connection;
    this.statsDate = statsDate;
  }

  public void updateDatabase() throws IOException, SQLException {
    updatePlayers("batter", statsDate, batterColumns, new TmpProjectionBatterFactory());
    updatePlayers("pitcher", statsDate, pitcherColumns, new TmpProjectionPitcherFactory());
  }

  private void updatePlayers(String playerType, LocalDate statsDate, List<String> batterColumns, TmpStatTableFactory tmpStatTableFactory) throws IOException, SQLException {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMdd");

    String pathname = "database/" + statsDate.getYear() + "/" + playerType + "Projections" + simpleDateFormat.format(statsDate.toDate()) + ".csv";

    File file = new File(pathname);
    FileReader fileReader = new FileReader(file);

    PlayerListParser playerListParser = new PlayerListParser(fileReader, tmpStatTableFactory, statsDate, batterColumns);
    playerListParser.uploadPlayersToDatabase(connection);
  }
}
