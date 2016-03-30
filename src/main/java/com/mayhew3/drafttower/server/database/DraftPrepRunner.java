package com.mayhew3.drafttower.server.database;

import org.joda.time.LocalDate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Date;
import java.sql.SQLException;

public class DraftPrepRunner {
  static LocalDate statsDate = new LocalDate(2016, 3, 24);

  public static void main(String... args) throws IOException, SQLException, URISyntaxException {
    SQLConnection connection = new MySQLConnectionFactory().createConnection();

    // insert CBS projections into temp tables
    ProjectionsUploader projectionsUploader = new ProjectionsUploader(connection, statsDate);
    projectionsUploader.updateDatabase();

    // insert CBS eligibilities into temp table
    EligibilityUploader eligibilityUploader = new EligibilityUploader(connection, statsDate);
    eligibilityUploader.updateDatabase();

    // insert CBS draft averages into temp table
    AvgDraftPosUploader avgDraftPosUploader = new AvgDraftPosUploader(connection, statsDate);
    avgDraftPosUploader.updateDatabase();

    // insert CBS expert rankings into temp table
    Top300Uploader top300Uploader = new Top300Uploader(connection, statsDate);
    top300Uploader.updateDatabase();

    // update mapping of CBS IDs to Player Strings.
    CbsIdScraper cbsIdScraper = new CbsIdScraper(connection, statsDate);
    cbsIdScraper.updateDatabase();

    // update player table based on new CBS IDs and changed Player Strings.
    PlayerStringSplitter playerStringSplitter = new PlayerStringSplitter(connection);
    playerStringSplitter.updateDatabase();

    // insert rows from temp tables into projection tables
    ConnectPlayerTable connectPlayerTable = new ConnectPlayerTable(connection);
    connectPlayerTable.updateDatabase();

    // add custom rankings for each team based on averages
    InitCustomRankings initCustomRankings = new InitCustomRankings(connection);
    initCustomRankings.updateDatabase();

    // Copy ranks from Draft Averages into projection tables
    PopulateDraftAverages populateDraftAverages = new PopulateDraftAverages(connection, new Date(statsDate.toDate().getTime()));
    populateDraftAverages.updateDatabase();

    // Clear and populate the Eligibilities table
    PopulateEligibilities populateEligibilities = new PopulateEligibilities(connection);
    populateEligibilities.updateDatabase();

    // Update projectionsbatting and projectionspitching Rank column based on average rank in temp table
    PopulateExpertRankings populateExpertRankings = new PopulateExpertRankings(connection, new Date(statsDate.toDate().getTime()));
    populateExpertRankings.updateDatabase();

    // Update player injury column
    InjuryUpdater injuryUpdater = new InjuryUpdater(connection);
    injuryUpdater.updateDatabase();

    // Get rid of DH noise in the Player eligibility strings
    TrimEligibilities trimEligibilities = new TrimEligibilities(connection);
    trimEligibilities.updateDatabase();

    // Update keepers
    PopulateKeepers populateKeepers = new PopulateKeepers(connection);
    populateKeepers.updateDatabase();


    DraftResultsClearer draftResultsClearer = new DraftResultsClearer(connection);
    draftResultsClearer.updateDatabase();
  }
}
