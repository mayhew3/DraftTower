package com.mayhew3.drafttower.client.myroster;

import com.mayhew3.drafttower.client.TestBase;
import com.mayhew3.drafttower.shared.Position;

import static com.mayhew3.drafttower.shared.Position.*;

/**
 * GWT tests for "my roster" table.
 */
public class MyRosterTableGwtTest extends TestBase {

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

  public void testMyRosterContents() {
    login(1);
    simulateDraftStatus(POSITIONS);

    assertEquals("C", getInnerText("-myRoster-1-0"));
    assertEquals("1B", getInnerText("-myRoster-2-0"));
    assertEquals("2B", getInnerText("-myRoster-3-0"));
    assertEquals("3B", getInnerText("-myRoster-4-0"));
    assertEquals("SS", getInnerText("-myRoster-5-0"));
    assertEquals("OF", getInnerText("-myRoster-6-0"));
    assertEquals("OF", getInnerText("-myRoster-7-0"));
    assertEquals("OF", getInnerText("-myRoster-8-0"));
    assertEquals("DH", getInnerText("-myRoster-9-0"));
    assertEquals("P", getInnerText("-myRoster-10-0"));
    assertEquals("P", getInnerText("-myRoster-11-0"));
    assertEquals("P", getInnerText("-myRoster-12-0"));
    assertEquals("P", getInnerText("-myRoster-13-0"));
    assertEquals("P", getInnerText("-myRoster-14-0"));
    assertEquals("P", getInnerText("-myRoster-15-0"));
    assertEquals("P", getInnerText("-myRoster-16-0"));
    assertEquals("RS", getInnerText("-myRoster-17-0"));
    assertEquals("RS", getInnerText("-myRoster-18-0"));
    assertEquals("RS", getInnerText("-myRoster-19-0"));
    assertEquals("RS", getInnerText("-myRoster-20-0"));
    assertEquals("RS", getInnerText("-myRoster-21-0"));
    assertEquals("RS", getInnerText("-myRoster-22-0"));

    assertEquals("Guy 11", getInnerText("-myRoster-2-1"));
    assertEquals("1B", getInnerText("-myRoster-2-2"));

    assertEquals("Guy 21", getInnerText("-myRoster-4-1"));
    assertEquals("3B", getInnerText("-myRoster-4-2"));

    assertEquals("Guy 31", getInnerText("-myRoster-6-1"));
    assertEquals("OF", getInnerText("-myRoster-6-2"));

    assertEquals("Guy 01", getInnerText("-myRoster-10-1"));
    assertEquals("P", getInnerText("-myRoster-10-2"));

    assertEquals("", getInnerText("-myRoster-5-1"));
    assertEquals("", getInnerText("-myRoster-5-2"));
  }
}