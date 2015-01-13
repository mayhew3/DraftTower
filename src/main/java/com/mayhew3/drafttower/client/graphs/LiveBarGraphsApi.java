package com.mayhew3.drafttower.client.graphs;

import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
import com.google.gwt.visualization.client.DataTable;
import com.google.gwt.visualization.client.LegendPosition;
import com.google.gwt.visualization.client.VisualizationUtils;
import com.google.gwt.visualization.client.visualizations.corechart.*;
import com.google.gwt.visualization.client.visualizations.corechart.CoreChart.Type;
import com.mayhew3.drafttower.shared.Scoring;

/**
 * Live implementation of {@link BarGraphsApi}.
 */
public class LiveBarGraphsApi implements BarGraphsApi {
  @Override
  public void loadVisualizationApi(final Runnable callback) {
    VisualizationUtils.loadVisualizationApi(new Runnable() {
      @Override
      public void run() {
        callback.run();
      }
    }, CoreChart.PACKAGE);
  }

  @Override
  public Widget createBarGraph(
      String title, String[] labels, Float[] values, float maxValue) {
    DataTable data = DataTable.create();
    data.addColumn(ColumnType.STRING);
    for (String label : labels) {
      data.addColumn(ColumnType.NUMBER, label);
    }
    data.addRows(1);
    data.setValue(0, 0, "");
    for (int i = 0; i < values.length; i++) {
      Float value = values[i];
      data.setValue(0, i + 1, value == null ? 0 : value);
    }
    BarChart barChart = new BarChart(data, getOptions(title, maxValue));
    return barChart;
  }

  private Options getOptions(String title, float maxValue) {
    Options options = Options.create();
    options.setType(Type.BARS);
    if (Scoring.CATEGORIES) {
      options.setColors("#aa4643", "#4572a7");
      options.setWidth(400);
      options.setHeight(100);
    } else {
      options.setWidth(800);
      options.setHeight(500);
    }
    options.set("enableInteractivity", false);
    options.setTitle(title);
    TextStyle titleTextStyle = TextStyle.create();
    titleTextStyle.setFontSize(12);
    options.setTitleTextStyle(titleTextStyle);
    if (Scoring.CATEGORIES) {
      options.setLegend(LegendPosition.NONE);
    }
    AxisOptions hAxisOptions = AxisOptions.create();
    hAxisOptions.setMinValue(0);
    hAxisOptions.setMaxValue(maxValue);
    options.setHAxisOptions(hAxisOptions);
    return options;
  }
}