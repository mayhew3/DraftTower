package com.mayhew3.drafttower.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.mayhew3.drafttower.client.events.BackOutPickEvent.Handler;

/**
 * Event fired on "back out last pick" click.
 */
public class BackOutPickEvent extends GwtEvent<Handler> {

  public static interface Handler extends EventHandler {
    void onBackOutPick(BackOutPickEvent event);
  }

  public static final Type<Handler> TYPE = new Type<>();

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onBackOutPick(this);
  }
}