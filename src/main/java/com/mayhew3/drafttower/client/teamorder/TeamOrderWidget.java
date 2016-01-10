package com.mayhew3.drafttower.client.teamorder;

import com.google.common.annotations.VisibleForTesting;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
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
      String disconnect();
    }

    @Source("TeamOrderWidget.css")
    Css css();
  }

  @VisibleForTesting
  static final Resources.Css CSS = ((Resources) GWT.create(Resources.class)).css();
  static {
    CSS.ensureInjected();
  }

  private final Label roundLabel;
  private final Label teamLabels[];
  private final HTML disconnectButtons[];
  private final Label statusMessage;

  @Inject
  public TeamOrderWidget(@NumTeams int numTeams,
      final TeamOrderPresenter presenter) {
    FlowPanel container = new FlowPanel();
    container.setStyleName(CSS.container());

    roundLabel = new Label();
    roundLabel.addStyleName(CSS.round());
    container.add(roundLabel);
    teamLabels = new Label[numTeams];
    disconnectButtons = new HTML[numTeams];
    for (int team = 1; team <= numTeams; team++) {
      Label teamLogo = new Label();
      teamLogo.setStyleName(CSS.teamLogo());
      teamLabels[team - 1] = teamLogo;
      container.add(teamLogo);
      HTML disconnectButton = new HTML("X");
      disconnectButton.setStyleName(CSS.disconnect());
      disconnectButton.setVisible(false);
      final int teamToDisconnect = team;
      disconnectButton.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          presenter.disconnectClient(teamToDisconnect);
        }
      });
      disconnectButtons[team - 1] = disconnectButton;
      container.add(disconnectButton);
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

  @Override
  public void setDisconnectControlsEnabled(boolean enabled) {
    for (HTML disconnectButton : disconnectButtons) {
      disconnectButton.setVisible(enabled);
    }
  }

  @Override
  protected void onEnsureDebugId(String baseID) {
    super.onEnsureDebugId(baseID);
    roundLabel.ensureDebugId(baseID + "-round");
    statusMessage.ensureDebugId(baseID + "-status");
    for (int i = 0; i < teamLabels.length; i++) {
      teamLabels[i].ensureDebugId(baseID + "-" + i);
    }
  }
}