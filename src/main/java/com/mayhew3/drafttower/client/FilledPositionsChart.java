package com.mayhew3.drafttower.client;

import com.google.common.base.Function;
import com.google.common.collect.*;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;
import com.mayhew3.drafttower.client.events.DraftStatusChangedEvent;
import com.mayhew3.drafttower.shared.DraftPick;
import com.mayhew3.drafttower.shared.Position;
import com.mayhew3.drafttower.shared.RosterUtil;
import com.mayhew3.drafttower.shared.SharedModule.NumTeams;

import java.util.List;
import java.util.Map;

import static com.mayhew3.drafttower.shared.Position.*;

/**
 * Chart showing proportion of teams which have filled each position so far.
 */
public class FilledPositionsChart extends Composite implements
    DraftStatusChangedEvent.Handler {

  interface Resources extends ClientBundle {
    interface Css extends CssResource {
      String container();
      String positionPanel();
      String positionLabel();
      String barLabel();
      String bar();
    }

    @Source("FilledPositionsChart.css")
    Css css();
  }

  private static final Resources.Css CSS = ((Resources) GWT.create(Resources.class)).css();
  static {
    CSS.ensureInjected();
  }

  private static final Position[] positions = {
      C, FB, SB, SS, TB, OF, DH, P
  };
  private static final String[] colors = {
      "#00FF00",
      "#30FF00",
      "#60FF00",
      "#90FF00",
      "#C0FF00",
      "#F0FF00",
      "#FFE000",
      "#FFB000",
      "#FF9000",
      "#FF6000",
      "#FF3000",
      "#FF0000",
      "#FF0000"
  };

  private final int numTeams;
  private final Map<Position, InlineLabel> barLabels = Maps.newEnumMap(Position.class);
  private final Map<Position, Label> bars = Maps.newEnumMap(Position.class);

  @Inject
  public FilledPositionsChart(@NumTeams int numTeams,
      EventBus eventBus) {
    this.numTeams = numTeams;
    FlowPanel container = new FlowPanel();
    container.setStyleName(CSS.container());

    for (Position position : positions) {
      FlowPanel positionPanel = new FlowPanel();
      positionPanel.setStyleName(CSS.positionPanel());

      InlineLabel positionLabel = new InlineLabel(position.getShortName());
      positionLabel.setStyleName(CSS.positionLabel());
      positionPanel.add(positionLabel);

      InlineLabel barLabel = new InlineLabel("0/" + getDenominator(position));
      barLabel.setStyleName(CSS.barLabel());
      positionPanel.add(barLabel);
      barLabels.put(position, barLabel);

      Label bar = new Label();
      bar.setStyleName(CSS.bar());
      positionPanel.add(bar);
      bars.put(position, bar);

      container.add(positionPanel);
    }

    initWidget(container);

    eventBus.addHandler(DraftStatusChangedEvent.TYPE, this);
  }

  private static int getDenominator(Position position) {
    if (position == OF) {
      return 30;
    }
    if (position == P) {
      return 70;
    }
    return 10;
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
          RosterUtil.constructRoster(Lists.newArrayList(picksPerTeam.get(i)));
      for (Position position : positions) {
        counts.put(position, counts.get(position) + roster.get(position).size());
      }
    }
    for (Position position : positions) {
      Integer numerator = counts.get(position);
      int denominator = getDenominator(position);
      barLabels.get(position).setText(numerator + "/" + denominator);
      bars.get(position).setWidth(((numerator / (float) denominator) * 100) + "px");
      bars.get(position).getElement().getStyle().setBackgroundColor(colors[numerator * 10 / denominator]);
    }
  }
}