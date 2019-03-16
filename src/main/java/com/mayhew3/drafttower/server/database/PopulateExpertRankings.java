package com.mayhew3.drafttower.server.database;

import com.sun.istack.internal.NotNull;
import org.joda.time.LocalDate;

import java.net.URISyntaxException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PopulateExpertRankings {
  private SQLConnection connection;
  private Date statDate;

  private Integer numberOfExperts = 2;

  private Logger logger = Logger.getLogger(PopulateExpertRankings.class.getName());

  public PopulateExpertRankings(SQLConnection connection, Date statDate) {
    this.connection = connection;
    this.statDate = statDate;
  }

  public static void main(String... args) throws URISyntaxException, SQLException {
    LocalDate localDate = DraftPrepRunner.statsDate;
    Date sqlDate = new Date(localDate.toDate().getTime());

    PopulateExpertRankings populateDraftAverages = new PopulateExpertRankings(new MySQLConnectionFactory().createConnection(), sqlDate);
    populateDraftAverages.updateDatabase();
  }

  public void updateDatabase() throws SQLException {
    ResultSet resultSet =  connection.prepareAndExecuteStatementFetch(
        "SELECT PlayerID, AVG(rank) as AvgRank, COUNT(1) as Rankings " +
            "FROM tmp_top300 " +
            "WHERE statdate = ? " +
            "GROUP BY PlayerID", statDate);

    PreparedStatement battingStatement = connection.prepareStatementNoParams("UPDATE projectionsbatting SET Rank = ? WHERE PlayerID = ?");
    PreparedStatement pitchingStatement = connection.prepareStatementNoParams("UPDATE projectionspitching SET Rank = ? WHERE PlayerID = ?");

    while (resultSet.next()) {
      int playerID = resultSet.getInt("PlayerID");
      int avgRank = resultSet.getInt("AvgRank");
      int rankings = resultSet.getInt("Rankings");

      Integer adjustedRank = getAdjustedRank(rankings, avgRank);

      logger.log(Level.INFO, "Updating player ID " + playerID + " to Rank " + adjustedRank);

      connection.executePreparedUpdateWithParams(battingStatement, adjustedRank, playerID);
      connection.executePreparedUpdateWithParams(pitchingStatement, adjustedRank, playerID);
    }
  }

  private Integer getAdjustedRank(Integer rankings, @NotNull Integer averageRank) {

    Integer unrankedExperts = numberOfExperts - rankings;

    if (unrankedExperts > 0) {
      Integer maximumScores = unrankedExperts * 300;
      Integer rankingsScores = averageRank * rankings;
      return (rankingsScores + maximumScores) / numberOfExperts;
    } else {
      return averageRank;
    }
  }

}
