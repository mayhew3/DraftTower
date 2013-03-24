package com.mayhew3.drafttower.server;

import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.mayhew3.drafttower.server.BindingAnnotations.AutoPickTableSpecs;
import com.mayhew3.drafttower.server.BindingAnnotations.TeamTokens;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.TableSpec;
import com.mayhew3.drafttower.shared.UnclaimedPlayerListRequest;
import com.mayhew3.drafttower.shared.UnclaimedPlayerListResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Servlet providing data for unclaimed player tables.
 */
@Singleton
public class UnclaimedPlayerLookupServlet extends HttpServlet {

  private final BeanFactory beanFactory;
  private final PlayerDataSource playerDataSource;
  private final Map<String, Integer> teamTokens;
  private final Map<Integer, TableSpec> autoPickTableSpecs;

  @Inject
  public UnclaimedPlayerLookupServlet(BeanFactory beanFactory,
      PlayerDataSource playerDataSource,
      @TeamTokens Map<String, Integer> teamTokens,
      @AutoPickTableSpecs Map<Integer, TableSpec> autoPickTableSpecs) {
    this.beanFactory = beanFactory;
    this.playerDataSource = playerDataSource;
    this.teamTokens = teamTokens;
    this.autoPickTableSpecs = autoPickTableSpecs;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String requestStr = CharStreams.toString(req.getReader());
    UnclaimedPlayerListRequest request =
        AutoBeanCodex.decode(beanFactory, UnclaimedPlayerListRequest.class, requestStr).as();
    UnclaimedPlayerListResponse response = playerDataSource.lookupUnclaimedPlayers(request);

    response.setUsersAutoPickTableSpec(AutoBeanUtils.deepEquals(
        AutoBeanUtils.getAutoBean(request.getTableSpec()),
        AutoBeanUtils.getAutoBean(autoPickTableSpecs.get(teamTokens.get(request.getTeamToken())))));

    resp.getWriter().append(AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(response)).getPayload());
    resp.setContentType("text/json");
  }
}