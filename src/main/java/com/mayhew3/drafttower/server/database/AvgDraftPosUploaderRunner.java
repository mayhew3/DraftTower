package com.mayhew3.drafttower.server.database;

import org.joda.time.LocalDate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

public class AvgDraftPosUploaderRunner {
  public static void main(String... args) throws URISyntaxException, SQLException, IOException {
    LocalDate statsDate = new LocalDate(2016, 3, 6);
    SQLConnection connection = new MySQLConnectionFactory().createConnection();
    AvgDraftPosUploader avgDraftPosUploader = new AvgDraftPosUploader(connection, statsDate);
    avgDraftPosUploader.updateDatabase();
  }
}
