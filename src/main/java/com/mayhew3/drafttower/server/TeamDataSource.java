package com.mayhew3.drafttower.server;

import com.mayhew3.drafttower.shared.TableSpec;
import com.mayhew3.drafttower.shared.Team;

import javax.servlet.ServletException;
import java.sql.SQLException;
import java.util.Map;

/**
 * Handles lookup and persistence of team-related data.
 */
public interface TeamDataSource {
  Integer getTeamNumber(String username, String password) throws ServletException;

  boolean isCommissionerTeam(int team) throws SQLException;

  Map<String, Team> getTeams() throws SQLException;

  Map<Integer,TableSpec> getAutoPickTableSpecs(int numTeams);

  void updateAutoPickTable(int teamID, TableSpec tableSpec);
}