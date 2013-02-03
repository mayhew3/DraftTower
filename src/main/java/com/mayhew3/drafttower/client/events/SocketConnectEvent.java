package com.mayhew3.drafttower.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.mayhew3.drafttower.client.events.SocketConnectEvent.Handler;

/**
 * Event fired when web socket connects.
 */
public class SocketConnectEvent extends GwtEvent<Handler> {

  public static interface Handler extends EventHandler {
    void onConnect(SocketConnectEvent event);
  }

  public static final Type<Handler> TYPE = new Type<Handler>();

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onConnect(this);
  }
}