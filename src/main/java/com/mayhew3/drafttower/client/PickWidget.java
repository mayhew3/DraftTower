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
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent;
import com.mayhew3.drafttower.client.events.EnqueuePlayerEvent;
import com.mayhew3.drafttower.client.events.PickPlayerEvent;
import com.mayhew3.drafttower.client.events.PlayerSelectedEvent;
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

  private final TeamInfo teamInfo;
  private final QueueDataProvider queueDataProvider;
  private final EventBus eventBus;

  @UiField Label selectedPlayerLabel;
  @UiField Button pick;
  @UiField Button enqueue;
  private DraftStatus status;
  private Long selectedPlayerId;

  @Inject
  public PickWidget(TeamInfo teamInfo,
      QueueDataProvider queueDataProvider,
      final EventBus eventBus) {
    this.teamInfo = teamInfo;
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
    selectedPlayerId = null;
    selectedPlayerLabel.setText("");
  }

  @UiHandler("enqueue")
  public void handleEnqueue(ClickEvent e) {
    eventBus.fireEvent(new EnqueuePlayerEvent(selectedPlayerId, null));
    selectedPlayerId = null;
    selectedPlayerLabel.setText("");
    updateButtonsEnabled();
  }

  private void updateButtonsEnabled() {
    pick.setEnabled(selectedPlayerId != null
        && status != null
        && status.getCurrentPickDeadline() > 0
        && !status.isPaused()
        && teamInfo.isLoggedIn()
        && teamInfo.getTeam() == status.getCurrentTeam());
    enqueue.setEnabled(selectedPlayerId != null && !queueDataProvider.isPlayerQueued(selectedPlayerId));
  }
}