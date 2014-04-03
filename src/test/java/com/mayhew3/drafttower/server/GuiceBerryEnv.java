package com.mayhew3.drafttower.server;

import com.google.guiceberry.GuiceBerryModule;
import com.google.gwt.inject.rebind.adapter.GinModuleAdapter;
import com.google.inject.AbstractModule;
import com.mayhew3.drafttower.shared.SharedModule;

/**
 * Dependency bindings for tests.
 */
public class GuiceBerryEnv extends AbstractModule {
  @Override
  protected void configure() {
    install(new GuiceBerryModule());
    install(new GinModuleAdapter(new SharedModule()));
    install(new GinModuleAdapter(new ServerTestSafeModule()));
    install(new GinModuleAdapter(new TestServerModule()));
  }
}