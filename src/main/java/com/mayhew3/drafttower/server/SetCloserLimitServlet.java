package com.mayhew3.drafttower.server;

import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.mayhew3.drafttower.server.BindingAnnotations.TeamTokens;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.SetCloserLimitRequest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Servlet handling changes to user's min/max closer settings.
 */
@Singleton
public class SetCloserLimitServlet extends HttpServlet {

  private final BeanFactory beanFactory;
  private final Map<String, TeamDraftOrder> teamTokens;
  private final CloserLimitsHandler closerLimitsHandler;

  @Inject
  public SetCloserLimitServlet(BeanFactory beanFactory,
      CloserLimitsHandler closerLimitsHandler,
      @TeamTokens Map<String, TeamDraftOrder> teamTokens) {
    this.beanFactory = beanFactory;
    this.closerLimitsHandler = closerLimitsHandler;
    this.teamTokens = teamTokens;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String requestStr = CharStreams.toString(req.getReader());
    SetCloserLimitRequest request =
        AutoBeanCodex.decode(beanFactory, SetCloserLimitRequest.class, requestStr).as();
    if (teamTokens.containsKey(request.getTeamToken())) {
      TeamDraftOrder teamDraftOrder = teamTokens.get(request.getTeamToken());
      closerLimitsHandler.setCloserLimits(teamDraftOrder, request.getMinClosers(), request.getMaxClosers());
    }
  }
}