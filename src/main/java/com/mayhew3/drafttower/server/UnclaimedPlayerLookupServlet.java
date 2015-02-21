package com.mayhew3.drafttower.server;

import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.UnclaimedPlayerListRequest;
import com.mayhew3.drafttower.shared.UnclaimedPlayerListResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet providing data for unclaimed player tables.
 */
@Singleton
public class UnclaimedPlayerLookupServlet extends HttpServlet {

  private final BeanFactory beanFactory;
  private final PlayerDataProvider playerDataProvider;

  @Inject
  public UnclaimedPlayerLookupServlet(BeanFactory beanFactory,
      PlayerDataProvider playerDataProvider) {
    this.beanFactory = beanFactory;
    this.playerDataProvider = playerDataProvider;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String requestStr = CharStreams.toString(req.getReader());
    UnclaimedPlayerListRequest request =
        AutoBeanCodex.decode(beanFactory, UnclaimedPlayerListRequest.class, requestStr).as();
    UnclaimedPlayerListResponse response;
    try {
      response = playerDataProvider.lookupUnclaimedPlayers(request);
    } catch (DataSourceException e) {
      throw new ServletException(e);
    }

    resp.getWriter().append(AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(response)).getPayload());
    resp.setContentType("text/json");
  }
}