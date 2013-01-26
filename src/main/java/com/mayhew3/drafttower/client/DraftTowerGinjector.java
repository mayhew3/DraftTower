package com.mayhew3.drafttower.client;

import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;

/**
 * Dependency injector for client-side dependencies.
 */
@GinModules(DraftTowerGinModule.class)
public interface DraftTowerGinjector extends Ginjector {
  MainPageWidget getMainPageWidget();
}