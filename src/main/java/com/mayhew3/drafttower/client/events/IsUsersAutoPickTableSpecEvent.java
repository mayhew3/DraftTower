package com.mayhew3.drafttower.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.mayhew3.drafttower.client.events.IsUsersAutoPickTableSpecEvent.Handler;

/**
 * Event fired to notify components of whether the current table spec is the user's
 * auto-pick table spec.
 */
public class IsUsersAutoPickTableSpecEvent extends GwtEvent<Handler> {

  public static interface Handler extends EventHandler {
    void onSetAutoPickTableSpec(IsUsersAutoPickTableSpecEvent event);
  }

  public static final Type<Handler> TYPE = new Type<Handler>();

  private final boolean isUsersAutoPickTableSpec;

  public IsUsersAutoPickTableSpecEvent(boolean isUsersAutoPickTableSpec) {
    this.isUsersAutoPickTableSpec = isUsersAutoPickTableSpec;
  }

  public boolean isUsersAutoPickTableSpec() {
    return isUsersAutoPickTableSpec;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onSetAutoPickTableSpec(this);
  }
}