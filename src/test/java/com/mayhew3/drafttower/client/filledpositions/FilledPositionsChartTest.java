package com.mayhew3.drafttower.client.filledpositions;

import com.mayhew3.drafttower.client.TestBase;

/**
 * GWT tests for filled positions chart widget.
 */
public class FilledPositionsChartTest extends TestBase {

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

  public void testFilledPositionsChartContents() {
    login(1);
    simulateDraftStatus(POSITIONS);
    assertEquals("0/10", getInnerText("-filledPositions-Clabel"));
    assertEquals(0, ensureDebugIdAndGetElement("-filledPositions-C", true).getOffsetWidth());
    assertEquals(FilledPositionsChart.colors[0], ensureDebugIdAndGetElement("-filledPositions-C", true).getStyle().getBackgroundColor());
    assertEquals("8/10", getInnerText("-filledPositions-1Blabel"));
    assertEquals(80, ensureDebugIdAndGetElement("-filledPositions-1B", true).getOffsetWidth());
    assertEquals(FilledPositionsChart.colors[8], ensureDebugIdAndGetElement("-filledPositions-1B", true).getStyle().getBackgroundColor());
    assertEquals("6/10", getInnerText("-filledPositions-2Blabel"));
    assertEquals(60, ensureDebugIdAndGetElement("-filledPositions-2B", true).getOffsetWidth());
    assertEquals(FilledPositionsChart.colors[6], ensureDebugIdAndGetElement("-filledPositions-2B", true).getStyle().getBackgroundColor());
    assertEquals("7/10", getInnerText("-filledPositions-3Blabel"));
    assertEquals(70, ensureDebugIdAndGetElement("-filledPositions-3B", true).getOffsetWidth());
    assertEquals(FilledPositionsChart.colors[7], ensureDebugIdAndGetElement("-filledPositions-3B", true).getStyle().getBackgroundColor());
    assertEquals("0/10", getInnerText("-filledPositions-SSlabel"));
    assertEquals(0, ensureDebugIdAndGetElement("-filledPositions-SS", true).getOffsetWidth());
    assertEquals(FilledPositionsChart.colors[0], ensureDebugIdAndGetElement("-filledPositions-SS", true).getStyle().getBackgroundColor());
    assertEquals("4/30", getInnerText("-filledPositions-OFlabel"));
    assertEquals(13, ensureDebugIdAndGetElement("-filledPositions-OF", true).getOffsetWidth());
    assertEquals(FilledPositionsChart.colors[1], ensureDebugIdAndGetElement("-filledPositions-OF", true).getStyle().getBackgroundColor());
    assertEquals("2/10", getInnerText("-filledPositions-DHlabel"));
    assertEquals(20, ensureDebugIdAndGetElement("-filledPositions-DH", true).getOffsetWidth());
    assertEquals(FilledPositionsChart.colors[2], ensureDebugIdAndGetElement("-filledPositions-DH", true).getStyle().getBackgroundColor());
    assertEquals("5/70", getInnerText("-filledPositions-Plabel"));
    assertEquals(7, ensureDebugIdAndGetElement("-filledPositions-P", true).getOffsetWidth());
    assertEquals(FilledPositionsChart.colors[0], ensureDebugIdAndGetElement("-filledPositions-P", true).getStyle().getBackgroundColor());
  }
}