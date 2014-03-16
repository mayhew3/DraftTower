package com.mayhew3.drafttower.server;

import com.mayhew3.drafttower.shared.PlayerDataSet;
import com.mayhew3.drafttower.shared.Team;

import javax.servlet.ServletException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles lookup and persistence of team-related data.
 */
public interface TeamDataSource {
  TeamDraftOrder getTeamDraftOrder(String username, String password) throws ServletException;

  boolean isCommissionerTeam(TeamDraftOrder teamDraftOrder) throws SQLException;

  Map<String, Team> getTeams() throws SQLException;

  HashMap<TeamDraftOrder, PlayerDataSet> getAutoPickWizards();

  void updateAutoPickWizard(TeamDraftOrder teamDraftOrder, PlayerDataSet wizardTable);

  TeamDraftOrder getDraftOrderByTeamId(TeamId teamID) throws SQLException;

  TeamId getTeamIdByDraftOrder(TeamDraftOrder draftOrder) throws SQLException;
}