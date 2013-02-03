package com.mayhew3.drafttower.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.mayhew3.drafttower.client.events.LoginEvent;
import com.mayhew3.drafttower.shared.DraftStatus;

/**
 * Widget for displaying the draft clock.
 */
public class DraftClock extends Composite implements
    DraftSocketHandler.DraftStatusListener,
    LoginEvent.Handler {

  interface Resources extends ClientBundle {
    interface Css extends CssResource {
      String container();
      String clock();
      String lowTime();
      String paused();
      String playPause();
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

  interface MyUiBinder extends UiBinder<Widget, DraftClock> {}
  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private final TeamInfo teamInfo;

  @UiField Label clockDisplay;
  @UiField Label playPause;
  private DraftStatus status;

  @Inject
  public DraftClock(DraftSocketHandler socketHandler,
      TeamInfo teamInfo,
      EventBus eventBus) {
    this.teamInfo = teamInfo;

    initWidget(uiBinder.createAndBindUi(this));

    playPause.setVisible(false);

    socketHandler.addListener(this);
    Scheduler.get().scheduleFixedPeriod(new Scheduler.RepeatingCommand() {
      public boolean execute() {
        update();
        return true;
      }
    }, MILLIS_PER_SECOND / 4);

    eventBus.addHandler(LoginEvent.TYPE, this);
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
      playPause.setText(status.isPaused() ? "▸" : "❙❙");
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

  public void onLogin(LoginEvent event) {
    playPause.setVisible(teamInfo.isCommissionerTeam());
  }

  @UiHandler("playPause")
  public void handlePlayPause(ClickEvent e) {

  }
}