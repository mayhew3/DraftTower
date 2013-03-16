package com.mayhew3.drafttower.client;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.media.client.Audio;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.inject.Inject;
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent;
import com.mayhew3.drafttower.shared.DraftStatus;
import com.mayhew3.drafttower.shared.SharedModule.NumTeams;

/**
 * Controls audio clips.
 */
public class AudioController extends Composite implements
    DraftStatusChangedEvent.Handler {

  private final TeamsInfo teamsInfo;
  private final int numTeams;
  private final Audio onDeck;
  private final Audio onTheClock;
  private int lastTeam = -1;

  @Inject
  public AudioController(TeamsInfo teamsInfo,
      @NumTeams int numTeams,
      EventBus eventBus) {
    this.teamsInfo = teamsInfo;
    this.numTeams = numTeams;
    FlowPanel container = new FlowPanel();
    container.setSize("0", "0");

    onDeck = Audio.createIfSupported();
    onDeck.setPreload("auto");
    onDeck.setControls(false);
    onDeck.setSrc("deck.mp3");

    onTheClock = Audio.createIfSupported();
    onTheClock.setPreload("auto");
    onTheClock.setControls(false);
    onTheClock.setSrc("clock.mp3");

    initWidget(container);
    eventBus.addHandler(DraftStatusChangedEvent.TYPE, this);
  }

  @Override
  public void onDraftStatusChanged(DraftStatusChangedEvent event) {
    onDeck.getAudioElement().pause();
    onDeck.getAudioElement().setCurrentTime(0);
    onTheClock.getAudioElement().pause();
    onTheClock.getAudioElement().setCurrentTime(0);

    DraftStatus status = event.getStatus();
    if (status.getCurrentPickDeadline() > 0 && status.getCurrentTeam() != lastTeam) {
      if (status.getCurrentTeam() == teamsInfo.getTeam()) {
        onTheClock.play();
      }
      if (status.getCurrentTeam() == teamsInfo.getTeam() - 1
          || teamsInfo.getTeam() == 1 && status.getCurrentTeam() == numTeams) {
        onDeck.play();
      }
      lastTeam = status.getCurrentTeam();
    }
  }
}