package com.mayhew3.drafttower.client.filledpositions;

import com.google.gwt.event.shared.EventBus;
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent;
import com.mayhew3.drafttower.shared.Position;
import com.mayhew3.drafttower.shared.RosterUtil;
import com.mayhew3.drafttower.shared.SharedModule.NumTeams;

import javax.inject.Inject;

import static com.mayhew3.drafttower.shared.Position.*;

/**
 * Presenter for filled positions chart.
 */
public class FilledPositionsPresenter implements
    DraftStatusChangedEvent.Handler {

  static final Position[] positions = {
      C, FB, SB, SS, TB, OF, DH, P
  };

  private final int numTeams;
  private final RosterUtil rosterUtil;
  private FilledPositionsView view;

  @Inject
  public FilledPositionsPresenter(
      @NumTeams int numTeams,
      EventBus eventBus,
      RosterUtil rosterUtil) {
    this.numTeams = numTeams;
    this.rosterUtil = rosterUtil;

    eventBus.addHandler(DraftStatusChangedEvent.TYPE, this);
  }

  @Override
  public void onDraftStatusChanged(DraftStatusChangedEvent event) {
    view.setCounts(new FilledPositionsCounts(event.getStatus().getPicks(), numTeams, rosterUtil));
  }

  public int getDenominator(Position position) {
    if (position == OF) {
      return 3 * numTeams;
    }
    if (position == P) {
      return 7 * numTeams;
    }
    return numTeams;
  }

  public void setView(FilledPositionsView view) {
    this.view = view;
  }
}