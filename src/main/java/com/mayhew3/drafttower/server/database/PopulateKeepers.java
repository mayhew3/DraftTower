package com.mayhew3.drafttower.server.database;

import java.sql.ResultSet;

public class PopulateKeepers extends DatabaseUtility {

  @SuppressWarnings("SpellCheckingInspection")
  public static void main(String[] args) {
    initConnection();

    System.out.println("Adding keepers.");

    removeExistingKeepers();

    addKeeper("gus", "Goldschmidt, Paul");
    addKeeper("kevin", "Cano, Robinson");
    addKeeper("kevin", "Fielder, Prince");
    addKeeper("kevin", "Wright, David");
    addKeeper("mayhew", "Gonzalez, Carlos");
    addKeeper("hunter", "Lee, Cliff");
    addKeeper("hunter", "Jones, Adam");
    addKeeper("mayhews", "McCutchen, Andrew");
    addKeeper("mayhews", "Ramirez, Hanley");
    addKeeper("laura", "Trout, Mike");
    addKeeper("alcides", "Cabrera, Miguel");
    addKeeper("lakshmi", "Kershaw, Clayton");
    addKeeper("lakshmi", "Braun, Ryan");
    addKeeper("lakshmi", "Davis, Chris");
  }


  private static void addKeeper(String ownerName, String playerString) {
    String[] split = playerString.split(", ");
    String lastName = split[0];
    String firstName = split[1];

    String sql = "INSERT INTO Keepers (TeamID, PlayerID) " +
        "VALUES (" + getTeamID(ownerName) + ", " + getPlayerIDFromFirstAndLastName(firstName, lastName) + ")";

    executeUpdate(sql);
  }

  private static int getTeamID(String ownerName) {
    ResultSet resultSet = executeQuery("SELECT ID FROM teams WHERE UserID = '" + ownerName + "'");

    if (hasMoreElements(resultSet)) {
      return getInt(resultSet, "ID");
    } else {
      throw new IllegalStateException("Couldn't find team with UserID " + ownerName);
    }
  }

  private static int getPlayerIDFromFirstAndLastName(String firstName, String lastName) {
    ResultSet resultSet = prepareAndExecuteStatementFetch("SELECT ID FROM Players WHERE FirstName = ? AND LastName = ?", firstName, lastName);
    if (!hasMoreElements(resultSet)) {
      throw new RuntimeException("Couldn't find player " + firstName + " " + lastName);
    }
    int playerID = getInt(resultSet, "ID");
    if (hasMoreElements(resultSet)) {
      throw new RuntimeException("Found multiple matches for player " + firstName + " " + lastName);
    }
    return playerID;
  }

  private static void removeExistingKeepers() {
    executeUpdate("DELETE FROM Keepers");
  }
}