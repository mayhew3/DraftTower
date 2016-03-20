package com.mayhew3.drafttower.server.database;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TrimEligibilities {
  private SQLConnection connection;
  private Logger logger = Logger.getLogger(TrimEligibilities.class.getName());

  public TrimEligibilities(SQLConnection connection) {
    this.connection = connection;
  }

  public static void main(String... arge) throws URISyntaxException, SQLException {
    TrimEligibilities trimEligibilities = new TrimEligibilities(new MySQLConnectionFactory().createConnection());
    trimEligibilities.updateDatabase();
  }

  public void updateDatabase() throws SQLException {

    String sql = "SELECT * \n" +
        "FROM players \n" +
        "WHERE Eligibility NOT IN ('P', 'DH'); " ;
    ResultSet resultSet = connection.prepareAndExecuteStatementFetch(sql);

    PreparedStatement preparedStatement = connection.prepareStatementNoParams("UPDATE players SET Eligibility = ? WHERE ID = ?");

    while (resultSet.next()) {
      int playerID = resultSet.getInt("ID");
      String eligibility = resultSet.getString("Eligibility");
      String playerString = resultSet.getString("PlayerString");

      List<String> allEm = Lists.newArrayList(eligibility.split(","));
      boolean replaced = allEm.remove("DH");

      if (replaced) {
        String shorterEligibilityString = Joiner.on(",").join(allEm);
        logger.log(Level.INFO, "Updating player " + playerString + " (" + playerID + ") with eligibility " + shorterEligibilityString);
        connection.executePreparedUpdateWithParams(preparedStatement, shorterEligibilityString, playerID);
      }
    }
  }
}
