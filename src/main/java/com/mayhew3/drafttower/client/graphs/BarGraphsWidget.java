package com.mayhew3.drafttower.client.graphs;

import com.google.common.collect.ImmutableMap;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.LegendPosition;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.corechart.*;
import com.google.gwt.visualization.client.visualizations.corechart.CoreChart.Type;
import com.google.inject.Inject;
import com.mayhew3.drafttower.shared.PlayerColumn;

import java.util.Map;

import static com.mayhew3.drafttower.shared.PlayerColumn.*;

/**
 * Widget containing team comparison bar graphs.
 */
public class BarGraphsWidget extends Composite implements BarGraphsView {

  interface Resources extends ClientBundle {
    interface Css extends CssResource {
      String me();
      String avg();
      String graph();
    }

    @Source("BarGraphsWidget.css")
    Css css();
  }

  private static final Resources.Css CSS = ((Resources) GWT.create(Resources.class)).css();
  static {
    CSS.ensureInjected();
  }

  private static final Map<PlayerColumn, Float> MAX_VALUES = ImmutableMap.<PlayerColumn, Float>builder()
      .put(HR, 400f)
      .put(RBI, 1000f)
      .put(OBP, 0.5f)
      .put(SLG, 0.6f)
      .put(RHR, 800f)
      .put(SBCS, 150f)
      .put(INN, 1800f)
      .put(K, 1500f)
      .put(ERA, 4.5f)
      .put(WHIP, 1.4f)
      .put(WL, 50f)
      .put(S, 120f)
      .build();

  private final FlowPanel container;

  private boolean apiLoaded;

  @Inject
  public BarGraphsWidget(final BarGraphsPresenter presenter) {
    container = new FlowPanel();
    container.setSize("820px", "750px");
    addLabels();

    VisualizationUtils.loadVisualizationApi(new Runnable() {
      @Override
      public void run() {
        apiLoaded = true;
      }
    }, CoreChart.PACKAGE);

    addAttachHandler(new AttachEvent.Handler() {
      @Override
      public void onAttachOrDetach(AttachEvent event) {
        presenter.setActive(true);
      }
    });

    initWidget(container);
    presenter.setView(this);
  }

  private void addLabels() {
    FlowPanel labels = new FlowPanel();
    Label me = new Label("Me");
    me.setStyleName(CSS.me());
    labels.add(me);
    Label avg = new Label("Avg");
    avg.setStyleName(CSS.avg());
    labels.add(avg);
    container.add(labels);
  }

  @Override
  public void clear() {
    if (apiLoaded) {
      container.clear();
      addLabels();
    }
  }

  @Override
  public void updateBar(PlayerColumn statColumn,
      Float myValue, Float avgValue) {
    DataTable data = DataTable.create();
    data.addColumn(ColumnType.STRING);
    data.addColumn(ColumnType.NUMBER, "Me");
    data.addColumn(ColumnType.NUMBER, "Avg");
    data.addRows(1);
    data.setValue(0, 0, "");
    data.setValue(0, 1, myValue);
    data.setValue(0, 2, avgValue);
    BarChart barChart = new BarChart(data, getOptions(statColumn));
    barChart.addStyleName(CSS.graph());
    container.add(barChart);
  }

  private Options getOptions(PlayerColumn graphStat) {
    Options options = Options.create();
    options.setType(Type.BARS);
    options.setColors("#aa4643", "#4572a7");
    options.setWidth(400);
    options.setHeight(100);
    options.set("enableInteractivity", false);
    options.setTitle(graphStat.getLongName());
    TextStyle titleTextStyle = TextStyle.create();
    titleTextStyle.setFontSize(12);
    options.setTitleTextStyle(titleTextStyle);
    options.setLegend(LegendPosition.NONE);
    AxisOptions hAxisOptions = AxisOptions.create();
    hAxisOptions.setMinValue(0);
    hAxisOptions.setMaxValue(MAX_VALUES.get(graphStat));
    options.setHAxisOptions(hAxisOptions);
    return options;
  }
}