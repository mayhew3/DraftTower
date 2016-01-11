package com.mayhew3.drafttower.client.filledpositions;

import com.mayhew3.drafttower.client.TestBase;
import com.mayhew3.drafttower.shared.Position;

import static com.mayhew3.drafttower.shared.Position.*;

/**
 * GWT tests for filled positions chart widget.
 */
public class FilledPositionsChartGwtTest extends TestBase {

  private static final Position[][] POSITIONS = {
      {P, P, P, P},
      {P, FB, TB, OF},
      {OF, OF, OF, OF},
      {FB, FB, FB, FB},
      {FB, SB, TB, null},
      {FB, SB, TB, null},
      {FB, SB, TB, null},
      {FB, SB, TB, null},
      {FB, SB, TB, null},
      {FB, SB, TB, null},
  };

  public void testFilledPositionsChartContents() {
    login(1);
    simulateDraftStatus(POSITIONS);
    assertEquals("0/10", getInnerText("-filledPositions-Clabel"));
    assertEquals(0, ensureDebugIdAndGetElement("-filledPositions-C", true).getOffsetWidth());
    assertEquals(0, ensureDebugIdAndGetElement("-filledPositions-C-last", true).getOffsetWidth());
    assertEquals(FilledPositionsChart.colors[0], ensureDebugIdAndGetElement("-filledPositions-C", true).getStyle().getBackgroundColor());
    assertEquals("8/10", getInnerText("-filledPositions-1Blabel"));
    assertEquals(56, ensureDebugIdAndGetElement("-filledPositions-1B", true).getOffsetWidth());
    assertEquals(0, ensureDebugIdAndGetElement("-filledPositions-1B-last", true).getOffsetWidth());
    assertEquals(FilledPositionsChart.colors[8], ensureDebugIdAndGetElement("-filledPositions-1B", true).getStyle().getBackgroundColor());
    assertEquals("6/10", getInnerText("-filledPositions-2Blabel"));
    assertEquals(42, ensureDebugIdAndGetElement("-filledPositions-2B", true).getOffsetWidth());
    assertEquals(0, ensureDebugIdAndGetElement("-filledPositions-2B-last", true).getOffsetWidth());
    assertEquals(FilledPositionsChart.colors[6], ensureDebugIdAndGetElement("-filledPositions-2B", true).getStyle().getBackgroundColor());
    assertEquals("7/10", getInnerText("-filledPositions-3Blabel"));
    assertEquals(49, ensureDebugIdAndGetElement("-filledPositions-3B", true).getOffsetWidth());
    assertEquals(35, ensureDebugIdAndGetElement("-filledPositions-3B-last", true).getOffsetWidth());
    assertEquals(FilledPositionsChart.colors[7], ensureDebugIdAndGetElement("-filledPositions-3B", true).getStyle().getBackgroundColor());
    assertEquals("0/10", getInnerText("-filledPositions-SSlabel"));
    assertEquals(0, ensureDebugIdAndGetElement("-filledPositions-SS", true).getOffsetWidth());
    assertEquals(0, ensureDebugIdAndGetElement("-filledPositions-SS-last", true).getOffsetWidth());
    assertEquals(FilledPositionsChart.colors[0], ensureDebugIdAndGetElement("-filledPositions-SS", true).getStyle().getBackgroundColor());
    assertEquals("4/30", getInnerText("-filledPositions-OFlabel"));
    assertEquals(9, ensureDebugIdAndGetElement("-filledPositions-OF", true).getOffsetWidth());
    assertEquals(2, ensureDebugIdAndGetElement("-filledPositions-OF-last", true).getOffsetWidth());
    assertEquals(FilledPositionsChart.colors[1], ensureDebugIdAndGetElement("-filledPositions-OF", true).getStyle().getBackgroundColor());
    assertEquals("2/10", getInnerText("-filledPositions-DHlabel"));
    assertEquals(14, ensureDebugIdAndGetElement("-filledPositions-DH", true).getOffsetWidth());
    assertEquals(7, ensureDebugIdAndGetElement("-filledPositions-DH-last", true).getOffsetWidth());
    assertEquals(FilledPositionsChart.colors[2], ensureDebugIdAndGetElement("-filledPositions-DH", true).getStyle().getBackgroundColor());
    assertEquals("5/70", getInnerText("-filledPositions-Plabel"));
    assertEquals(5, ensureDebugIdAndGetElement("-filledPositions-P", true).getOffsetWidth());
    assertEquals(1, ensureDebugIdAndGetElement("-filledPositions-P-last", true).getOffsetWidth());
    assertEquals(FilledPositionsChart.colors[0], ensureDebugIdAndGetElement("-filledPositions-P", true).getStyle().getBackgroundColor());
  }
}