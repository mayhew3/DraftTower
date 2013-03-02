package com.mayhew3.drafttower.server;

import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.mayhew3.drafttower.server.ServerModule.AutoPickTableSpecs;
import com.mayhew3.drafttower.server.ServerModule.TeamTokens;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.SetAutoPickTableSpecRequest;
import com.mayhew3.drafttower.shared.TableSpec;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Servlet handling changes to user's preferred table spec for auto-picks.
 */
@Singleton
public class SetAutoPickTableSpecServlet extends HttpServlet {

  private final BeanFactory beanFactory;
  private final Map<String, Integer> teamTokens;
  private final Map<Integer, TableSpec> autoPickTableSpecs;

  @Inject
  public SetAutoPickTableSpecServlet(BeanFactory beanFactory,
      @TeamTokens Map<String, Integer> teamTokens,
      @AutoPickTableSpecs Map<Integer, TableSpec> autoPickTableSpecs) {
    this.beanFactory = beanFactory;
    this.teamTokens = teamTokens;
    this.autoPickTableSpecs = autoPickTableSpecs;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String requestStr = CharStreams.toString(req.getReader());
    SetAutoPickTableSpecRequest request =
        AutoBeanCodex.decode(beanFactory, SetAutoPickTableSpecRequest.class, requestStr).as();
    autoPickTableSpecs.put(teamTokens.get(request.getTeamToken()),
        request.getTableSpec());
  }
}