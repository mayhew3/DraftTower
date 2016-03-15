package com.mayhew3.drafttower.server.database;

import java.net.URISyntaxException;
import java.sql.SQLException;

public class ExistingPlayerUpdaterRunner {
  public static void main(String... args) throws URISyntaxException, SQLException {
    ExistingPlayerUpdater existingPlayerUpdater = new ExistingPlayerUpdater(new MySQLConnectionFactory().createConnection());
    existingPlayerUpdater.updateDatabase();
  }
}
