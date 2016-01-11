package com.mayhew3.drafttower.client.graphs;

import com.google.gwt.user.client.ui.Widget;

/**
 * Interface wrapping visualization API interactions which can be stubbed out for tests.
 */
public interface BarGraphsApi {

  void loadVisualizationApi(Runnable callback);

  Widget createBarGraph(
      String title, String[] labels, Float[] values, Float maxValue);
}