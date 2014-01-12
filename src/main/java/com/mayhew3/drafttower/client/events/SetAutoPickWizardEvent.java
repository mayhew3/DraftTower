package com.mayhew3.drafttower.client.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.mayhew3.drafttower.client.events.SetAutoPickWizardEvent.Handler;
import com.mayhew3.drafttower.shared.PlayerDataSet;

/**
 * Event fired when the user picks a player.
 */
public class SetAutoPickWizardEvent extends GwtEvent<Handler> {

  public static interface Handler extends EventHandler {
    void onSetAutoPickWizard(SetAutoPickWizardEvent event);
  }

  public static final Type<Handler> TYPE = new Type<>();

  private final PlayerDataSet wizardTable;

  public SetAutoPickWizardEvent(PlayerDataSet wizardTable) {
    this.wizardTable = wizardTable;
  }

  public PlayerDataSet getWizardTable() {
    return wizardTable;
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