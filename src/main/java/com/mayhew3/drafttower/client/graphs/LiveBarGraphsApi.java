package com.mayhew3.drafttower.client.graphs;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.gwt.charts.client.*;
import com.googlecode.gwt.charts.client.corechart.BarChart;
import com.googlecode.gwt.charts.client.corechart.BarChartOptions;
import com.googlecode.gwt.charts.client.corechart.ColumnChart;
import com.googlecode.gwt.charts.client.corechart.ColumnChartOptions;
import com.googlecode.gwt.charts.client.options.*;
import com.mayhew3.drafttower.shared.Scoring;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;

/**
 * Live implementation of {@link BarGraphsApi}.
 */
public class LiveBarGraphsApi implements BarGraphsApi {

  @Inject
  public LiveBarGraphsApi() {}

  @Override
  public void loadVisualizationApi(final Runnable callback) {
    ChartLoader chartLoader = new ChartLoader(ChartPackage.CORECHART);
     chartLoader.loadApi(new Runnable() {
       @Override
       public void run() {
         callback.run();
       }
     });
  }

  @Override
  public Widget createBarGraph(
      String title, String[] labels, final Float[] values, Float maxValue) {
    DataTable data = DataTable.create();
    data.addColumn(ColumnType.STRING);
    for (String label : labels) {
      data.addColumn(ColumnType.NUMBER, label);
      data.addColumn(DataColumn.create(ColumnType.NUMBER, RoleType.ANNOTATION));
    }
    data.addRows(1);
    data.setValue(0, 0, "");
    List<Integer> indices = Lists.newArrayList();
    for (int i = 0; i < values.length; i++) {
      indices.add(i);
    }
    indices = Ordering.from(new Comparator<Integer>() {
      @Override
      public int compare(Integer o1, Integer o2) {
        return Float.compare(values[o2] == null ? 0 : values[o2],
            values[o1] == null ? 0 : values[o1]);
      }
    }).sortedCopy(indices);
    for (int i = 0; i < values.length; i++) {
      Float value = values[i];
      data.setValue(0, (i * 2) + 1, value == null ? 0 : value);
      data.setValue(0, (i * 2) + 2, indices.indexOf(i) + 1);
    }
    if (Scoring.CATEGORIES) {
      BarChart barChart = new BarChart();
      barChart.draw(data, (BarChartOptions) getOptions(title, maxValue));
//      BarChart barChart = new BarChart(data, getOptions(title, maxValue));
      return barChart;
    } else {
      ColumnChart columnChart = new ColumnChart();
      columnChart.draw(data, (ColumnChartOptions) getOptions(title, maxValue));
      return columnChart;
    }
  }

  private Options getOptions(String title, Float maxValue) {
    TextStyle titleTextStyle = TextStyle.create();
    titleTextStyle.setFontSize(12);
    if (Scoring.CATEGORIES) {
      BarChartOptions options = BarChartOptions.create();
      options.setColors("#aa4643", "#4572a7");
      options.setWidth(400);
      options.setHeight(100);
      options.setEnableInteractivity(false);
      options.setTitle(title);
      options.setTitleTextStyle(titleTextStyle);
      options.setLegend(Legend.create(LegendPosition.NONE));
      HAxis hAxis = HAxis.create();
      hAxis.setMinValue(0);
      if (maxValue != null) {
        hAxis.setMaxValue(maxValue);
      }
      options.setHAxis(hAxis);
      return options;
    } else {
      ColumnChartOptions options = ColumnChartOptions.create();
      options.setWidth(800);
      options.setHeight(240);
      Legend legend = Legend.create();
      TextStyle legendTextStyle = TextStyle.create();
      legendTextStyle.setFontSize(9);
      legend.setTextStyle(legendTextStyle);
      options.setLegend(legend);
      options.setTitle(title);
      options.setTitleTextStyle(titleTextStyle);
      VAxis vAxis = VAxis.create();
      vAxis.setMinValue(0);
      if (maxValue != null) {
        vAxis.setMaxValue(maxValue);
      }
      options.setVAxis(vAxis);
      return options;
    }
  }
}