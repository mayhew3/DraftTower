package com.mayhew3.drafttower.shared;

/**
 * Request object for data to populate an unclaimed player table.
 */
public interface UnclaimedPlayerListRequest {

  String getTeamToken();
  void setTeamToken(String teamToken);

  TableSpec getTableSpec();
  void setTableSpec(TableSpec tableSpec);
}