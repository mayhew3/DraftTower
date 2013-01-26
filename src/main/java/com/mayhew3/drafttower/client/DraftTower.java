package com.mayhew3.drafttower.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class DraftTower implements EntryPoint {

  private final DraftTowerGinjector injector = GWT.create(DraftTowerGinjector.class);

  /**
   * This is the entry point method.
   */
  public void onModuleLoad() {
    RootPanel root = RootPanel.get("root");
    root.add(injector.getMainPageWidget());
  }
}
