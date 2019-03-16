package com.mayhew3.drafttower.server.database;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PopulateKeepers {

  private SQLConnection connection;

  public PopulateKeepers(SQLConnection connection) {
    this.connection = connection;
  }

  public static void main(String... args) throws URISyntaxException, SQLException, IOException {
    SQLConnection connection = new MySQLConnectionFactory().createConnection();
    PopulateKeepers populateKeepers = new PopulateKeepers(connection);
    populateKeepers.updateDatabase();
  }

  @SuppressWarnings("SpellCheckingInspection")
  public void updateDatabase() throws SQLException {
    System.out.println("Adding keepers.");

    removeExistingKeepers();

    addKeeper("laura", "Betts", "Mookie");
    addKeeper("laura", "Verlander", "Justin");
    addKeeper("laura", "Goldschmidt", "Paul");

    addKeeper("mayhews", "Kluber", "Corey");
    addKeeper("mayhews", "Judge", "Aaron");

    addKeeper("kevin", 17298); // Jose Ramirez (entered by ID because there are two.)

    addKeeper("lakshmi", "Harper", "Bryce");
    addKeeper("lakshmi", "Bregman", "Alex");

    addKeeper("hunter", "Martinez", "J.D.");

    addKeeper("gus", "DeGrom", "Jacob");

    addKeeper("scott", "Sale", "Chris");
    addKeeper("scott", "Scherzer", "Max");
    addKeeper("scott", "Yelich", "Christian");

  }


  private void addKeeper(String ownerName, String lastName, String firstName) throws SQLException {
    addKeeper(ownerName, getPlayerIDFromFirstAndLastName(firstName, lastName));
  }

  private void addKeeper(String ownerName, Integer playerID) throws SQLException {
    connection.prepareAndExecuteStatementUpdate("INSERT INTO Keepers (TeamID, PlayerID) VALUES (?, ?)",
        getTeamID(ownerName), playerID);
  }

  private Integer getTeamID(String ownerName) throws SQLException {
    ResultSet resultSet = connection.prepareAndExecuteStatementFetch("SELECT ID FROM teams WHERE UserID = ?", ownerName);
    resultSet.next();

    return resultSet.getInt("ID");
  }

  private int getPlayerIDFromFirstAndLastName(String firstName, String lastName) throws SQLException {
    ResultSet resultSet = connection.prepareAndExecuteStatementFetch("SELECT ID FROM players WHERE FirstName = ? AND LastName = ?", firstName, lastName);
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
