package com.mayhew3.drafttower.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;

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
        serve("/socket").with(DraftTowerWebSocketServlet.class);
      }
    });
  }
}
