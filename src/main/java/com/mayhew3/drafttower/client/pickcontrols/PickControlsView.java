package com.mayhew3.drafttower.client.pickcontrols;

/**
 * Interface for pick controls widget.
 */
public interface PickControlsView {
  void setSelectedPlayerName(String name);

  void setPickEnabled(boolean enabled);

  void setEnqueueEnabled(boolean enabled);

  void setForcePickEnabled(boolean enabled);

  void setForcePickVisible(boolean visible);

  void setResetVisible(boolean visible);

  void setWakeUpVisible(boolean visible);
}