package com.mayhew3.drafttower.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Test version of entry point.
 */
public class DraftTowerTestEntryPoint implements EntryPoint {

  /**
   * This is the entry point method.
   */
  @Override
  public void onModuleLoad() {
    RootPanel root = RootPanel.get("root");
    root.add(DaggerDraftTowerTestComponent.create().mainPageWidget());
  }
}
