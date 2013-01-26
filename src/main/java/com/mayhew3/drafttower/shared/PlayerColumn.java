package com.mayhew3.drafttower.shared;

/**
 * Player column values.
 */
public enum PlayerColumn {
  NAME("Name", "Name"),
  POS("Pos", "Position"),
  OBP("OBP", "On-Base Percentage"),
  SLG("SLG", "Slugging Percentage"),
  RHR("R-HR", "Runs - Home Runs"),
  RBI("RBI", "Runs Batted In"),
  HR("HR", "Home Runs"),
  SBCS("SB-CS", "Stolen Bases - Caught Stealing"),
  INN("INN", "Innings Pitched"),
  ERA("ERA", "Earned Run Average"),
  WHIP("WHIP", "Walks and Hits per Inning Pitched"),
  WL("W-L", "Wins - Losses"),
  K("K", "Strikeouts (Pitcher)"),
  S("S", "Saves");

  private final String shortName;
  private final String longName;

  PlayerColumn(String shortName, String longName) {
    this.shortName = shortName;
    this.longName = longName;
  }

  public String getShortName() {
    return shortName;
  }

  public String getLongName() {
    return longName;
  }
}
