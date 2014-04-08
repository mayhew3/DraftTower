package com.mayhew3.drafttower.server;

import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.CopyAllPlayerRanksRequest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet handling copying ordering to custom rankings.
 */
@Singleton
public class CopyAllPlayerRanksServlet extends HttpServlet {

  private final BeanFactory beanFactory;
  private final PlayerDataSource playerDataSource;

  @Inject
  public CopyAllPlayerRanksServlet(BeanFactory beanFactory,
      PlayerDataSource playerDataSource) {
    this.beanFactory = beanFactory;
    this.playerDataSource = playerDataSource;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String requestStr = CharStreams.toString(req.getReader());
    CopyAllPlayerRanksRequest request =
        AutoBeanCodex.decode(beanFactory, CopyAllPlayerRanksRequest.class, requestStr).as();

    try {
      playerDataSource.copyTableSpecToCustom(request);
    } catch (DataSourceException e) {
      throw new ServletException(e);
    }
  }
}