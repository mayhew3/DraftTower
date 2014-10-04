package com.mayhew3.drafttower.client.depthcharts;

import com.mayhew3.drafttower.client.TestBase;

/**
 * GWT tests for depth charts table widget.
 */
public class DepthChartsTableTest extends TestBase {

  private static final String[][] POSITIONS = {
      {"P", "P", "P", "P"},
      {"P", "1B", "3B", "OF"},
      {"OF", "OF", "OF", "OF"},
      {"1B", "1B", "1B", "1B"},
      {"1B", "2B", "3B", ""},
      {"1B", "2B", "3B", ""},
      {"1B", "2B", "3B", ""},
      {"1B", "2B", "3B", ""},
      {"1B", "2B", "3B", ""},
      {"1B", "2B", "3B", ""},
  };

  public void testDepthChartsTableContents() {
    login(1);
    simulateDraftStatus(POSITIONS);
    click("-showDepthCharts");
    assertTrue(isVisible("-depthCharts"));

    for (int i = 1; i <= 10; i++) {
      assertEquals("" + i, getInnerText("-depthCharts-" + i + "-0"));
    }

    assertEquals("C", getInnerText("-depthCharts-0-1"));
    assertEquals("1B", getInnerText("-depthCharts-0-2"));
    assertEquals("2B", getInnerText("-depthCharts-0-3"));
    assertEquals("3B", getInnerText("-depthCharts-0-4"));
    assertEquals("SS", getInnerText("-depthCharts-0-5"));
    assertEquals("OF", getInnerText("-depthCharts-0-6"));
    assertEquals("DH", getInnerText("-depthCharts-0-7"));
    assertEquals("P", getInnerText("-depthCharts-0-8"));
    assertEquals("RS", getInnerText("-depthCharts-0-9"));

    assertTrue(getInnerText("-depthCharts-8-1").isEmpty());

    assertEquals("Guy 01Guy 11Guy 21Guy 31", getInnerText("-depthCharts-1-8"));

    assertEquals("Guy 02", getInnerText("-depthCharts-2-8"));
    assertEquals("Guy 12", getInnerText("-depthCharts-2-2"));
    assertEquals("Guy 22", getInnerText("-depthCharts-2-4"));
    assertEquals("Guy 32", getInnerText("-depthCharts-2-6"));

    assertEquals("Guy 03Guy 13Guy 23", getInnerText("-depthCharts-3-6"));
    assertEquals("Guy 33", getInnerText("-depthCharts-3-7"));

    assertEquals("Guy 04", getInnerText("-depthCharts-4-2"));
    assertEquals("Guy 14", getInnerText("-depthCharts-4-7"));
    assertEquals("Guy 24Guy 34", getInnerText("-depthCharts-4-9"));
  }
}