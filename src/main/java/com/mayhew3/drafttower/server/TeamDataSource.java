package com.mayhew3.drafttower.server;

import com.mayhew3.drafttower.shared.PlayerDataSet;
import com.mayhew3.drafttower.shared.Team;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles lookup and persistence of team-related data.
 */
public interface TeamDataSource {
  TeamDraftOrder getTeamDraftOrder(String username, String password) throws DataSourceException;

  boolean isCommissionerTeam(TeamDraftOrder teamDraftOrder) throws DataSourceException;

  Map<String, Team> getTeams() throws DataSourceException;

  HashMap<TeamDraftOrder, PlayerDataSet> getAutoPickWizards();

  void updateAutoPickWizard(TeamDraftOrder teamDraftOrder, PlayerDataSet wizardTable);

  TeamDraftOrder getDraftOrderByTeamId(TeamId teamID) throws DataSourceException;

  TeamId getTeamIdByDraftOrder(TeamDraftOrder draftOrder) throws DataSourceException;
}