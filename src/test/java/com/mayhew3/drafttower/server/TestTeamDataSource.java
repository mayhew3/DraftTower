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
  public Integer getTeamNumber(String username, String password) throws ServletException {
    return null;
  }

  @Override
  public boolean isCommissionerTeam(int team) throws SQLException {
    return false;
  }

  @Override
  public Map<String, Team> getTeams() throws SQLException {
    return new HashMap<>();
  }

  @Override
  public Map<Integer, PlayerDataSet> getAutoPickWizards() {
    return new HashMap<>();
  }

  @Override
  public void updateAutoPickWizard(int teamID, PlayerDataSet wizardTable) {
  }
}