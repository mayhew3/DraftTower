package com.mayhew3.drafttower.server.database;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class DailyBatter extends DailyPlayer {

  static PreparedStatement insertStatement;

  static String[] fieldNames = {
      "1B",
      "2B",
      "3B",
      "AB",
      "ABRISP",
      "AST",
      "BB",
      "CI",
      "CS",
      "CSC",
      "CYC",
      "DPT",
      "E",
      "FC",
      "G",
      "GDP",
      "GP1B",
      "GP2B",
      "GP3B",
      "GPC",
      "GPDH",
      "GPLF",
      "GPCF",
      "GPRF",
      "GPSS",
      "GSHR",
      "GST",
      "H",
      "HP",
      "HR",
      "HRISP",
      "IB",
      "KO",
      "LOB",
      "OFAST",
      "PBC",
      "PKOF",
      "PO",
      "PPos",
      "R",
      "RBI",
      "SB",
      "SBA",
      "SF",
      "SH",
      "TPT"
  };

  public DailyBatter(DateTime statDate) {
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

    insertStatement = utility.getPreparedStatement("INSERT INTO daily_batting " +
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

    String sql = "INSERT INTO weekly_batting (" +
      joiner.join(weeklyNonStats) + "," + joiner.join(fieldNames) + ") " +
        "SELECT " + joiner.join(selectFields) + " " +
        "FROM daily_batting " +
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
