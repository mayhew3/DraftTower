package com.mayhew3.drafttower.client.teamorder;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;
import com.mayhew3.drafttower.shared.SharedModule.NumTeams;

/**
 * Widget showing the order of upcoming picks.
 */
public class TeamOrderWidget extends Composite implements TeamOrderView {

  interface Resources extends ClientBundle {
    interface Css extends CssResource {
      String container();
      String round();
      String currentPick();
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

  private Label roundLabel;
  private Label teamLabels[];
  private Label statusMessage;

  @Inject
  public TeamOrderWidget(@NumTeams int numTeams,
      TeamOrderPresenter presenter) {
    FlowPanel container = new FlowPanel();
    container.setStyleName(CSS.container());

    roundLabel = new Label();
    roundLabel.addStyleName(CSS.round());
    container.add(roundLabel);
    teamLabels = new Label[numTeams];
    for (int team = 1; team <= numTeams; team++) {
      Label teamLogo = new Label();
      teamLogo.setStyleName(CSS.teamLogo());
      teamLabels[team - 1] = teamLogo;
      container.add(teamLogo);
    }
    statusMessage = new Label();
    statusMessage.setStyleName(CSS.statusMessage());
    container.add(statusMessage);

    initWidget(container);

    presenter.setView(this);
  }

  @Override
  public void setTeamName(int teamNum, String teamName) {
    teamLabels[teamNum - 1].setText(teamName);
  }

  @Override
  public void setRoundText(String text) {
    roundLabel.setText(text);
  }

  @Override
  public void setMe(int teamNum, boolean me) {
    teamLabels[teamNum - 1].setStyleName(CSS.me(), me);
  }

  @Override
  public void setCurrent(int teamNum, boolean current) {
    teamLabels[teamNum - 1].setStyleName(CSS.currentPick(), current);
  }

  @Override
  public void setDisconnected(int teamNum, boolean disconnected) {
    teamLabels[teamNum - 1].setStyleName(CSS.disconnected(), disconnected);
  }

  @Override
  public void setRobot(int teamNum, boolean robot) {
    teamLabels[teamNum - 1].setStyleName(CSS.robot(), robot);
  }

  @Override
  public void setKeeper(int teamNum, boolean keeper) {
    teamLabels[teamNum - 1].setStyleName(CSS.keeper(), keeper);
  }

  @Override
  public void setStatus(String status) {
    statusMessage.setText(status);
  }
}