package com.mayhew3.drafttower.client.clock;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.mayhew3.drafttower.client.DraftSocketHandler;
import com.mayhew3.drafttower.client.TeamsInfo;
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent;
import com.mayhew3.drafttower.client.events.LoginEvent;
import com.mayhew3.drafttower.client.events.PlayPauseEvent;
import com.mayhew3.drafttower.client.events.SocketDisconnectEvent;
import com.mayhew3.drafttower.shared.DraftStatus;

/**
 * Presenter for draft clock.
 */
public class ClockPresenter implements
    DraftStatusChangedEvent.Handler,
    SocketDisconnectEvent.Handler,
    LoginEvent.Handler {

  static final int MILLIS_PER_SECOND = 1000;
  private static final int MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;
  private static final int LOW_TIME_MS = 10 * MILLIS_PER_SECOND;

  private final DraftSocketHandler socketHandler;
  private final TeamsInfo teamsInfo;
  private final EventBus eventBus;
  private final CurrentTimeProvider currentTimeProvider;

  private ClockView view;
  private DraftStatus status;

  @Inject
  public ClockPresenter(DraftSocketHandler socketHandler,
      TeamsInfo teamsInfo,
      EventBus eventBus,
      CurrentTimeProvider currentTimeProvider) {
    this.socketHandler = socketHandler;
    this.teamsInfo = teamsInfo;
    this.eventBus = eventBus;
    this.currentTimeProvider = currentTimeProvider;

    eventBus.addHandler(LoginEvent.TYPE, this);
    eventBus.addHandler(DraftStatusChangedEvent.TYPE, this);
    eventBus.addHandler(SocketDisconnectEvent.TYPE, this);
  }

  public void setView(ClockView view) {
    this.view = view;
  }

  @Override
  public void onLogin(LoginEvent event) {
    view.setPlayPauseVisible(teamsInfo.isCommissionerTeam());
  }

  @Override
  public void onDraftStatusChanged(DraftStatusChangedEvent event) {
    this.status = event.getStatus();
    update();
  }

  @Override
  public void onDisconnect(SocketDisconnectEvent event) {
    view.clear();
  }

  public void playPause() {
    eventBus.fireEvent(new PlayPauseEvent());
  }

  public void update() {
    if (status != null) {
      if (status.getCurrentPickDeadline() == 0 || status.isOver()) {
        view.clear();
      } else if (!status.isPaused()) {
        long timeLeftMs = status.getCurrentPickDeadline() -
            (currentTimeProvider.getCurrentTimeMillis() + socketHandler.getServerClockDiff());
        timeLeftMs = Math.max(0, timeLeftMs);
        long minutes = timeLeftMs / MILLIS_PER_MINUTE;
        long seconds = (timeLeftMs % MILLIS_PER_MINUTE) / MILLIS_PER_SECOND;
        boolean lowTime = timeLeftMs < LOW_TIME_MS;
        view.updateTime(minutes, seconds, lowTime);
      }
      boolean paused = status.isPaused();
      boolean canPlay = paused || status.getCurrentPickDeadline() == 0;
      view.updatePaused(paused, canPlay);
    }
  }
}