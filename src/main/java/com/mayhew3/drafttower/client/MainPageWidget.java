package com.mayhew3.drafttower.client;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.inject.Inject;

/**
 * Widget containing the entire UI.
 */
public class MainPageWidget extends Composite {

  @Inject
  public MainPageWidget(DraftClock clock) {
    // TODO: use UiBinder to lay out real UI.
    FlowPanel container = new FlowPanel();
    container.add(clock);
    initWidget(container);
  }
}