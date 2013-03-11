package com.mayhew3.drafttower.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent;
import com.mayhew3.drafttower.shared.DraftStatus;
import com.mayhew3.drafttower.shared.SharedModule.NumTeams;

/**
 * Widget showing the order of upcoming picks.
 */
public class TeamOrderWidget extends Composite implements
    DraftStatusChangedEvent.Handler {

  interface Resources extends ClientBundle {
    interface Css extends CssResource {
      String container();
      String round();
      String currentPickArrow();
      String teamLogo();
      String me();
      String disconnected();
      String robot();
      String keeper();
      String statusMessage();
    }

    @Source("TeamOrderWidget.css")
    Css css();
  }

  private static final Resources.Css CSS = ((Resources) GWT.create(Resources.class)).css();
  static {
    CSS.ensureInjected();
  }

  private final int numTeams;
  private final TeamsInfo teamsInfo;
  private final FlowPanel container;

  @Inject
  public TeamOrderWidget(@NumTeams int numTeams,
      TeamsInfo teamsInfo,
      EventBus eventBus) {
    this.numTeams = numTeams;
    this.teamsInfo = teamsInfo;

    container = new FlowPanel();
    container.setStyleName(CSS.container());
    initWidget(container);

    eventBus.addHandler(DraftStatusChangedEvent.TYPE, this);
  }

  @Override
  public void onDraftStatusChanged(DraftStatusChangedEvent event) {
    container.clear();
    DraftStatus status = event.getStatus();
    Label roundLabel = new Label(status.isOver()
        ? "It's over!"
        : "Round " + (status.getPicks().size() / numTeams + 1));
    roundLabel.addStyleName(CSS.round());
    container.add(roundLabel);
    for (int i = 0; i < numTeams; i++) {
      int team = status.getCurrentTeam() + i;
      if (team > numTeams) {
        team -= numTeams;
      }
      SimplePanel teamLogo = new SimplePanel();
      Image image = new Image("team" + team + "logo.png");
      teamLogo.setWidget(image);
      teamLogo.setStyleName(CSS.teamLogo());
      teamLogo.setStyleName(CSS.me(),
          team == teamsInfo.getTeam());
      teamLogo.setStyleName(CSS.disconnected(),
          !status.getConnectedTeams().contains(team));
      teamLogo.setStyleName(CSS.robot(),
          status.getRobotTeams().contains(team));
      teamLogo.setStyleName(CSS.keeper(),
          status.getNextPickKeeperTeams().contains(team));
      image.setAltText(teamsInfo.getShortTeamName(team));
      image.setTitle(teamsInfo.getShortTeamName(team));
      container.add(teamLogo);
    }
    Label arrow = new Label("\u25bc");
    arrow.setStyleName(CSS.currentPickArrow());
    container.add(arrow);
    if (status.getCurrentPickDeadline() > 0) {
      if (status.getCurrentTeam() == teamsInfo.getTeam()) {
        Label statusMessage = new Label("Your pick!");
        statusMessage.setStyleName(CSS.statusMessage());
        container.add(statusMessage);
      }
      if (status.getCurrentTeam() == teamsInfo.getTeam() - 1
          || teamsInfo.getTeam() == 1 && status.getCurrentTeam() == numTeams) {
        Label statusMessage = new Label("On deck!");
        statusMessage.setStyleName(CSS.statusMessage());
        container.add(statusMessage);
      }
    }
  }
}