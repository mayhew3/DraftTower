package com.mayhew3.drafttower.client.filledpositions;

import com.google.common.base.Function;
import com.google.common.collect.*;
import com.google.gwt.event.shared.EventBus;
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent;
import com.mayhew3.drafttower.shared.DraftPick;
import com.mayhew3.drafttower.shared.Position;
import com.mayhew3.drafttower.shared.RosterUtil;
import com.mayhew3.drafttower.shared.SharedModule.NumTeams;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

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
    List<DraftPick> picks = event.getStatus().getPicks();
    ImmutableListMultimap<Integer, DraftPick> picksPerTeam =
        Multimaps.index(picks, new Function<DraftPick, Integer>() {
          @Override
          public Integer apply(DraftPick input) {
            return input.getTeam();
          }
        });
    Map<Position, Integer> counts = Maps.newEnumMap(Position.class);
    for (Position position : positions) {
      counts.put(position, 0);
    }
    for (int i = 1; i <= numTeams; i++) {
      Multimap<Position, DraftPick> roster =
          rosterUtil.constructRoster(Lists.newArrayList(picksPerTeam.get(i)));
      for (Position position : positions) {
        counts.put(position, counts.get(position) + roster.get(position).size());
      }
    }
    view.setCounts(counts);
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