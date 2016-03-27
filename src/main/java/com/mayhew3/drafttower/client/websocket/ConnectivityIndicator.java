package com.mayhew3.drafttower.client.websocket;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.mayhew3.drafttower.client.events.SocketConnectEvent;
import com.mayhew3.drafttower.client.events.SocketDisconnectEvent;

import javax.inject.Inject;

/**
 * Indicator light showing connectivity to the server.
 */
public class ConnectivityIndicator extends Composite implements
    SocketConnectEvent.Handler,
    SocketDisconnectEvent.Handler {

  interface Resources extends ClientBundle {
    interface Css extends CssResource {
      String indicator();
      String connected();
    }

    @Source("ConnectivityIndicator.css")
    Css css();
  }

  static final Resources.Css CSS = ((Resources) GWT.create(Resources.class)).css();
  static {
    CSS.ensureInjected();
  }

  private final HTML widget;

  @Inject
  public ConnectivityIndicator(EventBus eventBus) {
    widget = new HTML();
    widget.setStyleName(CSS.indicator());
    initWidget(widget);

    eventBus.addHandler(SocketConnectEvent.TYPE, this);
    eventBus.addHandler(SocketDisconnectEvent.TYPE, this);
  }

  @Override
  public void onConnect(SocketConnectEvent event) {
    widget.addStyleName(CSS.connected());
  }

  @Override
  public void onDisconnect(SocketDisconnectEvent event) {
    widget.removeStyleName(CSS.connected());
  }
}