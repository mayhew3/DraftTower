package com.mayhew3.drafttower.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;
import com.mayhew3.drafttower.shared.DraftStatus;

/**
 * Widget for displaying the draft clock.
 */
public class DraftClock extends Composite implements DraftSocketHandler.DraftStatusListener {

  interface Resources extends ClientBundle {
    interface Css extends CssResource {
      String clock();
      String lowTime();
      String paused();
    }

    @Source("DraftClock.css")
    Css css();
  }

  private static final int MILLIS_PER_SECOND = 1000;
  private static final int MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;
  private static final int LOW_TIME_MS = 10 * MILLIS_PER_SECOND;

  private static final Resources.Css CSS = ((Resources) GWT.create(Resources.class)).css();
  static {
    CSS.ensureInjected();
  }

  private final Label clockDisplay;
  private DraftStatus status;

  @Inject
  public DraftClock(DraftSocketHandler socketHandler) {
    clockDisplay = new Label();
    clockDisplay.setStyleName(CSS.clock());
    initWidget(clockDisplay);

    socketHandler.addListener(this);
    Scheduler.get().scheduleFixedPeriod(new Scheduler.RepeatingCommand() {
      public boolean execute() {
        update();
        return true;
      }
    }, MILLIS_PER_SECOND / 4);
  }

  private void update() {
    if (status != null) {
      if (!status.isPaused()) {
        long timeLeftMs = status.getCurrentPickDeadline() - System.currentTimeMillis();
        timeLeftMs = Math.max(0, timeLeftMs);
        long minutes = timeLeftMs / MILLIS_PER_MINUTE;
        long seconds = (timeLeftMs % MILLIS_PER_MINUTE) / MILLIS_PER_SECOND;
        clockDisplay.setText(minutes + ":" + (seconds < 10 ? "0" : "") + seconds);
        clockDisplay.setStyleName(CSS.lowTime(), timeLeftMs < LOW_TIME_MS);
      }
      clockDisplay.setStyleName(CSS.paused(), status.isPaused());
    }
  }

  public void onConnect() {
    // No-op.
  }

  public void onMessage(DraftStatus status) {
    this.status = status;
    update();
  }

  public void onDisconnect() {
    clockDisplay.setText("");
  }
}