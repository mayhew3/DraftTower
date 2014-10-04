package com.mayhew3.drafttower.client.filledpositions;

import com.google.common.annotations.VisibleForTesting;
import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
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
      String bar();
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

  private final Map<Position, InlineLabel> barLabels = new EnumMap<>(Position.class);
  private final Map<Position, Label> bars = new EnumMap<>(Position.class);

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
      positionPanel.add(barLabel);
      barLabels.put(position, barLabel);

      Label bar = new Label();
      bar.setStyleName(CSS.bar());
      positionPanel.add(bar);
      bars.put(position, bar);

      container.add(positionPanel);
    }

    initWidget(container);

    presenter.setView(this);
  }

  @Override
  public void setCounts(Map<Position, Integer> counts) {
    for (Position position : FilledPositionsPresenter.positions) {
      Integer numerator = counts.get(position);
      int denominator = presenter.getDenominator(position);
      barLabels.get(position).setText(numerator + "/" + denominator);
      bars.get(position).setWidth(((numerator / (float) denominator) * 100) + "px");
      bars.get(position).getElement().getStyle().setBackgroundColor(colors[numerator * 10 / denominator]);
    }
  }

  @Override
  protected void onEnsureDebugId(String baseID) {
    super.onEnsureDebugId(baseID);
    for (Entry<Position, Label> bar : bars.entrySet()) {
      bar.getValue().ensureDebugId(baseID + "-" + bar.getKey().getShortName());
    }
    for (Entry<Position, InlineLabel> barLabel : barLabels.entrySet()) {
      barLabel.getValue().ensureDebugId(
          baseID + "-" + barLabel.getKey().getShortName() + "label");
    }
  }
}