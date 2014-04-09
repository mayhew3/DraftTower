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

  private final TeamsInfo teamsInfo;

  private AudioView audioView;
  private DraftStatus lastStatus;
  private boolean itWasOver;

  @Inject
  public AudioPresenter(TeamsInfo teamsInfo,
      EventBus eventBus) {
    this.teamsInfo = teamsInfo;
    eventBus.addHandler(DraftStatusChangedEvent.TYPE, this);
  }

  public void setAudioView(AudioView audioView) {
    this.audioView = audioView;
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
      if (lastStatus != null
          && status.getPicks().size() > lastStatus.getPicks().size()) {
        DraftPick lastPick = status.getPicks().get(status.getPicks().size() - 1);
        if (!lastPick.isKeeper()) {
          msg.append(getTeamName(lastPick.getTeam()))
              .append(" selects ")
              .append(lastPick.getPlayerName())
              .append(". ");
        }
      }
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
    if (msg.length() > 0) {
      audioView.play(msg.toString());
    }
    lastStatus = status;
  }

  private String getTeamName(int team) {
    String teamName = teamsInfo.getShortTeamName(team);
    teamName = teamName.replace("Lakshmi", "Lahkshmee");
    teamName = teamName.replace("Alcides", "Ahlseedess");
    return teamName;
  }
}