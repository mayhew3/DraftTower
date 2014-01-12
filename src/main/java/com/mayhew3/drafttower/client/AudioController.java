package com.mayhew3.drafttower.client;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.media.client.Audio;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.inject.Inject;
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent;
import com.mayhew3.drafttower.shared.DraftStatus;

/**
 * Controls audio clips.
 */
public class AudioController extends Composite implements
    DraftStatusChangedEvent.Handler {

  private final TeamsInfo teamsInfo;
  private final Audio onDeck;
  private final Audio onTheClock;
  private final Audio itsOver;
  private int lastTeam = -1;

  @Inject
  public AudioController(TeamsInfo teamsInfo,
      EventBus eventBus) {
    this.teamsInfo = teamsInfo;
    FlowPanel container = new FlowPanel();
    container.setSize("0", "0");

    this.onDeck = createAudio("deck.mp3");
    this.onTheClock = createAudio("clock.mp3");
    this.itsOver = createAudio("over.mp3");

    initWidget(container);
    eventBus.addHandler(DraftStatusChangedEvent.TYPE, this);
  }

  private Audio createAudio(String src) {
    Audio onDeck = Audio.createIfSupported();
    onDeck.setPreload("auto");
    onDeck.setControls(false);
    onDeck.setSrc(src);
    return onDeck;
  }

  @Override
  public void onDraftStatusChanged(DraftStatusChangedEvent event) {
    onDeck.getAudioElement().pause();
    onDeck.getAudioElement().setCurrentTime(0);
    onTheClock.getAudioElement().pause();
    onTheClock.getAudioElement().setCurrentTime(0);
    itsOver.getAudioElement().pause();
    itsOver.getAudioElement().setCurrentTime(0);

    DraftStatus status = event.getStatus();
    if (status.isOver()) {
      itsOver.play();
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