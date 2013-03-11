package com.mayhew3.drafttower.shared;

/**
 * Player position.
 */
public enum Position {
  P("P", "Pitcher"),
  C("C", "Catcher"),
  FB("1B", "First Base"),
  SB("2B", "Second Base"),
  TB("3B", "Third Base"),
  SS("SS", "Shortstop"),
  OF("OF", "Outfield"),
  DH("DH", "Designated Hitter"),

  // Filtering:
  UNF("Unfilled", "Unfilled Positions"),

  // Misc:
  RS("RS", "Reserves");

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

  public static Position fromShortName(String shortName) {
    for (Position position : values()) {
      if (position.getShortName().equals(shortName)) {
        return position;
      }
    }
    throw new IllegalArgumentException("No position " + shortName);
  }
}
