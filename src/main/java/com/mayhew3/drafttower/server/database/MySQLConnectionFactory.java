package com.mayhew3.drafttower.server.database;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MySQLConnectionFactory extends ConnectionFactory {

  protected final Logger logger = Logger.getLogger(DatabaseUtility.class.getName());

  @Override
  public SQLConnection createConnection() throws URISyntaxException, SQLException {
    return new MySQLConnection(initiateDBConnect());
  }

  private Connection initiateDBConnect() throws URISyntaxException, SQLException {
    logger.log(Level.INFO, "Initializing connection.");

    try {
      Class.forName("com.mysql.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      System.out.println("Cannot find MySQL drivers. Exiting.");
      throw new RuntimeException(e.getLocalizedMessage());
    }

    try {
      String dbhost = System.getenv("dbhost");
      String dbuser = System.getenv("dbuser");
      String dbpassword = System.getenv("dbpassword");
      return DriverManager.getConnection(dbhost, dbuser, dbpassword);
    } catch (SQLException e) {
      System.out.println("Cannot connect to database. Exiting.");
      throw new RuntimeException(e.getLocalizedMessage());
    }
  }
}
