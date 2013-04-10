package com.mayhew3.drafttower.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.mayhew3.drafttower.client.events.*;
import com.mayhew3.drafttower.shared.DraftPick;
import com.mayhew3.drafttower.shared.DraftStatus;

/**
 * Widget for making picks.
 */
public class PickWidget extends Composite implements
    PlayerSelectedEvent.Handler,
    DraftStatusChangedEvent.Handler {

  interface Resources extends ClientBundle {
    interface Css extends CssResource {
      String container();
      String force();
    }

    @Source("PickWidget.css")
    Css css();
  }

  private static final Resources.Css CSS = ((Resources) GWT.create(Resources.class)).css();
  static {
    CSS.ensureInjected();
  }

  interface MyUiBinder extends UiBinder<Widget, PickWidget> {}
  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private final TeamsInfo teamsInfo;
  private final QueueDataProvider queueDataProvider;
  private final EventBus eventBus;

  @UiField Label selectedPlayerLabel;
  @UiField Button pick;
  @UiField Button enqueue;
  @UiField Button forcePick;
  @UiField Button wakeUp;
  private DraftStatus status;
  private Long selectedPlayerId;

  @Inject
  public PickWidget(TeamsInfo teamsInfo,
      QueueDataProvider queueDataProvider,
      final EventBus eventBus) {
    this.teamsInfo = teamsInfo;
    this.queueDataProvider = queueDataProvider;
    this.eventBus = eventBus;

    initWidget(uiBinder.createAndBindUi(this));

    pick.setEnabled(false);

    eventBus.addHandler(PlayerSelectedEvent.TYPE, this);
    eventBus.addHandler(DraftStatusChangedEvent.TYPE, this);
  }

  @Override
  public void onDraftStatusChanged(DraftStatusChangedEvent event) {
    status = event.getStatus();
    for (DraftPick pick : event.getStatus().getPicks()) {
      if (pick.getPlayerId() == selectedPlayerId) {
        clearSelectedPlayer();
      }
    }
    updateButtonsEnabled();
  }

  @Override
  public void onPlayerSelected(PlayerSelectedEvent event) {
    selectedPlayerId = event.getPlayerId();
    selectedPlayerLabel.setText(selectedPlayerId == null ? "" :
        event.getPlayerName());
    updateButtonsEnabled();
  }

  @UiHandler("pick")
  public void handlePick(ClickEvent e) {
    eventBus.fireEvent(new PickPlayerEvent(selectedPlayerId));
    clearSelectedPlayer();
  }

  @UiHandler("enqueue")
  public void handleEnqueue(ClickEvent e) {
    eventBus.fireEvent(new EnqueuePlayerEvent(selectedPlayerId, null));
    clearSelectedPlayer();
  }

  @UiHandler("forcePick")
  public void handleForcePick(ClickEvent e) {
    eventBus.fireEvent(new ForcePickPlayerEvent(selectedPlayerId));
    clearSelectedPlayer();
  }

  @UiHandler("wakeUp")
  public void handleWakeUp(ClickEvent e) {
    eventBus.fireEvent(new WakeUpEvent());
  }

  private void clearSelectedPlayer() {
    selectedPlayerId = null;
    selectedPlayerLabel.setText("");
    updateButtonsEnabled();
  }

  private void updateButtonsEnabled() {
    pick.setEnabled(selectedPlayerId != null
        && status != null
        && status.getCurrentPickDeadline() > 0
        && !status.isPaused()
        && teamsInfo.isLoggedIn()
        && teamsInfo.getTeam() == status.getCurrentTeam());
    enqueue.setEnabled(selectedPlayerId != null && !queueDataProvider.isPlayerQueued(selectedPlayerId));
    forcePick.setEnabled(status != null && status.getCurrentPickDeadline() > 0);
    forcePick.setVisible(teamsInfo.isCommissionerTeam());
    wakeUp.setVisible(status.getRobotTeams().contains(teamsInfo.getTeam()));
  }
}