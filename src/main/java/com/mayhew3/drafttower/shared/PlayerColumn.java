package com.mayhew3.drafttower.shared;

import java.util.Comparator;

/**
 * Player column values.
 */
public enum PlayerColumn {
  NAME("Name", "Name", "Player", false, true),
  MLB("Tm", "MLB Team", "MLBTeam", false, true),
  ELIG("Elig", "Eligible Positions", "Eligibility", false, true),
  G("G", "Games Played", "G", true, false),
  AB("AB", "At Bats", "AB", true, false),
  OBP("OBP", "On-Base Percentage", "OBP", true, false),
  SLG("SLG", "Slugging Percentage", "SLG", true, false),
  RHR("R-", "Runs - Home Runs", "RHR", true, false),
  RBI("RBI", "Runs Batted In", "RBI", true, false),
  HR("HR", "Home Runs", "HR", true, false),
  SBCS("SB-", "Stolen Bases - Caught Stealing", "SBC", true, false),
  INN("INN", "Innings Pitched", "INN", true, false),
  ERA("ERA", "Earned Run Average", "ERA", true, true),
  WHIP("WHIP", "Walks and Hits per Inning Pitched", "WHIP", true, true),
  WL("W-", "Wins - Losses", "WL", true, false),
  K("K", "Strikeouts (Pitcher)", "K", true, false),
  S("S", "Saves", "S", true, false),
  RANK("Rank", "Rank", "Rank", true, true),
  DRAFT("Draft", "Average Position in CBS Drafts", "Draft", true, true),
  WIZARD("Wizard", "Wizard", "Wizard", true, false),
  MYRANK("MyRank", "MyRank", "MyRank", true, true);

  private final String shortName;
  private final String longName;
  private final String columnName;
  private final boolean sortAsNumber;
  private final boolean defaultSortAscending;
  private final int nullValue;

  PlayerColumn(String shortName, String longName, String columnName, boolean sortAsNumber, boolean defaultSortAscending) {
    this.shortName = shortName;
    this.longName = longName;
    this.columnName = columnName;
    this.sortAsNumber = sortAsNumber;
    this.defaultSortAscending = defaultSortAscending;
    nullValue = defaultSortAscending ? Integer.MAX_VALUE : Integer.MIN_VALUE;
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

  public Comparator<Player> getComparator(final boolean ascending) {
    return new Comparator<Player>() {
      @Override
      public int compare(Player p1, Player p2) {
        int rtn;
        String p1Value = p1.getColumnValues().get(PlayerColumn.this);
        String p2Value = p2.getColumnValues().get(PlayerColumn.this);
        if (sortAsNumber) {
          rtn = Float.compare(p1Value == null ? nullValue : Float.parseFloat(p1Value),
              p2Value == null ? nullValue : Float.parseFloat(p2Value));
        } else {
          rtn = p1Value.compareTo(p2Value);
        }
        return ascending ? rtn : -rtn;
      }
    };
  }

  public boolean isDefaultSortAscending() {
    return defaultSortAscending;
  }
}
