package com.mayhew3.drafttower.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class DraftTower implements EntryPoint {

  /**
   * This is the entry point method.
   */
  @Override
  public void onModuleLoad() {
    RootPanel root = RootPanel.get("root");
    DraftTowerClientComponent clientComponent = DaggerDraftTowerClientComponent.create();
    clientComponent.injectEager();
    root.add(clientComponent.mainPageWidget());
  }
}
