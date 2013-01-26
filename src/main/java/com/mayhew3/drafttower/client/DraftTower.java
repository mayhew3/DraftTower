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
  public void onModuleLoad() {
    RootPanel root = RootPanel.get("root");
    // TODO: add real UI to root panel.
    root.add(new WebSocketDemonstrationPanel());
  }
}
