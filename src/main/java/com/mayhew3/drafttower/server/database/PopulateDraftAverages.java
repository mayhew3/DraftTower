package com.mayhew3.drafttower.server.database;

import org.joda.time.LocalDate;

import java.net.URISyntaxException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PopulateDraftAverages {
  private SQLConnection connection;
  private Date statDate;

  public PopulateDraftAverages(SQLConnection connection, Date statDate) {
    this.connection = connection;
    this.statDate = statDate;
  }

  public static void main(String... args) throws URISyntaxException, SQLException {
    LocalDate localDate = new LocalDate(2016, 3, 6);
    Date sqlDate = new Date(localDate.toDate().getTime());

    PopulateDraftAverages populateDraftAverages = new PopulateDraftAverages(new MySQLConnectionFactory().createConnection(), sqlDate);
    populateDraftAverages.updateDatabase();
  }

  public void updateDatabase() throws SQLException {
    updatePlayerIDs();

    updateBatters();
    updatePitchers();
  }

  private void updatePlayerIDs() throws SQLException {
    connection.prepareAndExecuteStatementUpdate("update cbs_draftaverages da\n" +
        "set da.playerid = (select p.id from players p where p.playerstring = da.player) " +
        "where da.playerid is null and da.StatDate = ?", statDate);

    ResultSet resultSet = connection.prepareAndExecuteStatementFetch("select * from cbs_draftaverages where playerid is null and StatDate = ?", statDate);
    if (resultSet.next()) {
      throw new IllegalStateException("Players in cbs_draftaverages without matching Players.PlayerString");
    }
  }

  private void updateBatters() throws SQLException {
    connection.prepareAndExecuteStatementUpdate(
        "update projectionsbatting pb\n" +
        "inner join cbs_draftaverages cd\n" +
        " on pb.PlayerID = cd.PlayerID\n" +
        "set pb.Draft = cd.Rank " +
        "where cd.StatDate = ?", statDate);

  }

  private void updatePitchers() throws SQLException {
    connection.prepareAndExecuteStatementUpdate(
        "update projectionspitching pp\n" +
        "inner join cbs_draftaverages cd\n" +
        " on pp.PlayerID = cd.PlayerID\n" +
        "set pp.Draft = cd.Rank " +
        "where cd.StatDate = ?", statDate);
  }
}
