package com.mayhew3.drafttower.shared;

/**
 * User settings for table display.
 */
public interface TableSpec {
  PlayerDataSet getPlayerDataSet();
  void setPlayerDataSet(PlayerDataSet playerDataSet);

  PlayerColumn getSortCol();
  void setSortCol(PlayerColumn sortCol);

  boolean isAscending();
  void setAscending(boolean ascending);
}