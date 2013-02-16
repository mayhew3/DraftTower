package com.mayhew3.drafttower.shared;

/**
 * Request object for data to populate an unclaimed player table.
 */
public interface UnclaimedPlayerListRequest {

  String getTeamToken();
  void setTeamToken(String teamToken);

  ProjectionSystem getProjectionSystem();
  void setProjectionSystem(ProjectionSystem projectionSystem);

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
}