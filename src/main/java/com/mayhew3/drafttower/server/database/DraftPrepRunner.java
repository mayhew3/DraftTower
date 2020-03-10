package com.mayhew3.drafttower.server.database;

import com.mayhew3.drafttower.server.database.dataobject.DraftPrepStatus;
import org.joda.time.LocalDate;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class DraftPrepRunner {
  static LocalDate statsDate = new LocalDate(2020, 3, 9);

  protected static final Logger logger = Logger.getLogger(DraftPrepRunner.class.getName());

  public static void main(String... args) throws IOException, SQLException, URISyntaxException {
    SQLConnection connection = new MySQLConnectionFactory().createConnection();

    List<DraftDataStep> steps = new ArrayList<>();

    // insert CBS projections into temp tables
    steps.add(new ProjectionsUploader(connection, statsDate));

    // insert CBS eligibilities into temp table
    steps.add(new EligibilityUploader(connection, statsDate));

    // insert CBS draft averages into temp table
    steps.add(new AvgDraftPosUploader(connection, statsDate));

    // insert CBS expert rankings into temp table
    steps.add(new Top300Uploader(connection, statsDate));

    // update mapping of CBS IDs to Player Strings.
    steps.add(new CbsIdScraper(connection, statsDate));

    // update player table based on new CBS IDs and changed Player Strings.
    steps.add(new PlayerStringSplitter(connection, statsDate));

    // insert rows from temp tables into projection tables
    steps.add(new ConnectPlayerTable(connection));

    // Update keepers
    steps.add(new PopulateKeepers(connection));

    // add custom rankings for each team based on averages
    steps.add(new InitCustomRankings(connection));

    // Copy ranks from Draft Averages into projection tables
    steps.add(new PopulateDraftAverages(connection, statsDate));

    // Clear and populate the Eligibilities table
    steps.add(new PopulateEligibilities(connection));

    // Update projectionsbatting and projectionspitching Rank column based on average rank in temp table
    steps.add(new PopulateExpertRankings(connection, statsDate));

    // Update player injury column
    steps.add(new InjuryUpdater(connection));

    // Get rid of DH noise in the Player eligibility strings
    steps.add(new TrimEligibilities(connection));

    // Clear the draft results from last season
    steps.add(new DraftResultsClearer(connection));

    DraftPrepStatus status = getStatus(connection);
    String lastCompleted = status.lastCompletedStep.getValue();
    boolean skip = (lastCompleted != null);

    if (skip) {
      logger.info("Draft Prep stopped after " + lastCompleted + "... skipping steps through that one.");
    }

    for (DraftDataStep step : steps) {
      String stepName = step.getStepName();
      if (!skip) {
        step.updateDatabase();
        status.lastCompletedStep.changeValue(stepName);
        status.commit(connection);
      } else {
        logger.info("Skipping step: '" + stepName + "'");
        skip = !stepName.equals(lastCompleted);
      }
    }

    status.lastCompletedStep.changeValue(null);
    status.commit(connection);
  }

  private static DraftPrepStatus getStatus(SQLConnection connection) throws SQLException {
    DraftPrepStatus draftPrepStatus = new DraftPrepStatus();
    String sql = "SELECT * FROM " + draftPrepStatus.getTableName();
    ResultSet resultSet = connection.prepareAndExecuteStatementFetch(sql);
    resultSet.next();
    draftPrepStatus.initializeFromDBObject(resultSet);
    return draftPrepStatus;
  }
}
