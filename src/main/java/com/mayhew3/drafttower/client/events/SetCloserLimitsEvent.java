package com.mayhew3.drafttower.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.mayhew3.drafttower.client.events.SetCloserLimitsEvent.Handler;

/**
 * Event fired when the user changes closer limits.
 */
public class SetCloserLimitsEvent extends GwtEvent<Handler> {

  public static interface Handler extends EventHandler {
    void onSetCloserLimits(SetCloserLimitsEvent event);
  }

  public static final Type<Handler> TYPE = new Type<>();
  private final int minClosers;
  private final int maxClosers;


  public SetCloserLimitsEvent(int minClosers, int maxClosers) {
    this.minClosers = minClosers;
    this.maxClosers = maxClosers;
  }

  public int getMinClosers() {
    return minClosers;
  }

  public int getMaxClosers() {
    return maxClosers;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onSetCloserLimits(this);
  }
}