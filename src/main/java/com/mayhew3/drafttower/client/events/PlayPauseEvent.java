package com.mayhew3.drafttower.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.mayhew3.drafttower.client.events.PlayPauseEvent.Handler;

/**
 * Event fired on play/pause click.
 */
public class PlayPauseEvent extends GwtEvent<Handler> {

  public static interface Handler extends EventHandler {
    void onPlayPause(PlayPauseEvent event);
  }

  public static final Type<Handler> TYPE = new Type<Handler>();

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onPlayPause(this);
  }
}