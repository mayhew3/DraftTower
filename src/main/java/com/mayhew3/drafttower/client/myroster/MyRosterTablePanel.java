package com.mayhew3.drafttower.client.myroster;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.inject.Inject;

/**
 * Widget containing user's current roster.
 */
public class MyRosterTablePanel extends Composite {

  interface Resources extends ClientBundle {
    interface Css extends CssResource {
      String container();
      String table();
    }

    @Source("MyRosterTablePanel.css")
    Css css();
  }

  private static final Resources.Css CSS = ((Resources) GWT.create(Resources.class)).css();
  static {
    CSS.ensureInjected();
  }

  private final MyRosterTable table;

  @Inject
  public MyRosterTablePanel(MyRosterTable table) {
    this.table = table;

    FlowPanel container = new FlowPanel();
    container.setStyleName(CSS.container());

    table.addStyleName(CSS.table());
    container.add(table);

    initWidget(container);
  }

  @Override
  protected void onEnsureDebugId(String baseID) {
    table.ensureDebugId(baseID);
  }
}