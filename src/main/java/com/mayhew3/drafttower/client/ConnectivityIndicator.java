package com.mayhew3.drafttower.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.inject.Inject;
import com.mayhew3.drafttower.shared.DraftStatus;

/**
 * Indicator light showing connectivity to the server.
 */
public class ConnectivityIndicator extends Composite implements DraftSocketHandler.DraftStatusListener {

  interface Resources extends ClientBundle {
    interface Css extends CssResource {
      String indicator();
      String connected();
    }

    @Source("ConnectivityIndicator.css")
    Css css();
  }

  private static final Resources.Css CSS = ((Resources) GWT.create(Resources.class)).css();
  static {
    CSS.ensureInjected();
  }

  private final HTML widget;

  @Inject
  public ConnectivityIndicator(DraftSocketHandler socketHandler) {
    socketHandler.addListener(this);
    widget = new HTML();
    widget.setStyleName(CSS.indicator());
    initWidget(widget);
  }

  public void onConnect() {
    widget.addStyleName(CSS.connected());
  }

  public void onMessage(DraftStatus status) {
    // No-op.
  }

  public void onDisconnect() {
    widget.removeStyleName(CSS.connected());
  }
}