package com.mayhew3.drafttower.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.mayhew3.drafttower.client.events.WakeUpEvent.Handler;

/**
 * Event fired on "wake up" click.
 */
public class WakeUpEvent extends GwtEvent<Handler> {

  public static interface Handler extends EventHandler {
    void onWakeUp(WakeUpEvent event);
  }

  public static final Type<Handler> TYPE = new Type<>();

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onWakeUp(this);
  }
}