package com.mayhew3.drafttower.shared;

/**
 * Request object for data to populate an unclaimed player table.
 */
public interface UnclaimedPlayerListRequest {

  String getTeamToken();
  void setTeamToken(String teamToken);

  PlayerDataSet getProjectionSystem();
  void setProjectionSystem(PlayerDataSet playerDataSet);

  int getRowStart();
  void setRowStart(int rowStart);

  int getRowCount();
  void setRowCount(int rowCount);

  PlayerColumn getSortCol();
  void setSortCol(PlayerColumn sortCol);

  Position getPositionFilter();
  void setPositionFilter(Position positionFilter);

  String getSearchQuery();
  void setSearchQuery(String searchQuery);

  boolean getHideInjuries();
  void setHideInjuries(boolean hideInjuries);
}