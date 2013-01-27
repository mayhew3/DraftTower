package com.mayhew3.drafttower.shared;

/**
 * Player position.
 */
public enum Position {
  SP("SP", "Starting Pitcher"),
  RP("RP", "Relief Pitcher"),
  C("C", "Catcher"),
  FB("1B", "First Base"),
  SB("2B", "Second Base"),
  TB("3B", "Third Base"),
  SS("SS", "Shortstop"),
  OF("OF", "Outfield"),
  DH("DH", "Designated Hitter"),

  // Filtering:
  P("P", "All Pitchers"),
  UNF("Unfilled", "Unfilled Positions");

  private final String shortName;
  private final String longName;

  Position(String shortName, String longName) {
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
