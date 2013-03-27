package com.mayhew3.drafttower.shared;

/**
 * Request object sent when user chooses to use the Wizard for a specified table.
 */
public interface SetWizardTableRequest {

  String getTeamToken();
  void setTeamToken(String teamToken);

  PlayerDataSet getPlayerDataSet();
  void setPlayerDataSet(PlayerDataSet playerDataSet);
}