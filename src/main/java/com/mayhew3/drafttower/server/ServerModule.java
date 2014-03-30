package com.mayhew3.drafttower.server;

import com.google.gwt.inject.rebind.adapter.GinModuleAdapter;
import com.google.inject.AbstractModule;
import com.mayhew3.drafttower.shared.SharedModule;

/**
 * Server-side dependency module.
 */
public class ServerModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new GinModuleAdapter(new SharedModule()));
    install(new ServerTestSafeModule());
    install(new ServerProductionModule());
  }
}