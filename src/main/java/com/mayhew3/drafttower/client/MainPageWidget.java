package com.mayhew3.drafttower.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.inject.Inject;

/**
 * Widget containing the entire UI.
 */
public class MainPageWidget extends Composite {

  @Inject
  public MainPageWidget(DraftClock clock,
                        final DraftSocketHandler socketHandler) {
    // TODO: use UiBinder to lay out real UI.
    FlowPanel container = new FlowPanel();
    container.add(clock);

    container.add(new Button("Start", new ClickHandler() {
      public void onClick(ClickEvent event) {
        socketHandler.sendMessage("start");
      }
    }));

    container.add(new Button("Pause", new ClickHandler() {
      public void onClick(ClickEvent event) {
        socketHandler.sendMessage("pause");
      }
    }));

    container.add(new Button("Resume", new ClickHandler() {
      public void onClick(ClickEvent event) {
        socketHandler.sendMessage("resume");
      }
    }));

    container.add(new Button("Simulate pick", new ClickHandler() {
      public void onClick(ClickEvent event) {
        socketHandler.sendMessage("doPick");
      }
    }));

    initWidget(container);
  }
}