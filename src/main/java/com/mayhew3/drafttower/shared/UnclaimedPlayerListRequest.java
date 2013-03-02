package com.mayhew3.drafttower.shared;

/**
 * Request object for data to populate an unclaimed player table.
 */
public interface UnclaimedPlayerListRequest {

  String getTeamToken();
  void setTeamToken(String teamToken);

  int getRowStart();
  void setRowStart(int rowStart);

  int getRowCount();
  void setRowCount(int rowCount);

  TableSpec getTableSpec();
  void setTableSpec(TableSpec tableSpec);

  Position getPositionFilter();
  void setPositionFilter(Position positionFilter);

  String getSearchQuery();
  void setSearchQuery(String searchQuery);

  boolean getHideInjuries();
  void setHideInjuries(boolean hideInjuries);
}