package com.mayhew3.drafttower.server;

import com.google.common.base.Objects;
import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.mayhew3.drafttower.server.BindingAnnotations.TeamTokens;
import com.mayhew3.drafttower.server.BindingAnnotations.AutoPickWizards;
import com.mayhew3.drafttower.shared.*;

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
  private final Map<Integer, PlayerDataSet> autoPickWizardTables;

  @Inject
  public UnclaimedPlayerLookupServlet(BeanFactory beanFactory,
      PlayerDataSource playerDataSource,
      @TeamTokens Map<String, Integer> teamTokens,
      @AutoPickWizards Map<Integer, PlayerDataSet> autoPickWizardTables) {
    this.beanFactory = beanFactory;
    this.playerDataSource = playerDataSource;
    this.teamTokens = teamTokens;
    this.autoPickWizardTables = autoPickWizardTables;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String requestStr = CharStreams.toString(req.getReader());
    UnclaimedPlayerListRequest request =
        AutoBeanCodex.decode(beanFactory, UnclaimedPlayerListRequest.class, requestStr).as();
    UnclaimedPlayerListResponse response = playerDataSource.lookupUnclaimedPlayers(request);

    PlayerDataSet currentDataSet = request.getTableSpec().getPlayerDataSet();
    PlayerDataSet autoPickWizardDataSet = autoPickWizardTables.get(teamTokens.get(request.getTeamToken()));
    response.setUsersAutoPickWizardTable(Objects.equal(currentDataSet, autoPickWizardDataSet));

    resp.getWriter().append(AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(response)).getPayload());
    resp.setContentType("text/json");
  }
}