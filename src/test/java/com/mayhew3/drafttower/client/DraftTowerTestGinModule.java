package com.mayhew3.drafttower.client;

import com.google.gwt.inject.client.AbstractGinModule;
import com.mayhew3.drafttower.server.ServerTestSafeModule;
import com.mayhew3.drafttower.server.TestServerModule;

import javax.inject.Singleton;

/**
 * Dependency injection module for test client-side dependencies.
 */
public class DraftTowerTestGinModule extends AbstractGinModule {
  @Override
  protected void configure() {
    install(new ServerTestSafeModule());
    install(new TestServerModule());
    bind(ServerRpc.class).to(TestServerRpc.class).in(Singleton.class);
  }
}