package com.mayhew3.drafttower.server.database;

import com.google.common.collect.Lists;

import java.sql.ResultSet;
import java.util.List;

public class PopulateEligibilities extends DatabaseUtility {

  public static void main(String[] arge) {
    initConnection();

    List<String> possiblePositions = Lists.newArrayList("C", "1B", "2B", "3B", "SS", "OF", "DH", "P");

    String sql = "SELECT * \n" +
        "FROM Players \n" +
        "WHERE Eligibility IS NOT NULL " +
        "AND NOT EXISTS (SELECT 1 \n" +
        "        FROM Eligibilities \n" +
        "        WHERE PlayerID = Players.ID);";
    ResultSet resultSet = prepareAndExecuteStatementFetch(sql);

    while (hasMoreElements(resultSet)) {
      int playerID = getInt(resultSet, "ID");
      String eligibility = getString(resultSet, "Eligibility");

      String[] allEm = eligibility.split(",");
      for (String position : allEm) {
        if (!possiblePositions.contains(position)) {
          throw new RuntimeException("Unknown position found: " + position);
        }
      }

      for (String position : allEm) {
        System.out.println("Adding '" + position + "' position for player " + playerID);
        prepareAndExecuteStatementUpdate("INSERT INTO Eligibilities (PlayerID, Position) VALUES (?, ?)", playerID, position);
      }
    }
  }
}
