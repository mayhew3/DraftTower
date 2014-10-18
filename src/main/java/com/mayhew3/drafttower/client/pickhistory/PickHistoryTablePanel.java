package com.mayhew3.drafttower.client.pickhistory;

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
import com.mayhew3.drafttower.client.TeamsInfo;
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
  private final PickHistoryTable table;
  private final TeamsInfo teamsInfo;

  @Inject
  public PickHistoryTablePanel(PickHistoryTable table,
      TeamsInfo teamsInfo,
      final EventBus eventBus) {
    this.teamsInfo = teamsInfo;

    FlowPanel container = new FlowPanel();
    container.setStyleName(CSS.container());

    this.table = table;
    this.table.addStyleName(CSS.table());
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
    backOutButton.setVisible(teamsInfo.isCommissionerTeam());
  }

  @Override
  protected void onEnsureDebugId(String baseID) {
    super.onEnsureDebugId(baseID);
    backOutButton.ensureDebugId(baseID + "-backOut");
    table.ensureDebugId(baseID);

  }
}