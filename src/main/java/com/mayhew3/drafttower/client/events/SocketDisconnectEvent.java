package com.mayhew3.drafttower.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.mayhew3.drafttower.client.events.SocketDisconnectEvent.Handler;

/**
 * Event fired when web socket disconnects.
 */
public class SocketDisconnectEvent extends GwtEvent<Handler> {

  public interface Handler extends EventHandler {
    void onDisconnect(SocketDisconnectEvent event);
  }

  public static final Type<Handler> TYPE = new Type<>();

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onDisconnect(this);
  }
}