package com.mayhew3.drafttower.shared;

/**
 * Request object sent when user changes a player's rank.
 */
public interface SetAutoPickTableSpecRequest {

  String getTeamToken();
  void setTeamToken(String teamToken);

  TableSpec getTableSpec();
  void setTableSpec(TableSpec tableSpec);
}