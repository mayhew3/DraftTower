package com.mayhew3.drafttower.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
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
import com.mayhew3.drafttower.shared.Player;

import static com.mayhew3.drafttower.shared.PlayerColumn.NAME;

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
  private final EventBus eventBus;

  @UiField Label selectedPlayerLabel;
  @UiField Button pick;
  @UiField Button enqueue;
  private DraftStatus status;
  private Player selectedPlayer;

  @Inject
  public PickWidget(TeamInfo teamInfo,
      final EventBus eventBus) {
    this.teamInfo = teamInfo;
    this.eventBus = eventBus;

    initWidget(uiBinder.createAndBindUi(this));

    pick.setEnabled(false);

    eventBus.addHandler(PlayerSelectedEvent.TYPE, this);
    eventBus.addHandler(DraftStatusChangedEvent.TYPE, this);

    Event.addNativePreviewHandler(new NativePreviewHandler() {
      @Override
      public void onPreviewNativeEvent(NativePreviewEvent preview) {
        NativeEvent event = preview.getNativeEvent();

        int keycode = event.getKeyCode();
        boolean ctrl = event.getCtrlKey() || event.getMetaKey();
        boolean alt = event.getAltKey();
        boolean shift = event.getShiftKey();
        if (pick.isEnabled() && event.getType().equalsIgnoreCase("keydown")) {
          if (ctrl && alt && shift && keycode == 'J') {
            preview.consume();
            eventBus.fireEvent(new PickPlayerEvent(Player.BEST_DRAFT_PICK));
          }
        }
      }
    });
  }

  @Override
  public void onDraftStatusChanged(DraftStatusChangedEvent event) {
    status = event.getStatus();
    updateButtonsEnabled();
  }

  @Override
  public void onPlayerSelected(PlayerSelectedEvent event) {
    selectedPlayer = event.getPlayer();
    selectedPlayerLabel.setText(selectedPlayer == null ? "" :
        selectedPlayer.getColumnValues().get(NAME));
    updateButtonsEnabled();
  }

  @UiHandler("pick")
  public void handlePick(ClickEvent e) {
    eventBus.fireEvent(new PickPlayerEvent(selectedPlayer.getPlayerId()));
    selectedPlayer = null;
    selectedPlayerLabel.setText("");
  }

  @UiHandler("enqueue")
  public void handleEnqueue(ClickEvent e) {
    eventBus.fireEvent(new EnqueuePlayerEvent(selectedPlayer.getPlayerId(), null));
    selectedPlayer = null;
    selectedPlayerLabel.setText("");
    updateButtonsEnabled();
  }

  private void updateButtonsEnabled() {
    pick.setEnabled(selectedPlayer != null
        && status != null
        && status.getCurrentPickDeadline() > 0
        && !status.isPaused()
        && teamInfo.isLoggedIn()
        && teamInfo.getTeam() == status.getCurrentTeam());
    enqueue.setEnabled(selectedPlayer != null);
  }
}