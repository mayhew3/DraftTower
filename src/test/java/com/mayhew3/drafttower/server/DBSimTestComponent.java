package com.mayhew3.drafttower.server;

import com.mayhew3.drafttower.server.ServerTestSafeModule.EagerSingletons;
import dagger.Component;

import javax.inject.Singleton;

/**
 * Dependency component for DB simulation tests.
 */
@Component(modules = DBSimTestModule.class)
@Singleton
public interface DBSimTestComponent extends SimTestComponent {
  EagerSingletons injectEager();
}