package com.mayhew3.drafttower.client;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.media.client.Audio;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.inject.Inject;
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent;
import com.mayhew3.drafttower.client.events.LoginEvent;
import com.mayhew3.drafttower.shared.DraftStatus;

/**
 * Controls audio clips.
 */
public class AudioController extends Composite implements
    DraftStatusChangedEvent.Handler,
    LoginEvent.Handler {

  private final TeamsInfo teamsInfo;
  private Audio onDeck;
  private Audio onTheClock;
  private final Audio itsOver;
  private int lastTeam = -1;
  private boolean itWasOver;

  @Inject
  public AudioController(TeamsInfo teamsInfo,
      EventBus eventBus) {
    this.teamsInfo = teamsInfo;
    FlowPanel container = new FlowPanel();
    container.setSize("0", "0");

    this.itsOver = createAudio("over.mp3");

    initWidget(container);
    eventBus.addHandler(DraftStatusChangedEvent.TYPE, this);
    eventBus.addHandler(LoginEvent.TYPE, this);
  }

  private Audio createAudio(String src) {
    Audio onDeck = Audio.createIfSupported();
    onDeck.getAudioElement().setAttribute("rel", "noreferrer");
    onDeck.setPreload("auto");
    onDeck.setControls(false);
    onDeck.setSrc(src);
    return onDeck;
  }

  @Override
  public void onLogin(LoginEvent event) {
    this.onDeck = createAudio("deck_" + teamsInfo.getTeam() + ".mp3");
    this.onTheClock = createAudio("clock_" + teamsInfo.getTeam() + ".mp3");
  }

  @Override
  public void onDraftStatusChanged(DraftStatusChangedEvent event) {
    if (onDeck == null || onTheClock == null) {
      return;
    }
    onDeck.getAudioElement().pause();
    onDeck.getAudioElement().setCurrentTime(0);
    onTheClock.getAudioElement().pause();
    onTheClock.getAudioElement().setCurrentTime(0);
    itsOver.getAudioElement().pause();
    itsOver.getAudioElement().setCurrentTime(0);

    DraftStatus status = event.getStatus();
    if (status.isOver()) {
      if (!itWasOver) {
        itsOver.play();
      }
      itWasOver = true;
    } else if (status.getCurrentPickDeadline() > 0 && status.getCurrentTeam() != lastTeam) {
      if (teamsInfo.isMyPick(status)) {
        onTheClock.play();
      }
      if (teamsInfo.isOnDeck(status)) {
        onDeck.play();
      }
      lastTeam = status.getCurrentTeam();
    }
  }
}