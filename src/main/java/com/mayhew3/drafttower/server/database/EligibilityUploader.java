package com.mayhew3.drafttower.server.database;

import com.google.common.collect.Lists;
import org.joda.time.LocalDate;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class EligibilityUploader {
  private SQLConnection connection;
  private LocalDate statsDate;

  public EligibilityUploader(SQLConnection connection, LocalDate statsDate) {
    this.connection = connection;
    this.statsDate = statsDate;
  }

  public void updateDatabase() throws IOException, SQLException {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMdd");

    String pathname = "database/" + statsDate.getYear() + "/batterEligibility" + simpleDateFormat.format(statsDate.toDate()) + ".csv";

    File file = new File(pathname);
    FileReader fileReader = new FileReader(file);

    EligibilityParser eligibilityParser = new EligibilityParser(fileReader, statsDate, Lists.newArrayList("Eligible"));
    eligibilityParser.uploadPlayersToDatabase(connection);
  }

}
