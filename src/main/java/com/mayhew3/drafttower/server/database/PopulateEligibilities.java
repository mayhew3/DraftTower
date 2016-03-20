package com.mayhew3.drafttower.server.database;

import com.google.common.collect.Lists;
import org.joda.time.LocalDate;

import java.net.URISyntaxException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PopulateEligibilities {
  private SQLConnection connection;

  public PopulateEligibilities(SQLConnection connection) {
    this.connection = connection;
  }

  public static void main(String... arge) throws URISyntaxException, SQLException {
    PopulateEligibilities populateEligibilities = new PopulateEligibilities(new MySQLConnectionFactory().createConnection());
    populateEligibilities.updateDatabase();
  }

  public void updateDatabase() throws SQLException {
    validateEligibilitiesExist();

    emptyEligibilityTable();
    insertIntoEligibilitiesTable();

    verifyNoMissingRows();
  }

  private void validateEligibilitiesExist() throws SQLException {
    validateEligibilitiesExist("projectionsbatting");
    validateEligibilitiesExist("projectionspitching");
  }

  private void validateEligibilitiesExist(final String tableName) throws SQLException {
    ResultSet resultSet = connection.prepareAndExecuteStatementFetch("select *\n" +
        "from players p\n" +
        "inner join " + tableName + " pb\n" +
        " on pb.playerid = p.id\n" +
        "where p.eligibility is null");

    if (resultSet.next()) {
      throw new IllegalStateException("Eligibility column is null for some rows in Players table that are referenced by " + tableName + ".");
    }
  }

  private void emptyEligibilityTable() throws SQLException {
    connection.prepareAndExecuteStatementUpdate("truncate table eligibilities");
  }

  private void insertIntoEligibilitiesTable() throws SQLException {
    List<String> possiblePositions = Lists.newArrayList("C", "1B", "2B", "3B", "SS", "OF", "DH", "P");

    PreparedStatement preparedStatement = connection.prepareStatementNoParams("INSERT INTO eligibilities (PlayerID, Position) VALUES (?, ?)");

    String sql = "SELECT * \n" +
        "FROM players \n" +
        "WHERE Eligibility IS NOT NULL " +
        "AND NOT EXISTS (SELECT 1 \n" +
        "        FROM eligibilities \n" +
        "        WHERE PlayerID = players.ID);";
    ResultSet resultSet = connection.prepareAndExecuteStatementFetch(sql);

    while (resultSet.next()) {
      int playerID = resultSet.getInt("ID");
      String eligibility = resultSet.getString("Eligibility");

      String[] allEm = eligibility.split(",");
      for (String position : allEm) {
        if (!possiblePositions.contains(position)) {
          throw new RuntimeException("Unknown position found: " + position);
        }
      }

      for (String position : allEm) {
        System.out.println("Adding '" + position + "' position for player " + playerID);
        connection.executePreparedUpdateWithParams(preparedStatement, playerID, position);
      }
    }

    preparedStatement.close();
  }

  private void verifyNoMissingRows() throws SQLException {
    verifyNoMissingRows("projectionsbatting");
    verifyNoMissingRows("projectionspitching");
  }

  private void verifyNoMissingRows(String tableName) throws SQLException {
    ResultSet resultSet = connection.prepareAndExecuteStatementFetch(
        "select *\n" +
        "from " + tableName + "\n" +
        "where playerid not in (select playerid from eligibilities)");

    if (resultSet.next()) {
      throw new IllegalStateException("Row in " + tableName + " found with no corresponding row in eligibilities table.");
    }
  }
}
