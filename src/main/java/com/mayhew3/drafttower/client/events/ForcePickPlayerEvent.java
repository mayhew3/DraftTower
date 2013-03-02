package com.mayhew3.drafttower.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.mayhew3.drafttower.client.events.ForcePickPlayerEvent.Handler;

/**
 * Event fired on "back out last pick" click.
 */
public class ForcePickPlayerEvent extends GwtEvent<Handler> {

  public static interface Handler extends EventHandler {
    void onForcePick(ForcePickPlayerEvent event);
  }

  public static final Type<Handler> TYPE = new Type<Handler>();

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onForcePick(this);
  }
}