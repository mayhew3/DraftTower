package com.mayhew3.drafttower.client.audio;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.mayhew3.drafttower.client.TeamsInfo;
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent;
import com.mayhew3.drafttower.shared.DraftPick;
import com.mayhew3.drafttower.shared.DraftStatus;

import javax.inject.Singleton;

/**
 * Logic for audio clips.
 */
@Singleton
public class AudioPresenter implements DraftStatusChangedEvent.Handler {

  enum Level {
    OFF,
    LOW,
    HIGH
  }

  private final TeamsInfo teamsInfo;

  private AudioView audioView;
  private SpeechControlView speechControlView;
  private DraftStatus lastStatus;
  private boolean itWasOver;
  private Level level = Level.HIGH;

  @Inject
  public AudioPresenter(TeamsInfo teamsInfo,
      EventBus eventBus) {
    this.teamsInfo = teamsInfo;
    eventBus.addHandler(DraftStatusChangedEvent.TYPE, this);
  }

  public void setAudioView(AudioView audioView) {
    this.audioView = audioView;
  }

  public void setSpeechControlView(SpeechControlView speechControlView) {
    this.speechControlView = speechControlView;
  }

  @Override
  public void onDraftStatusChanged(DraftStatusChangedEvent event) {
    DraftStatus status = event.getStatus();
    if (status.isOver()) {
      if (!itWasOver) {
        audioView.playItsOver();
      }
      itWasOver = true;
      return;
    }
    StringBuilder msg = new StringBuilder();
    if (status.getCurrentPickDeadline() > 0) {
      if (level == Level.HIGH) {
        if (lastStatus != null) {
          int lastStatusNumPicks = lastStatus.getPicks().size();
          int numPicks = status.getPicks().size();
          if (numPicks > lastStatusNumPicks) {
            int pickIndex = numPicks - 1;
            DraftPick lastPick = status.getPicks().get(pickIndex);
            while (lastPick.isKeeper() && pickIndex - 1 >= lastStatusNumPicks) {
              pickIndex--;
              lastPick = status.getPicks().get(pickIndex);
            }
            if (!lastPick.isKeeper()) {
              msg.append(getTeamName(lastPick.getTeam()))
                  .append(" selects ")
                  .append(lastPick.getPlayerName())
                  .append(". ");
            }
          }
        }
      }
      if (level != Level.OFF) {
        if (lastStatus != null
            && status.getCurrentTeam() != lastStatus.getCurrentTeam()) {
          if (teamsInfo.isMyPick(status)) {
            msg.append(getTeamName(teamsInfo.getTeam()))
                .append(". you're on the clock");
          }
          if (teamsInfo.isOnDeck(status)) {
            msg.append(getTeamName(teamsInfo.getTeam()))
                .append(". you're on deck");
          }
        }
      }
    }
    if (msg.length() > 0) {
      audioView.play(msg.toString());
    }
    lastStatus = status;
  }

  public void toggleLevel() {
    switch (level) {
      case LOW:
        level = Level.OFF;
        break;
      case HIGH:
        level = Level.LOW;
        break;
      case OFF:
        level = Level.HIGH;
        break;
    }
    speechControlView.setLevel(level);
  }

  private String getTeamName(int team) {
    String teamName = teamsInfo.getShortTeamName(team);
    teamName = teamName.replace("Lakshmi", "Lahkshmee");
    teamName = teamName.replace("Alcides", "Ahlseedess");
    return teamName;
  }
}