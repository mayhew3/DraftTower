package com.mayhew3.drafttower.server;

import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.ChangePlayerRankRequest;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet handling changes in player rank.
 */
@Singleton
public class ChangePlayerRankServlet extends HttpServlet {

  private final BeanFactory beanFactory;
  private final PlayerDataSource playerDataSource;

  @Inject
  public ChangePlayerRankServlet(BeanFactory beanFactory,
      PlayerDataSource playerDataSource) {
    this.beanFactory = beanFactory;
    this.playerDataSource = playerDataSource;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String requestStr = CharStreams.toString(req.getReader());
    ChangePlayerRankRequest request =
        AutoBeanCodex.decode(beanFactory, ChangePlayerRankRequest.class, requestStr).as();
    playerDataSource.changePlayerRank(request);
  }
}