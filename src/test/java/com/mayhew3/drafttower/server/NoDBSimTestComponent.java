package com.mayhew3.drafttower.server;

import com.mayhew3.drafttower.server.ServerTestSafeModule.EagerSingletons;
import dagger.Component;

import javax.inject.Singleton;

/**
 * Dependency component for non-DB simulation tests.
 */
@Component(modules = SimTestModule.class)
@Singleton
public interface NoDBSimTestComponent extends SimTestComponent {
  EagerSingletons injectEager();
}