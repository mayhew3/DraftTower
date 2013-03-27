package com.mayhew3.drafttower.server;

import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.mayhew3.drafttower.server.BindingAnnotations.TeamTokens;
import com.mayhew3.drafttower.server.BindingAnnotations.AutoPickWizards;
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
  private final Map<String, Integer> teamTokens;
  private final Map<Integer, PlayerDataSet> autoPickWizards;

  @Inject
  public SetAutoPickWizardServlet(BeanFactory beanFactory,
                                  TeamDataSource teamDataSource,
                                  @TeamTokens Map<String, Integer> teamTokens,
                                  @AutoPickWizards Map<Integer, PlayerDataSet> autoPickWizards) {
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
    int teamID = teamTokens.get(request.getTeamToken());
    PlayerDataSet dataSet = request.getPlayerDataSet();
    autoPickWizards.put(teamID, dataSet);

    teamDataSource.updateAutoPickWizard(teamID, dataSet);
  }
}