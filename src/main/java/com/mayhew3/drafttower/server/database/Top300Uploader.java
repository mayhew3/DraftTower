package com.mayhew3.drafttower.server.database;

import com.google.common.collect.Lists;
import com.mayhew3.drafttower.server.database.dataobject.TmpDraftAveragesFactory;
import com.mayhew3.drafttower.server.database.dataobject.TmpTop300Factory;
import org.joda.time.LocalDate;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class Top300Uploader {
  private SQLConnection connection;
  private LocalDate statsDate;

  public static void main(String... args) throws URISyntaxException, SQLException, IOException {
    LocalDate statsDate = DraftPrepRunner.statsDate;
    SQLConnection connection = new MySQLConnectionFactory().createConnection();
    Top300Uploader top300Uploader = new Top300Uploader(connection, statsDate);
    top300Uploader.updateDatabase();
  }

  public Top300Uploader(SQLConnection connection, LocalDate statsDate) {
    this.connection = connection;
    this.statsDate = statsDate;
  }

  public void updateDatabase() throws IOException, SQLException {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMdd");

    String pathname = "database/" + statsDate.getYear() + "/top300_" + simpleDateFormat.format(statsDate.toDate()) + ".csv";

    File file = new File(pathname);
    FileReader fileReader = new FileReader(file);

    PlayerListParser playerListParser = new PlayerListParser(fileReader, new TmpTop300Factory(), statsDate, Lists.newArrayList("Rank", "Expert"));
    playerListParser.uploadPlayersToDatabase(connection);
  }

}
