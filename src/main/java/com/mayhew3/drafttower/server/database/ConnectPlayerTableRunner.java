package com.mayhew3.drafttower.server.database;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

public class ConnectPlayerTableRunner {
  public static void main(String... args) throws URISyntaxException, SQLException, IOException {
    SQLConnection connection = new MySQLConnectionFactory().createConnection();
    ConnectPlayerTable connectPlayerTable = new ConnectPlayerTable(connection);
    connectPlayerTable.updateDatabase();
  }
}
