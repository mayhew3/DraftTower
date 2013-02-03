package com.mayhew3.drafttower.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.inject.Inject;
import com.mayhew3.drafttower.client.events.BackOutPickEvent;
import com.mayhew3.drafttower.client.events.LoginEvent;

/**
 * Widget containing pick history table and commissioner back-out control.
 */
public class PickHistoryTablePanel extends Composite implements
    LoginEvent.Handler {

  interface Resources extends ClientBundle {
    interface Css extends CssResource {
      String container();
      String table();
    }

    @Source("PickHistoryTablePanel.css")
    Css css();
  }

  private static final Resources.Css CSS = ((Resources) GWT.create(Resources.class)).css();
  static {
    CSS.ensureInjected();
  }

  private final Button backOutButton;
  private final TeamInfo teamInfo;

  @Inject
  public PickHistoryTablePanel(PickHistoryTable table,
      TeamInfo teamInfo,
      final EventBus eventBus) {
    this.teamInfo = teamInfo;

    FlowPanel container = new FlowPanel();
    container.setStyleName(CSS.container());

    table.addStyleName(CSS.table());
    ScrollPanel scrollPanel = new ScrollPanel(table);
    scrollPanel.setHeight("200px");
    container.add(scrollPanel);

    backOutButton = new Button("Back out last pick", new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        eventBus.fireEvent(new BackOutPickEvent());
      }
    });
    backOutButton.setVisible(false);
    container.add(backOutButton);

    initWidget(container);

    eventBus.addHandler(LoginEvent.TYPE, this);
  }

  @Override
  public void onLogin(LoginEvent event) {
    backOutButton.setVisible(teamInfo.isCommissionerTeam());
  }
}