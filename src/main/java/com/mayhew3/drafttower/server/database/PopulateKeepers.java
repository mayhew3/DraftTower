package com.mayhew3.drafttower.server.database;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PopulateKeepers {

  private SQLConnection connection;

  public PopulateKeepers(SQLConnection connection) {
    this.connection = connection;
  }

  @SuppressWarnings("SpellCheckingInspection")
  public void updateDatabase() throws SQLException {
    System.out.println("Adding keepers.");

    removeExistingKeepers();

    addKeeper("gus", "Harper", "Bryce");
    addKeeper("gus", "Rizzo", "Anthony");
    addKeeper("gus", "Betts", "Mookie");
  }


  private void addKeeper(String ownerName, String lastName, String firstName) throws SQLException {
    connection.prepareAndExecuteStatementUpdate("INSERT INTO Keepers (TeamID, PlayerID) VALUES (?, ?)",
        getTeamID(ownerName), getPlayerIDFromFirstAndLastName(firstName, lastName));
  }

  private Integer getTeamID(String ownerName) throws SQLException {
    ResultSet resultSet = connection.prepareAndExecuteStatementFetch("SELECT ID FROM teams WHERE UserID = ?", ownerName);
    resultSet.next();

    return resultSet.getInt("ID");
  }

  private int getPlayerIDFromFirstAndLastName(String firstName, String lastName) throws SQLException {
    ResultSet resultSet = connection.prepareAndExecuteStatementFetch("SELECT ID FROM Players WHERE FirstName = ? AND LastName = ?", firstName, lastName);
    if (resultSet.next()) {
      int playerID = resultSet.getInt("ID");
      if (resultSet.next()) {
        throw new RuntimeException("Found multiple matches for player " + firstName + " " + lastName);
      }
      return playerID;
    } else {
      throw new RuntimeException("Couldn't find player " + firstName + " " + lastName);
    }
  }

  private void removeExistingKeepers() throws SQLException {
    connection.prepareAndExecuteStatementUpdate("DELETE FROM Keepers");
  }
}
