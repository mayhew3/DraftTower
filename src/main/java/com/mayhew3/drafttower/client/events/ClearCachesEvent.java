package com.mayhew3.drafttower.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.mayhew3.drafttower.client.events.ClearCachesEvent.Handler;

/**
 * Event fired on "clear caches" click.
 */
public class ClearCachesEvent extends GwtEvent<Handler> {

  public static interface Handler extends EventHandler {
    void onClearCaches(ClearCachesEvent event);
  }

  public static final Type<Handler> TYPE = new Type<>();

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onClearCaches(this);
  }
}