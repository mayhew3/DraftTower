package com.mayhew3.drafttower.server;

import com.google.inject.Inject;
import com.mayhew3.drafttower.server.ServerModule.TeamTokens;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * Servlet for logins.
 */
public class LoginServlet extends HttpServlet {

  private final Map<String, Integer> teamTokens;

  @Inject
  public LoginServlet(@TeamTokens Map<String, Integer> teamTokens) {
    this.teamTokens = teamTokens;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Integer team = getTeamNumber(req.getParameter("username"), req.getParameter("password"));
    if (team != null) {
      String teamToken = UUID.randomUUID().toString();
      resp.setContentType("text/plain");
      resp.getWriter().append(teamToken);
      teamTokens.put(teamToken, team);
    } else {
      resp.setStatus(403);
    }
  }

  /** Returns the team number corresponding to the given login credentials, or null for an invalid login. */
  private Integer getTeamNumber(String username, String password) {
    // TODO(m3): implement correctly
    if (username.equals("invalid")) {
      return null;
    }
    return Integer.parseInt(username);
  }
}