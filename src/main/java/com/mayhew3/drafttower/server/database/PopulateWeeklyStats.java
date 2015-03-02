package com.mayhew3.drafttower.server.database;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PopulateWeeklyStats extends DatabaseUtility {

  public static void main(String[] args) throws SQLException {
    PopulateWeeklyStats utility = new PopulateWeeklyStats();

    PreparedStatement batterStatement = DailyBatter.prepareWeeklyStatement(utility);
    PreparedStatement pitcherStatement = DailyPitcher.prepareWeeklyStatement(utility);

    batterStatement.executeUpdate();
    pitcherStatement.executeUpdate();
  }
}
