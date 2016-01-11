package com.mayhew3.drafttower.server;

import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.mayhew3.drafttower.shared.AddOrRemoveFavoriteRequest;
import com.mayhew3.drafttower.shared.BeanFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet handling changes in favorite players.
 */
@Singleton
public class AddOrRemoveFavoritePlayerServlet extends HttpServlet {

  private final BeanFactory beanFactory;
  private final PlayerDataProvider playerDataProvider;

  @Inject
  public AddOrRemoveFavoritePlayerServlet(BeanFactory beanFactory,
      PlayerDataProvider playerDataProvider) {
    this.beanFactory = beanFactory;
    this.playerDataProvider = playerDataProvider;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String requestStr = CharStreams.toString(req.getReader());
    AddOrRemoveFavoriteRequest request =
        AutoBeanCodex.decode(beanFactory, AddOrRemoveFavoriteRequest.class, requestStr).as();
    try {
      playerDataProvider.addOrRemoveFavorite(request);
    } catch (DataSourceException e) {
      throw new ServletException(e);
    }
  }
}