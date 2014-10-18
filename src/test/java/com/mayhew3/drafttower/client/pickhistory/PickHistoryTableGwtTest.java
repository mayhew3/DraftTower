package com.mayhew3.drafttower.client.pickhistory;

import com.mayhew3.drafttower.client.TestBase;
import com.mayhew3.drafttower.shared.Position;

import static com.mayhew3.drafttower.shared.Position.*;

/**
 * GWT tests for pick history table widget.
 */
public class PickHistoryTableGwtTest extends TestBase {

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

  public void testPickHistoryTableContents() {
    login(1);
    simulateDraftStatus(POSITIONS);
    assertEquals("4:4", getInnerText("-pickHistory-1-0"));
    assertEquals("4", getInnerText("-pickHistory-1-1"));
    assertEquals("Guy 34", getInnerText("-pickHistory-1-2"));
    assertEquals("4:3", getInnerText("-pickHistory-2-0"));
    assertEquals("3", getInnerText("-pickHistory-2-1"));
    assertEquals("Guy 33", getInnerText("-pickHistory-2-2"));
    assertEquals("3:2", getInnerText("-pickHistory-13-0"));
    assertEquals("2", getInnerText("-pickHistory-13-1"));
    assertEquals("Guy 22", getInnerText("-pickHistory-13-2"));
  }

  public void testBackOutPick() {
    login(1);
    simulateDraftStatus(POSITIONS);
    click("-pickHistory-backOut");
    assertEquals(33, ginjector.getDraftStatus().getPicks().size());
  }

  public void testBackOutPickInvisibleNonCommish() {
    login(2);
    assertFalse(isVisible("-pickHistory-backOut"));
  }
}