package com.mayhew3.drafttower.client.players.unclaimed;

import com.google.gwt.dom.client.InputElement;
import com.mayhew3.drafttower.client.TestBase;
import com.mayhew3.drafttower.shared.Position;
import com.mayhew3.drafttower.shared.Scoring;

import static com.mayhew3.drafttower.shared.Position.P;

/**
 * GWT test for main table panel.
 */
public class UnclaimedPlayerTablePanelGwtTest extends TestBase {

  public void testDataSetButtons() {
    login(1);
    click("-players-Averages");
    assertContains("gwt-ToggleButton-down",
        ensureDebugIdAndGetElement("-players-Averages", true).getClassName());
    assertDoesNotContain("gwt-ToggleButton-down",
        ensureDebugIdAndGetElement("-players-CBSSports", true).getClassName());
  }

  public void testSinglePositionFilter() {
    login(1);
    click("-players-2B");
    assertContains("gwt-ToggleButton-down",
        ensureDebugIdAndGetElement("-players-2B", true).getClassName());
    for (int i = 1; i < 16; i++) {
      assertEquals("2B", getInnerText("-players-table-" + i + "-4"));
    }
  }

  public void testStartingPitcherFilter() {
    login(1);
    click("-players-SP");
    assertContains("gwt-ToggleButton-down",
        ensureDebugIdAndGetElement("-players-SP", true).getClassName());
    for (int i = 1; i < 16; i++) {
      assertEquals("P", getInnerText("-players-table-" + i + "-4"));
      assertEquals("0", getInnerText("-players-table-" + i + "-10"));
    }
  }

  public void testCloserPitcherFilter() {
    login(1);
    click("-players-RP");
    assertContains("gwt-ToggleButton-down",
        ensureDebugIdAndGetElement("-players-RP", true).getClassName());
    for (int i = 1; i < 16; i++) {
      assertEquals("P", getInnerText("-players-table-" + i + "-4"));
      assertFalse("0".equals(getInnerText("-players-table-" + i + "-10")));
    }
  }

  public void testAllPositionFilter() {
    login(1);
    simulateDraftStatus(new Position[][]{
        {P, P, P, P, P, P, P, P, P, P},
        {P, P, P, P, P, P, P, P, P, P},
        {P, P, P, P, P, P, P, P, P, P},
        {P, P, P, P, P, P, P, P, P, P},
        {P, P, P, P, P, P, P, P, P, P},
        {P, P, P, P, P, P, P, P, P, P},
        {P, P, P, P, P, P, P, P, P, P},
    });
    assertEquals("C", getInnerText("-players-table-1-4"));
    click("-players-All");
    assertContains("gwt-ToggleButton-down",
        ensureDebugIdAndGetElement("-players-All", true).getClassName());
    assertEquals("P", getInnerText("-players-table-1-4"));
  }

  public void testUnfilledPositionFilter() {
    login(1);
    click("-players-All");
    simulateDraftStatus(new Position[][]{
        {P, P, P, P, P, P, P, P, P, P},
        {P, P, P, P, P, P, P, P, P, P},
        {P, P, P, P, P, P, P, P, P, P},
        {P, P, P, P, P, P, P, P, P, P},
        {P, P, P, P, P, P, P, P, P, P},
        {P, P, P, P, P, P, P, P, P, P},
        {P, P, P, P, P, P, P, P, P, P},
    });
    assertEquals("P", getInnerText("-players-table-1-4"));
    click("-players-Unfilled");
    assertContains("gwt-ToggleButton-down",
        ensureDebugIdAndGetElement("-players-Unfilled", true).getClassName());
    assertEquals("C", getInnerText("-players-table-1-4"));
  }

  public void testPositionOverrideCheckboxes() {
    login(1);
    click("-players-override-P-checkbox");
    assertEquals("C", getInnerText("-players-table-1-4"));

    click("-players-All");
    assertFalse(isVisible("-players-override-P"));

    click("-players-Unfilled");
    simulateDraftStatus(new Position[][]{
        {P, P, P, P, P, P, P, P, P, P},
        {P, P, P, P, P, P, P, P, P, P},
        {P, P, P, P, P, P, P, P, P, P},
        {P, P, P, P, P, P, P, P, P, P},
        {P, P, P, P, P, P, P, P, P, P},
        {P, P, P, P, P, P, P, P, P, P},
        {P, P, P, P, P, P, P, P, P, P},
    });
    assertFalse(isVisible("-players-override-P"));

    click("-players-override-C-checkbox");
    assertEquals("1B", getInnerText("-players-table-1-4"));
  }

  public void testNameSearch() {
    login(1);
    assertFalse(isVisible("-players-clear"));
    type("-players-search", "1");
    for (int i = 1; i < 40; i++) {
      assertContains("1", getInnerText("-players-table-" + i + "-1"));
    }
    assertTrue(isVisible("-players-clear"));
    type("-players-search", "11");
    for (int i = 1; i < 23; i++) {
      assertContains("11", getInnerText("-players-table-" + i + "-1"));
    }
    click("-players-clear");
    assertEquals("",
        InputElement.as(ensureDebugIdAndGetElement("-players-search", true)).getValue());
    assertEquals("0000000000", getInnerText("-players-table-1-1"));
    assertFalse(isVisible("-players-clear"));
  }

  @SuppressWarnings("ConstantConditions")
  public void testCopyRanksCheckbox() {
    login(1);
    ginjector.getScheduler().flush();
    click(Scoring.CATEGORIES ? "-players-table-0-13" : "-players-table-0-17");
    click("-players-autopickSettings");
    assertFalse(isEnabled("-players-copyRanks"));
    click(Scoring.CATEGORIES ? "-players-table-0-15" : "-players-table-0-19");
    click("-players-autopickSettings");
    assertFalse(isEnabled("-players-copyRanks"));

    click("-players-table-0-1");
    click("-players-autopickSettings");
    click("-players-copyRanks");
    for (int i = 1; i < 40; i++) {
      assertEquals(Integer.toString(i), getInnerText("-players-table-" + i + (Scoring.CATEGORIES ? "-15" : "-19")));
    }
  }

  public void testAutoPickWizardCheckbox() {
    login(1);
    click("-players-autopickSettings");
    assertFalse(isEnabled("-players-autopick"));
    click("-players-table-0-" + (Scoring.CATEGORIES ? "13" : "17"));
    click("-players-autopickSettings");
    assertTrue(isEnabled("-players-autopick"));

    click("-players-GURU");
    click("-players-autopickSettings");
    assertFalse(isChecked("-players-autopick"));
    click("-players-autopickSettings");
    click("-players-autopick");
    assertTrue(isChecked("-players-autopick"));

    click("-players-Averages");
    click("-players-autopickSettings");
    assertFalse(isChecked("-players-autopick"));
    click("-players-GURU");
    click("-players-autopickSettings");
    assertTrue(isChecked("-players-autopick"));
  }

  public void testHideInjuriesCheckbox() {
    login(1);
    assertEquals("5555555555", getInnerText("-players-table-6-1"));
    click("-players-hideInjuries");
    assertEquals("6666666666", getInnerText("-players-table-6-1"));
  }

  public void testPageControls() {
    login(1);

    click("-players-nextPage");
    assertEquals("40404040404040404040", getInnerText("-players-table-1-1"));

    click("-players-prevPage");
    assertEquals("0000000000", getInnerText("-players-table-1-1"));

    click("-players-nextPage");
    click("-players-nextPage");
    click("-players-nextPage");
    click("-players-nextPage");
    click("-players-nextPage");
    click("-players-firstPage");
    assertEquals("0000000000", getInnerText("-players-table-1-1"));
  }
}