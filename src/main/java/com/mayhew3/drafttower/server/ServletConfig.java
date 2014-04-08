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
    if (System.getProperty("gwtTests") != null) {
      // Don't inject server modules during tests - they're faked client-side.
      return Guice.createInjector();
    }
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
        serve("/" + ServletEndpoints.CHANGE_PLAYER_RANK_ENDPOINT)
            .with(ChangePlayerRankServlet.class);
        serve("/" + ServletEndpoints.COPY_ALL_PLAYER_RANKS_ENDPOINT)
            .with(CopyAllPlayerRanksServlet.class);
        serve("/" + ServletEndpoints.SET_AUTOPICK_WIZARD_ENDPOINT)
            .with(SetAutoPickWizardServlet.class);
        serve("/" + ServletEndpoints.QUEUE_ENDPOINT + "/*")
            .with(QueueServlet.class);
        serve("/" + ServletEndpoints.GRAPHS_ENDPOINT)
            .with(GraphsServlet.class);
      }
    }, new ServerModule());
  }
}
