package com.mayhew3.drafttower.server;

import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.mayhew3.drafttower.server.BindingAnnotations.AutoPickWizards;
import com.mayhew3.drafttower.server.BindingAnnotations.TeamTokens;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.PlayerDataSet;
import com.mayhew3.drafttower.shared.SetWizardTableRequest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Servlet handling changes to user's preferred wizard for auto-picks.
 */
@Singleton
public class SetAutoPickWizardServlet extends HttpServlet {

  private final BeanFactory beanFactory;
  private final TeamDataSource teamDataSource;
  private final Map<String, TeamDraftOrder> teamTokens;
  private final Map<TeamDraftOrder, PlayerDataSet> autoPickWizards;

  @Inject
  public SetAutoPickWizardServlet(BeanFactory beanFactory,
      TeamDataSource teamDataSource,
      @TeamTokens Map<String, TeamDraftOrder> teamTokens,
      @AutoPickWizards Map<TeamDraftOrder, PlayerDataSet> autoPickWizards) {
    this.beanFactory = beanFactory;
    this.teamDataSource = teamDataSource;
    this.teamTokens = teamTokens;
    this.autoPickWizards = autoPickWizards;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String requestStr = CharStreams.toString(req.getReader());
    SetWizardTableRequest request =
        AutoBeanCodex.decode(beanFactory, SetWizardTableRequest.class, requestStr).as();
    if (teamTokens.containsKey(request.getTeamToken())) {
      TeamDraftOrder teamDraftOrder = teamTokens.get(request.getTeamToken());
      PlayerDataSet dataSet = request.getPlayerDataSet();
      autoPickWizards.put(teamDraftOrder, dataSet);

      teamDataSource.updateAutoPickWizard(teamDraftOrder, dataSet);
    }
  }
}