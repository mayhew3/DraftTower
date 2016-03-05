package com.mayhew3.drafttower.server.database;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.joda.time.DateMidnight;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class DailyPitcher extends DailyPlayer {

  static PreparedStatement insertStatement;

  static String[] fieldNames = {
      "1BA",
      "2BA",
      "3BA",
      "ABA",
      "APP",
      "B",
      "BBI",
      "BSL",
      "CG",
      "CGL",
      "ER",
      "GF",
      "GO",
      "GS",
      "HA",
      "AO",
      "HB",
      "HD",
      "HRA",
      "IBBI",
      "INNdGS",
      "IRS",
      "K",
      "L",
      "NH",
      "NHL",
      "OUTS",
      "PA",
      "PC",
      "PG",
      "PKO",
      "QS",
      "RA",
      "RL",
      "RW",
      "S",
      "SO",
      "SOP",
      "W",
      "WP"
  };

  public DailyPitcher(DateMidnight statDate) {
    super(statDate);
  }


  public static void prepareStatement(DatabaseConnection utility) {

    List<String> questionMarks = new ArrayList<>();

    for (String ignored : nonStatNames) {
      questionMarks.add("?");
    }

    for (String ignored : fieldNames) {
      questionMarks.add("?");
    }

    Joiner joiner = Joiner.on(",");

    insertStatement = utility.getPreparedStatement("INSERT INTO daily_pitching " +
        "(" + joiner.join(nonStatNames) + "," + joiner.join(fieldNames) + ") " +
        "VALUES (" + joiner.join(questionMarks) + ")");
  }


  public static PreparedStatement prepareWeeklyStatement(DatabaseUtility utility) {
    List<String> selectFields = new ArrayList<>();

    List<String> weeklyNonStats = Lists.newArrayList(
        "Player",
        "PeriodNumber",
        "Year"
    );

    for (String fieldName : weeklyNonStats) {
      selectFields.add(fieldName);
    }

    for (String fieldName : fieldNames) {
      selectFields.add("SUM(" + fieldName + ")");
    }

    Joiner joiner = Joiner.on(",");

    String sql = "INSERT INTO weekly_pitching (" +
        joiner.join(weeklyNonStats) + "," + joiner.join(fieldNames) + ") " +
        "SELECT " + joiner.join(selectFields) + " " +
        "FROM daily_pitching " +
        "WHERE PeriodNumber IS NOT NULL " +
        "GROUP BY Player, PeriodNumber, Year";

    return utility.getPreparedStatement(sql);
  }

  @Override
  protected List<String> getSpecificFieldNames() {
    return Lists.newArrayList(fieldNames);
  }

  @Override
  protected PreparedStatement getPreparedStatement() {
    return insertStatement;
  }

}
