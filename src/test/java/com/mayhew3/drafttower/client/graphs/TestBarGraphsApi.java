package com.mayhew3.drafttower.client.graphs;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import javax.inject.Inject;

/**
 * Test version of {@link BarGraphsApi} which creates simple labels instead of bar graphs.
 */
public class TestBarGraphsApi implements BarGraphsApi {

  @Inject
  public TestBarGraphsApi() {}

  @Override
  public void loadVisualizationApi(Runnable callback) {
    callback.run();
  }

  @Override
  public Widget createBarGraph(String title, String[] labels, Float[] values, Float maxValue) {
    StringBuilder contents = new StringBuilder();
    for (int i = 0; i < labels.length; i++) {
      String label = labels[i];
      Float value = values[i];
      contents.append(label)
          .append("=")
          .append(value)
          .append(",");
    }
    return new Label(contents.toString());
  }
}