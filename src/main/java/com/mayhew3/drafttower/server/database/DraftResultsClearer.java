package com.mayhew3.drafttower.server.database;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DraftResultsClearer {

  private Logger logger = Logger.getLogger(DraftResultsClearer.class.getName());

  private SQLConnection connection;

  public static void main(String... args) throws URISyntaxException, SQLException, IOException {
    SQLConnection connection = new MySQLConnectionFactory().createConnection();
    DraftResultsClearer draftResultsClearer = new DraftResultsClearer(connection);
    draftResultsClearer.updateDatabase();
  }

  public DraftResultsClearer(SQLConnection connection) {
    this.connection = connection;
  }

  public void updateDatabase() throws SQLException {
    logger.info("Clearing Draft Results.");

    connection.prepareAndExecuteStatementUpdate("truncate table draftresults");

  }

}
