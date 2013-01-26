package com.mayhew3.drafttower.client;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;
import com.mayhew3.drafttower.shared.DraftStatus;

/**
 * Widget for displaying the draft clock.
 */
public class DraftClock extends Composite implements DraftSocketHandler.DraftStatusListener {

  private final static int MILLIS_PER_SECOND = 1000;
  private final static int MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;

  private final Label clockDisplay;
  private DraftStatus status;

  @Inject
  public DraftClock(DraftSocketHandler socketHandler) {
    clockDisplay = new Label();
    initWidget(clockDisplay);

    socketHandler.addListener(this);
    Scheduler.get().scheduleFixedPeriod(new Scheduler.RepeatingCommand() {
      public boolean execute() {
        if (status != null && !status.isPaused()) {
          long timeLeftMs = status.getCurrentPickDeadline() - System.currentTimeMillis();
          timeLeftMs = Math.max(0, timeLeftMs);
          long minutes = timeLeftMs / MILLIS_PER_MINUTE;
          long seconds = (timeLeftMs % MILLIS_PER_MINUTE) / MILLIS_PER_SECOND;
          clockDisplay.setText(minutes + ":" + (seconds < 10 ? "0" : "") + seconds);
        }
        return true;
      }
    }, 1 * MILLIS_PER_SECOND);
  }

  public void onConnect() {
    // No-op.
  }

  public void onMessage(DraftStatus status) {
    this.status = status;
  }

  public void onDisconnect() {
    clockDisplay.setText("");
  }
}