package com.mayhew3.drafttower.server.database;

import org.joda.time.LocalDate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

public class EligibilityUploaderRunner {
  public static void main(String... args) throws URISyntaxException, SQLException, IOException {
    LocalDate statsDate = new LocalDate(2016, 3, 6);
    SQLConnection connection = new MySQLConnectionFactory().createConnection();
    EligibilityUploader eligibilityUploader = new EligibilityUploader(connection, statsDate);
    eligibilityUploader.updateDatabase();
  }
}
