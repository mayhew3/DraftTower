package com.mayhew3.drafttower.shared;

/**
 * Request object sent when user copies ordering to custom rankings.
 */
public interface CopyAllPlayerRanksRequest {

  String getTeamToken();
  void setTeamToken(String teamToken);

  TableSpec getTableSpec();
  void setTableSpec(TableSpec tableSpec);
}