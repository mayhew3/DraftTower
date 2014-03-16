package com.mayhew3.drafttower.server;

import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.mayhew3.drafttower.server.BindingAnnotations.TeamTokens;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.GetGraphsDataRequest;
import com.mayhew3.drafttower.shared.GraphsData;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/**
 * Servlet providing data for bar graphs.
 */
@Singleton
public class GraphsServlet extends HttpServlet {

  private final BeanFactory beanFactory;
  private final PlayerDataSource playerDataSource;
  private final Map<String, TeamDraftOrder> teamTokens;

  @Inject
  public GraphsServlet(BeanFactory beanFactory,
      PlayerDataSource playerDataSource,
      @TeamTokens Map<String, TeamDraftOrder> teamTokens) {
    this.beanFactory = beanFactory;
    this.playerDataSource = playerDataSource;
    this.teamTokens = teamTokens;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String requestStr = CharStreams.toString(req.getReader());
    GetGraphsDataRequest request =
        AutoBeanCodex.decode(beanFactory, GetGraphsDataRequest.class, requestStr).as();
    GraphsData response;
    try {
      if (teamTokens.containsKey(request.getTeamToken())) {
        response = playerDataSource.getGraphsData(teamTokens.get(request.getTeamToken()));
        resp.getWriter().append(AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(response)).getPayload());
      }
    } catch (SQLException e) {
      throw new ServletException(e);
    }

    resp.setContentType("text/json");
  }
}