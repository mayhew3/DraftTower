package com.mayhew3.drafttower.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.mayhew3.drafttower.shared.ServletEndpoints;

/**
 * Server-side dependency injection (including servlet configuration).
 */
public class ServletConfig extends GuiceServletContextListener {

  @Override
  protected Injector getInjector() {
    return Guice.createInjector(new ServletModule() {
      @Override
      protected void configureServlets() {
        super.configureServlets();
        serve("/" + ServletEndpoints.LOGIN_ENDPOINT)
            .with(LoginServlet.class);
        serve("/" + ServletEndpoints.DRAFT_SOCKET_ENDPOINT)
            .with(DraftTowerWebSocketServlet.class);
        serve("/" + ServletEndpoints.UNCLAIMED_PLAYERS_ENDPOINT)
            .with(UnclaimedPlayerLookupServlet.class);
      }
    }, new ServerModule());
  }
}
