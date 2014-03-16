package com.mayhew3.drafttower.server;

import com.mayhew3.drafttower.shared.PlayerDataSet;
import com.mayhew3.drafttower.shared.Team;

import javax.servlet.ServletException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Test version of {@link TeamDataSource}.
 */
public class TestTeamDataSource implements TeamDataSource {
  @Override
  public TeamDraftOrder getTeamDraftOrder(String username, String password) throws ServletException {
    return null;
  }

  @Override
  public boolean isCommissionerTeam(TeamDraftOrder teamDraftOrder) throws SQLException {
    return false;
  }

  @Override
  public Map<String, Team> getTeams() throws SQLException {
    return new HashMap<>();
  }

  @Override
  public HashMap<TeamDraftOrder, PlayerDataSet> getAutoPickWizards() {
    return new HashMap<>();
  }

  @Override
  public void updateAutoPickWizard(TeamDraftOrder teamDraftOrder, PlayerDataSet wizardTable) {
  }

  @Override
  public TeamDraftOrder getDraftOrderByTeamId(TeamId teamID) throws SQLException {
    return new TeamDraftOrder(teamID.get());
  }

  @Override
  public TeamId getTeamIdByDraftOrder(TeamDraftOrder draftOrder) throws SQLException {
    return new TeamId(draftOrder.get());
  }
}