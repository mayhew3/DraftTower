package com.mayhew3.drafttower.client.filledpositions;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.mayhew3.drafttower.shared.Position;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Chart showing proportion of teams which have filled each position so far.
 */
public class FilledPositionsChart extends Composite implements FilledPositionsView {

  interface Resources extends ClientBundle {
    interface Css extends CssResource {
      String container();
      String positionPanel();
      String positionLabel();
      String barLabel();
      String innerBarLabel();
      String outerBarLabel();
      String bar();
      String lastRoundBar();
    }

    @Source("FilledPositionsChart.css")
    Css css();
  }

  private static final Resources.Css CSS = ((Resources) GWT.create(Resources.class)).css();
  static {
    CSS.ensureInjected();
  }

  @VisibleForTesting
  static final String[] colors = {
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

  private final FilledPositionsPresenter presenter;

  private final Multimap<Position, InlineLabel> barLabels = HashMultimap.create();
  private final Map<Position, Widget> bars = new EnumMap<>(Position.class);
  private final Map<Position, Widget> lastRoundBars = new EnumMap<>(Position.class);

  @Inject
  public FilledPositionsChart(FilledPositionsPresenter presenter) {
    this.presenter = presenter;

    FlowPanel container = new FlowPanel();
    container.setStyleName(CSS.container());

    for (Position position : FilledPositionsPresenter.positions) {
      FlowPanel positionPanel = new FlowPanel();
      positionPanel.setStyleName(CSS.positionPanel());

      InlineLabel positionLabel = new InlineLabel(position.getShortName());
      positionLabel.setStyleName(CSS.positionLabel());
      positionPanel.add(positionLabel);

      InlineLabel barLabel = new InlineLabel("0/" + presenter.getDenominator(position));
      barLabel.setStyleName(CSS.barLabel());
      barLabel.addStyleName(CSS.outerBarLabel());
      positionPanel.add(barLabel);
      barLabels.put(position, barLabel);

      FlowPanel bar = new FlowPanel();
      bar.setStyleName(CSS.bar());
      positionPanel.add(bar);
      bars.put(position, bar);

      FlowPanel lastRoundBar = new FlowPanel();
      lastRoundBar.setStyleName(CSS.lastRoundBar());
      bar.add(lastRoundBar);
      lastRoundBars.put(position, lastRoundBar);

      InlineLabel innerBarLabel = new InlineLabel("0/" + presenter.getDenominator(position));
      innerBarLabel.setStyleName(CSS.barLabel());
      innerBarLabel.addStyleName(CSS.innerBarLabel());
      bar.add(innerBarLabel);
      barLabels.put(position, innerBarLabel);

      container.add(positionPanel);
    }

    initWidget(container);

    presenter.setView(this);
  }

  @Override
  public void setCounts(FilledPositionsCounts counts) {
    for (Position position : FilledPositionsPresenter.positions) {
      Integer numerator = counts.getPositionCount(position);
      int denominator = presenter.getDenominator(position);
      for (InlineLabel label : barLabels.get(position)) {
        label.setText(numerator + "/" + denominator);
      }
      bars.get(position).setWidth(((numerator / (float) denominator) * 100) + "px");
      bars.get(position).getElement().getStyle().setBackgroundColor(colors[numerator * 10 / denominator]);
      int lastRoundNumerator = counts.getPositionLastRoundCount(position);
      lastRoundBars.get(position).setWidth(((lastRoundNumerator / (float) denominator) * 100) + "px");
    }
  }

  @Override
  protected void onEnsureDebugId(String baseID) {
    super.onEnsureDebugId(baseID);
    for (Entry<Position, Widget> bar : bars.entrySet()) {
      bar.getValue().ensureDebugId(baseID + "-" + bar.getKey().getShortName());
    }
    for (Entry<Position, Widget> lastRoundBar : lastRoundBars.entrySet()) {
      lastRoundBar.getValue().ensureDebugId(baseID + "-" + lastRoundBar.getKey().getShortName() + "-last");
    }
    for (Position position : barLabels.keySet()) {
      barLabels.get(position).iterator().next().ensureDebugId(
          baseID + "-" + position.getShortName() + "label");
    }
  }
}