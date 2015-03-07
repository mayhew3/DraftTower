package com.mayhew3.drafttower.client.pickcontrols;

import com.google.gwt.event.shared.EventBus;
import com.mayhew3.drafttower.client.TeamsInfo;
import com.mayhew3.drafttower.client.events.*;
import com.mayhew3.drafttower.client.players.queue.QueueDataProvider;
import com.mayhew3.drafttower.shared.DraftPick;
import com.mayhew3.drafttower.shared.DraftStatus;

import javax.inject.Inject;

/**
 * Presenter for pick controls.
 */
public class PickControlsPresenter implements
    PlayerSelectedEvent.Handler,
    DraftStatusChangedEvent.Handler {

  private final TeamsInfo teamsInfo;
  private final QueueDataProvider queueDataProvider;
  private final EventBus eventBus;

  private PickControlsView view;

  private DraftStatus status;
  private Long selectedPlayerId;

  @Inject
  public PickControlsPresenter(TeamsInfo teamsInfo,
      QueueDataProvider queueDataProvider,
      final EventBus eventBus) {
    this.teamsInfo = teamsInfo;
    this.queueDataProvider = queueDataProvider;
    this.eventBus = eventBus;

    eventBus.addHandler(PlayerSelectedEvent.TYPE, this);
    eventBus.addHandler(DraftStatusChangedEvent.TYPE, this);
  }

  public void setView(PickControlsView view) {
    this.view = view;
  }

  @Override
  public void onDraftStatusChanged(DraftStatusChangedEvent event) {
    status = event.getStatus().getDraftStatus();
    for (DraftPick pick : status.getPicks()) {
      if (selectedPlayerId != null && pick.getPlayerId() == selectedPlayerId) {
        clearSelectedPlayer();
        return;
      }
    }
    updateButtonsEnabled();
  }

  @Override
  public void onPlayerSelected(PlayerSelectedEvent event) {
    selectedPlayerId = event.getPlayerId();
    view.setSelectedPlayerName(event.getPlayerName());
    updateButtonsEnabled();
  }

  private void clearSelectedPlayer() {
    selectedPlayerId = null;
    view.setSelectedPlayerName("");
    updateButtonsEnabled();
  }

  private void updateButtonsEnabled() {
    view.setPickEnabled(selectedPlayerId != null
        && status != null
        && status.getCurrentPickDeadline() > 0
        && !status.isPaused()
        && teamsInfo.isLoggedIn()
        && teamsInfo.getTeam() == status.getCurrentTeam());
    view.setEnqueueEnabled(selectedPlayerId != null && !queueDataProvider.isPlayerQueued(selectedPlayerId));
    view.setForcePickEnabled(status != null && status.getCurrentPickDeadline() > 0);
    view.setForcePickVisible(teamsInfo.isCommissionerTeam());
    view.setResetVisible(teamsInfo.isCommissionerTeam());
    view.setWakeUpVisible(status != null && status.getRobotTeams().contains(teamsInfo.getTeam()));
  }

  public void pick() {
    eventBus.fireEvent(new PickPlayerEvent(selectedPlayerId));
    clearSelectedPlayer();
  }

  public void enqueue() {
    eventBus.fireEvent(new EnqueuePlayerEvent(selectedPlayerId, null));
    clearSelectedPlayer();
  }

  public void forcePick() {
    eventBus.fireEvent(new ForcePickPlayerEvent(selectedPlayerId));
    clearSelectedPlayer();
  }

  public void wakeUp() {
    eventBus.fireEvent(new WakeUpEvent());
  }

  public void resetDraft() {
    eventBus.fireEvent(new ResetDraftEvent());
  }
}