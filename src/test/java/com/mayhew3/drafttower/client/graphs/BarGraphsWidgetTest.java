package com.mayhew3.drafttower.client.graphs;

import com.mayhew3.drafttower.client.TestBase;
import com.mayhew3.drafttower.shared.Position;

import static com.mayhew3.drafttower.shared.Position.*;

/**
 * GWT tests for bar graphs.
 */
public class BarGraphsWidgetTest extends TestBase {

  private static final Position[][] POSITIONS = {
      {P, FB, TB, OF},
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

  public void testBarGraphsContents() {
    login(1);
    simulateDraftStatus(POSITIONS);
    click("-showBarGraphs");

    assertEquals("Me=10.0,Avg=12.400001,",
        getInnerText("-barGraphs-HR"));
    assertEquals("Me=30.0,Avg=37.2,",
        getInnerText("-barGraphs-RBI"));
    assertEquals("Me=0.2825,Avg=0.3005,",
        getInnerText("-barGraphs-OBP"));
    assertEquals("Me=0.46500003,Avg=0.50100005,",
        getInnerText("-barGraphs-SLG"));
    assertEquals("Me=30.0,Avg=37.2,",
        getInnerText("-barGraphs-R-"));
    assertEquals("Me=10.0,Avg=12.400001,",
        getInnerText("-barGraphs-SB-"));
    assertEquals("Me=10.0,Avg=2.1,",
        getInnerText("-barGraphs-INN"));
    assertEquals("Me=5.0,Avg=1.1,",
        getInnerText("-barGraphs-K"));
    assertEquals("Me=2.0,Avg=0.40100002,",
        getInnerText("-barGraphs-ERA"));
    assertEquals("Me=1.0,Avg=0.201,",
        getInnerText("-barGraphs-WHIP"));
    assertEquals("Me=0.0,Avg=0.1,",
        getInnerText("-barGraphs-W-"));
    assertEquals("Me=0.0,Avg=0.1,",
        getInnerText("-barGraphs-S"));
  }
}