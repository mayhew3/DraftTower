package com.mayhew3.drafttower.server.database;

import java.net.URISyntaxException;
import java.sql.SQLException;

public class PlayerStringSplitterRunner {
  public static void main(String... args) throws URISyntaxException, SQLException {
    PlayerStringSplitter playerStringSplitter = new PlayerStringSplitter(new MySQLConnectionFactory().createConnection());
    playerStringSplitter.updateDatabase();
  }
}
