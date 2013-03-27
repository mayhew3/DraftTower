package com.mayhew3.drafttower.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.mayhew3.drafttower.client.events.IsUsersAutoPickWizardTableEvent.Handler;

/**
 * Event fired to notify components of whether the current table spec is the user's
 * auto-pick table spec.
 */
public class IsUsersAutoPickWizardTableEvent extends GwtEvent<Handler> {

  public static interface Handler extends EventHandler {
    void onSetAutoPickWizard(IsUsersAutoPickWizardTableEvent event);
  }

  public static final Type<Handler> TYPE = new Type<Handler>();

  private final boolean isUsersAutoPickWizardTable;

  public IsUsersAutoPickWizardTableEvent(boolean isUsersAutoPickWizard) {
    this.isUsersAutoPickWizardTable = isUsersAutoPickWizard;
  }

  public boolean isUsersAutoPickWizardTable() {
    return isUsersAutoPickWizardTable;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onSetAutoPickWizard(this);
  }
}