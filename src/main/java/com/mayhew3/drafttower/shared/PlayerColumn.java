package com.mayhew3.drafttower.shared;

import com.google.common.collect.ImmutableMap;

/**
 * Player column values.
 */
public enum PlayerColumn {
  NAME("Name", "Name", "Player"),
  POS("Pos", "Position", "Position"),
  ELIG("Elig", "Eligible Positions", "Eligibility"),
  OBP("OBP", "On-Base Percentage", "OBP"),
  SLG("SLG", "Slugging Percentage", "SLG"),
  RHR("R-HR", "Runs - Home Runs", "RHR"),
  RBI("RBI", "Runs Batted In", "RBI"),
  HR("HR", "Home Runs", "HR"),
  SBCS("SB-CS", "Stolen Bases - Caught Stealing", "SBC"),
  INN("INN", "Innings Pitched", "INN"),
  ERA("ERA", "Earned Run Average", "ERA"),
  WHIP("WHIP", "Walks and Hits per Inning Pitched", "WHIP"),
  WL("W-L", "Wins - Losses", "WL"),
  K("K", "Strikeouts (Pitcher)", "K"),
  S("S", "Saves", "S"),
  RANK("Rank", "Rank", "Rank"),
  RATING("Rating", "Rating", "Rating");

  private final String shortName;
  private final String longName;
  private final String columnName;

  PlayerColumn(String shortName, String longName, String columnName) {
    this.shortName = shortName;
    this.longName = longName;
    this.columnName = columnName;
  }

  public String getShortName() {
    return shortName;
  }

  public String getLongName() {
    return longName;
  }

  public String getColumnName() {
    return columnName;
  }
}
