package com.mayhew3.drafttower.server;

import com.google.inject.Inject;
import com.mayhew3.drafttower.shared.BeanFactory;
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

  public static final String BAD_PASSWORD = "badpass";
  public static final int COMMISH_TEAM = 1;

  @Inject BeanFactory beanFactory;

  @Override
  public TeamDraftOrder getTeamDraftOrder(String username, String password) throws ServletException {
    if (password.equals(BAD_PASSWORD)) {
      return null;
    }
    return new TeamDraftOrder(Integer.parseInt(username));
  }

  @Override
  public boolean isCommissionerTeam(TeamDraftOrder teamDraftOrder) throws SQLException {
    return teamDraftOrder.get() == COMMISH_TEAM;
  }

  @Override
  public Map<String, Team> getTeams() throws SQLException {
    HashMap<String, Team> teams = new HashMap<>();
    for (int i = 0; i < 10; i++) {
      String teamNumber = Integer.toString(i);
      Team team = beanFactory.createTeam().as();
      team.setShortName(teamNumber);
      team.setLongName(teamNumber);
      teams.put(teamNumber, team);
    }
    return teams;
  }

  @Override
  public HashMap<TeamDraftOrder, PlayerDataSet> getAutoPickWizards() {
    // TODO(kprevas): implement
    return new HashMap<>();
  }

  @Override
  public void updateAutoPickWizard(TeamDraftOrder teamDraftOrder, PlayerDataSet wizardTable) {
    // TODO(kprevas): implement
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