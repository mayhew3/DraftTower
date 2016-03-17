package com.mayhew3.drafttower.server.database;

import org.joda.time.LocalDate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

public class DraftPrepRunner {
  public static void main(String... args) throws IOException, SQLException, URISyntaxException {
    SQLConnection connection = new MySQLConnectionFactory().createConnection();
    LocalDate statsDate = new LocalDate(2016, 3, 6);

    // insert CBS projections into temp tables
    ProjectionsUploader projectionsUploader = new ProjectionsUploader(connection, statsDate);
    projectionsUploader.updateDatabase();

    // insert CBS eligibilities into temp table
    EligibilityUploader eligibilityUploader = new EligibilityUploader(connection, statsDate);
    eligibilityUploader.updateDatabase();

    // update mapping of CBS IDs to Player Strings.
    CbsIdScraper cbsIdScraper = new CbsIdScraper(connection, statsDate);
    cbsIdScraper.updateDatabase();

    // update player table based on new CBS IDs and changed Player Strings.
    PlayerStringSplitter playerStringSplitter = new PlayerStringSplitter(connection);
    playerStringSplitter.updateDatabase();

    // insert rows from temp tables into projection tables
    ConnectPlayerTable connectPlayerTable = new ConnectPlayerTable(connection);
    connectPlayerTable.updateDatabase();
  }
}
