package com.mayhew3.drafttower.client.teamorder;

import com.google.gwt.event.shared.EventBus;
import com.mayhew3.drafttower.client.TeamsInfo;
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent;
import com.mayhew3.drafttower.client.events.LoginEvent;
import com.mayhew3.drafttower.shared.DraftStatus;
import com.mayhew3.drafttower.shared.SharedModule.NumTeams;

import javax.inject.Inject;

/**
 * Presenter for team order display.
 */
public class TeamOrderPresenter implements
    DraftStatusChangedEvent.Handler,
    LoginEvent.Handler {

  private final int numTeams;
  private final TeamsInfo teamsInfo;
  private TeamOrderView view;

  @Inject
  public TeamOrderPresenter(@NumTeams int numTeams,
      TeamsInfo teamsInfo,
      EventBus eventBus) {
    this.numTeams = numTeams;
    this.teamsInfo = teamsInfo;

    eventBus.addHandler(DraftStatusChangedEvent.TYPE, this);
    eventBus.addHandler(LoginEvent.TYPE, this);
  }

  @Override
  public void onLogin(LoginEvent event) {
    for (int team = 1; team <= numTeams; team++) {
      view.setTeamName(team, teamsInfo.getShortTeamName(team));
      view.setMe(team, team == teamsInfo.getTeam());
    }
  }

  @Override
  public void onDraftStatusChanged(DraftStatusChangedEvent event) {
    DraftStatus status = event.getStatus();
    view.setRoundText(status.isOver()
        ? "It's over!"
        : "Round " + (status.getPicks().size() / numTeams + 1));
    for (int team = 1; team <= numTeams; team++) {
      view.setCurrent(team,
          team == status.getCurrentTeam());
      view.setDisconnected(team,
          !status.getConnectedTeams().contains(team));
      view.setRobot(team,
          status.getRobotTeams().contains(team));
      view.setKeeper(team,
          status.getNextPickKeeperTeams().contains(team));
    }
    if (status.isOver()) {
      view.setStatus("");
    } else if (status.getCurrentPickDeadline() > 0) {
      if (teamsInfo.isMyPick(status)) {
        view.setStatus("Your pick!");
      } else if (teamsInfo.isOnDeck(status)) {
        view.setStatus("On deck!");
      } else {
        view.setStatus("");
      }
    }
  }

  public void setView(TeamOrderView view) {
    this.view = view;
  }
}