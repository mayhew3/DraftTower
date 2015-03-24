package com.mayhew3.drafttower.client.players.unclaimed;

import com.google.gwt.dom.client.IFrameElement;
import com.mayhew3.drafttower.client.TestBase;
import com.mayhew3.drafttower.shared.Position;
import com.mayhew3.drafttower.shared.Scoring;

import static com.mayhew3.drafttower.shared.Position.P;

/**
 * GWT test for main table widget.
 */
@SuppressWarnings("ConstantConditions")
public class UnclaimedPlayerTableGwtTest extends TestBase {

  public void testInjuryColumn() {
    login(1);
    assertEquals("+", getInnerText("-players-table-6-0"));
    assertContains("busted wang", getInnerHTML("-players-table-6-0"));
  }

  public void testNewsPopup() {
    login(1);
    click("-players-table-1-2");
    assertTrue(isVisible("-playerPopup"));
    IFrameElement popup = IFrameElement.as(
        ensureDebugIdAndGetElement("-playerPopup", true));
    assertContains("#0", popup.getSrc());
  }

  public void testDragPlayerAboveFirstEntry() {
    login(1);
    dragToTop("-players-table-2", "-players-table-1");
    assertEquals("1111111111", getInnerText("-players-table-1-1"));
    assertEquals("0000000000", getInnerText("-players-table-2-1"));
  }

  public void testDragPlayerBelowLastEntry() {
    login(1);
    dragToBottom("-players-table-2", "-players-table-40");
    assertEquals("1111111111", getInnerText("-players-table-40-1"));
    assertEquals("39393939393939393939", getInnerText("-players-table-39-1"));
  }

  public void testReorderDownToTop() {
    login(1);
    dragToTop("-players-table-1", "-players-table-3");
    assertEquals("1111111111", getInnerText("-players-table-1-1"));
    assertEquals("0000000000", getInnerText("-players-table-2-1"));
    assertEquals("2222222222", getInnerText("-players-table-3-1"));
  }

  public void testReorderDownToBottom() {
    login(1);
    dragToBottom("-players-table-1", "-players-table-3");
    assertEquals("1111111111", getInnerText("-players-table-1-1"));
    assertEquals("2222222222", getInnerText("-players-table-2-1"));
    assertEquals("0000000000", getInnerText("-players-table-3-1"));
  }

  public void testReorderUpToBottom() {
    login(1);
    dragToBottom("-players-table-3", "-players-table-1");
    assertEquals("0000000000", getInnerText("-players-table-1-1"));
    assertEquals("2222222222", getInnerText("-players-table-2-1"));
    assertEquals("1111111111", getInnerText("-players-table-3-1"));
  }

  public void testReorderUpToTop() {
    login(1);
    dragToTop("-players-table-3", "-players-table-1");
    assertEquals("2222222222", getInnerText("-players-table-1-1"));
    assertEquals("0000000000", getInnerText("-players-table-2-1"));
    assertEquals("1111111111", getInnerText("-players-table-3-1"));
  }

  public void testReorderDownNoOp() {
    login(1);
    dragToTop("-players-table-1", "-players-table-2");
    assertEquals("0000000000", getInnerText("-players-table-1-1"));
    assertEquals("1111111111", getInnerText("-players-table-2-1"));
    assertEquals("2222222222", getInnerText("-players-table-3-1"));
  }

  public void testReorderUpNoOp() {
    login(1);
    dragToBottom("-players-table-3", "-players-table-2");
    assertEquals("0000000000", getInnerText("-players-table-1-1"));
    assertEquals("1111111111", getInnerText("-players-table-2-1"));
    assertEquals("2222222222", getInnerText("-players-table-3-1"));
  }

  public void testNotDroppableWhenNotSortedByRank() {
    login(1);
    ginjector.getScheduler().flush();
    click("-players-table-0-1");
    assertEquals("0000000000", getInnerText("-players-table-1-1"));
    assertEquals("100100100100100100100100100100", getInnerText("-players-table-2-1"));
    dragToTop("-players-table-1", "-players-table-3");
    assertEquals("0000000000", getInnerText("-players-table-1-1"));
    assertEquals("100100100100100100100100100100", getInnerText("-players-table-2-1"));
  }

  public void testSortByName() {
    login(1);
    ginjector.getScheduler().flush();
    click("-players-table-0-1");
    assertEquals("0000000000", getInnerText("-players-table-1-1"));
    assertEquals("100100100100100100100100100100", getInnerText("-players-table-2-1"));
    click("-players-table-0-1");
    assertEquals("99999999999999999999", getInnerText("-players-table-1-1"));
    assertEquals("9999999999", getInnerText("-players-table-2-1"));
  }

  public void testSortByTeam() {
    login(1);
    ginjector.getScheduler().flush();
    click("-players-table-0-3");
    assertEquals("XXX0", getInnerText("-players-table-1-3"));
    assertEquals("XXX0", getInnerText("-players-table-2-3"));
    click("-players-table-0-3");
    assertEquals("XXX9", getInnerText("-players-table-1-3"));
    assertEquals("XXX9", getInnerText("-players-table-2-3"));
  }

  public void testSortByElig() {
    login(1);
    ginjector.getScheduler().flush();
    click("-players-table-0-4");
    assertEquals("1B", getInnerText("-players-table-1-4"));
    assertEquals("1B", getInnerText("-players-table-2-4"));
    click("-players-table-0-4");
    assertEquals("SS", getInnerText("-players-table-1-4"));
    assertEquals("SS", getInnerText("-players-table-2-4"));
  }

  public void testSortByG_AB() {
    if (Scoring.POINTS) {
      return;
    }
    login(1);
    ginjector.getScheduler().flush();
    click("-players-table-0-5");
    assertEquals("880", getInnerText("-players-table-1-5"));
    assertEquals("860", getInnerText("-players-table-2-5"));
    click("-players-table-0-5");
    assertEquals("28", getInnerText("-players-table-1-5"));
    assertEquals("28", getInnerText("-players-table-2-5"));
  }

  public void testSortByG() {
    if (Scoring.CATEGORIES) {
      return;
    }
    login(1);
    ginjector.getScheduler().flush();
    click("-players-table-0-5");
    assertEquals("132", getInnerText("-players-table-1-5"));
    assertEquals("129", getInnerText("-players-table-2-5"));
    click("-players-table-0-5");
    assertEquals("0", getInnerText("-players-table-1-5"));
    assertEquals("0", getInnerText("-players-table-2-5"));
  }

  public void testSortByHR_INN() {
    if (Scoring.POINTS) {
      return;
    }
    login(1);
    ginjector.getScheduler().flush();
    click("-players-table-0-6");
    assertEquals("44", getInnerText("-players-table-1-6"));
    assertEquals("43", getInnerText("-players-table-2-6"));
    click("-players-table-0-6");
    assertEquals("149", getInnerText("-players-table-1-6"));
    assertEquals("148", getInnerText("-players-table-2-6"));
  }

  public void testSortByGS_AB() {
    if (Scoring.CATEGORIES) {
      return;
    }
    login(1);
    ginjector.getScheduler().flush();
    click("-players-table-0-6");
    assertEquals("880", getInnerText("-players-table-1-6"));
    assertEquals("860", getInnerText("-players-table-2-6"));
    click("-players-table-0-6");
    assertEquals("28", getInnerText("-players-table-1-6"));
    assertEquals("28", getInnerText("-players-table-2-6"));
  }

  public void testSortByRBI_K() {
    if (Scoring.POINTS) {
      return;
    }
    login(1);
    ginjector.getScheduler().flush();
    click("-players-table-0-7");
    assertEquals("132", getInnerText("-players-table-1-7"));
    assertEquals("129", getInnerText("-players-table-2-7"));
    click("-players-table-0-7");
    assertEquals("144", getInnerText("-players-table-1-7"));
    assertEquals("143", getInnerText("-players-table-2-7"));
  }

  public void testSortByINN_BA() {
    if (Scoring.CATEGORIES) {
      return;
    }
    login(1);
    ginjector.getScheduler().flush();
    click("-players-table-0-7");
    assertEquals("0.64", getInnerText("-players-table-1-7"));
    assertEquals("0.63", getInnerText("-players-table-2-7"));
    click("-players-table-0-7");
    assertEquals("149", getInnerText("-players-table-1-7"));
    assertEquals("148", getInnerText("-players-table-2-7"));
  }

  public void testSortByOBP_ERA() {
    if (Scoring.POINTS) {
      return;
    }
    login(1);
    ginjector.getScheduler().flush();
    click("-players-table-0-8");
    assertEquals("0.69", getInnerText("-players-table-1-8"));
    assertEquals("0.68", getInnerText("-players-table-2-8"));
    click("-players-table-0-8");
    assertEquals("2.0", getInnerText("-players-table-1-8"));
    assertEquals("2.01", getInnerText("-players-table-2-8"));
  }

  public void testSortByOBP_WHIP() {
    if (Scoring.CATEGORIES) {
      return;
    }
    login(1);
    ginjector.getScheduler().flush();
    click("-players-table-0-8");
    assertEquals("0.69", getInnerText("-players-table-1-8"));
    assertEquals("0.68", getInnerText("-players-table-2-8"));
    click("-players-table-0-8");
    assertEquals("1.0", getInnerText("-players-table-1-8"));
    assertEquals("1.01", getInnerText("-players-table-2-8"));
  }

  public void testSortBySLG_WHIP() {
    if (Scoring.POINTS) {
      return;
    }
    login(1);
    ginjector.getScheduler().flush();
    click("-players-table-0-9");
    assertEquals("1.28", getInnerText("-players-table-1-9"));
    assertEquals("1.26", getInnerText("-players-table-2-9"));
    click("-players-table-0-9");
    assertEquals("1.0", getInnerText("-players-table-1-9"));
    assertEquals("1.01", getInnerText("-players-table-2-9"));
  }

  public void testSortBySLG_ERA() {
    if (Scoring.CATEGORIES) {
      return;
    }
    login(1);
    ginjector.getScheduler().flush();
    click("-players-table-0-9");
    assertEquals("1.28", getInnerText("-players-table-1-9"));
    assertEquals("1.26", getInnerText("-players-table-2-9"));
    click("-players-table-0-9");
    assertEquals("2.0", getInnerText("-players-table-1-9"));
    assertEquals("2.01", getInnerText("-players-table-2-9"));
  }

  public void testSortByRHR_WL() {
    if (Scoring.POINTS) {
      return;
    }
    login(1);
    ginjector.getScheduler().flush();
    click("-players-table-0-10");
    assertEquals("132", getInnerText("-players-table-1-10"));
    assertEquals("129", getInnerText("-players-table-2-10"));
    click("-players-table-0-10");
    assertEquals("139", getInnerText("-players-table-1-10"));
    assertEquals("138", getInnerText("-players-table-2-10"));
  }

  public void testSortByH_HA() {
    if (Scoring.CATEGORIES) {
      return;
    }
    login(1);
    ginjector.getScheduler().flush();
    click("-players-table-0-10");
    assertEquals("220", getInnerText("-players-table-1-10"));
    assertEquals("215", getInnerText("-players-table-2-10"));
    click("-players-table-0-10");
    assertEquals("0", getInnerText("-players-table-1-10"));
    assertEquals("2", getInnerText("-players-table-2-10"));
  }

  public void testSortBySBCS_S() {
    if (Scoring.POINTS) {
      return;
    }
    login(1);
    ginjector.getScheduler().flush();
    click("-players-table-0-11");
    assertEquals("44", getInnerText("-players-table-1-11"));
    assertEquals("43", getInnerText("-players-table-2-11"));
    click("-players-table-0-11");
    assertEquals("139", getInnerText("-players-table-1-11"));
    assertEquals("138", getInnerText("-players-table-2-11"));
  }

  public void testSortByHR_HRA() {
    if (Scoring.CATEGORIES) {
      return;
    }
    login(1);
    ginjector.getScheduler().flush();
    click("-players-table-0-11");
    assertEquals("44", getInnerText("-players-table-1-11"));
    assertEquals("43", getInnerText("-players-table-2-11"));
    click("-players-table-0-11");
    assertEquals("0", getInnerText("-players-table-1-11"));
    assertEquals("0", getInnerText("-players-table-2-11"));
  }

  public void testSortByRBI_W() {
    if (Scoring.CATEGORIES) {
      return;
    }
    login(1);
    ginjector.getScheduler().flush();
    click("-players-table-0-12");
    assertEquals("132", getInnerText("-players-table-1-12"));
    assertEquals("129", getInnerText("-players-table-2-12"));
    click("-players-table-0-12");
    assertEquals("23", getInnerText("-players-table-1-12"));
    assertEquals("23", getInnerText("-players-table-2-12"));
  }

  public void testSortByR_L() {
    if (Scoring.CATEGORIES) {
      return;
    }
    login(1);
    ginjector.getScheduler().flush();
    click("-players-table-0-13");
    assertEquals("132", getInnerText("-players-table-1-13"));
    assertEquals("129", getInnerText("-players-table-2-13"));
    click("-players-table-0-13");
    assertEquals("0", getInnerText("-players-table-1-13"));
    assertEquals("0", getInnerText("-players-table-2-13"));
  }

  public void testSortByBB_BBI() {
    if (Scoring.CATEGORIES) {
      return;
    }
    login(1);
    ginjector.getScheduler().flush();
    click("-players-table-0-14");
    assertEquals("88", getInnerText("-players-table-1-14"));
    assertEquals("86", getInnerText("-players-table-2-14"));
    click("-players-table-0-14");
    assertEquals("0", getInnerText("-players-table-1-14"));
    assertEquals("1", getInnerText("-players-table-2-14"));
  }

  public void testSortByKO_K() {
    if (Scoring.CATEGORIES) {
      return;
    }
    login(1);
    ginjector.getScheduler().flush();
    click("-players-table-0-15");
    assertEquals("5", getInnerText("-players-table-1-15"));
    assertEquals("5", getInnerText("-players-table-2-15"));
    click("-players-table-0-15");
    assertEquals("144", getInnerText("-players-table-1-15"));
    assertEquals("143", getInnerText("-players-table-2-15"));
  }

  public void testSortBySB_S() {
    if (Scoring.CATEGORIES) {
      return;
    }
    login(1);
    ginjector.getScheduler().flush();
    click("-players-table-0-16");
    assertEquals("44", getInnerText("-players-table-1-16"));
    assertEquals("43", getInnerText("-players-table-2-16"));
    click("-players-table-0-16");
    assertEquals("47", getInnerText("-players-table-1-16"));
    assertEquals("46", getInnerText("-players-table-2-16"));
  }

  public void testSortByPTS() {
    if (Scoring.CATEGORIES) {
      return;
    }
    login(1);
    ginjector.getScheduler().flush();
    click("-players-table-0-17");
    assertEquals("2780", getInnerText("-players-table-1-17"));
    assertEquals("2760", getInnerText("-players-table-2-17"));
    click("-players-table-0-17");
    assertEquals("0", getInnerText("-players-table-1-17"));
    assertEquals("0", getInnerText("-players-table-2-17"));
  }

  public void testSortByRank() {
    login(1);
    ginjector.getScheduler().flush();
    String column = Scoring.CATEGORIES ? "12" : "18";
    click("-players-table-0-" + column);
    assertEquals("1", getInnerText("-players-table-1-" + column));
    assertEquals("2", getInnerText("-players-table-2-" + column));
    click("-players-table-0-" + column);
    assertEquals("265", getInnerText("-players-table-1-" + column));
    assertEquals("264", getInnerText("-players-table-2-" + column));
  }

  public void testSortByWizard() {
    login(1);
    ginjector.getScheduler().flush();
    String column = Scoring.CATEGORIES ? "13" : "19";
    click("-players-table-0-" + column);
    assertEquals("0.45", getInnerText("-players-table-1-" + column));
    assertEquals("0.45", getInnerText("-players-table-2-" + column));
    click("-players-table-0-" + column);
    assertEquals("0.0", getInnerText("-players-table-1-" + column));
    assertEquals("0.0", getInnerText("-players-table-2-" + column));
  }

  public void testSortByDraft() {
    login(1);
    ginjector.getScheduler().flush();
    String column = Scoring.CATEGORIES ? "14" : "20";
    click("-players-table-0-" + column);
    assertEquals("1", getInnerText("-players-table-1-" + column));
    assertEquals("2", getInnerText("-players-table-2-" + column));
    click("-players-table-0-" + column);
    assertEquals("265", getInnerText("-players-table-1-" + column));
    assertEquals("264", getInnerText("-players-table-2-" + column));
  }

  public void testSortByMyRank() {
    login(1);
    ginjector.getScheduler().flush();
    String column = Scoring.CATEGORIES ? "15" : "21";
    assertEquals("1", getInnerText("-players-table-1-" + column));
    assertEquals("2", getInnerText("-players-table-2-" + column));
    click("-players-table-0-" + column);
    assertEquals("265", getInnerText("-players-table-1-" + column));
    assertEquals("264", getInnerText("-players-table-2-" + column));
  }

  public void testPositionFiltersUpdateColumnNames() {
    login(1);
    ginjector.getScheduler().flush();
    if (Scoring.CATEGORIES) {
      assertEquals("AB/G", getInnerText("-players-table-0-5"));
    } else {
      assertEquals("AB/GS", getInnerText("-players-table-0-6"));
    }

    click("-players-P");
    ginjector.getScheduler().flush();
    if (Scoring.CATEGORIES) {
      assertEquals("G", getInnerText("-players-table-0-5"));
    } else {
      assertEquals("GS", getInnerText("-players-table-0-6"));
    }

    click("-players-SS");
    ginjector.getScheduler().flush();
    if (Scoring.CATEGORIES) {
      assertEquals("AB", getInnerText("-players-table-0-5"));
    } else {
      assertEquals("AB", getInnerText("-players-table-0-6"));
    }

    simulateDraftStatus(new Position[][]{
        {P, P, P, P, P, P, P, P, P, P},
        {P, P, P, P, P, P, P, P, P, P},
        {P, P, P, P, P, P, P, P, P, P},
        {P, P, P, P, P, P, P, P, P, P},
        {P, P, P, P, P, P, P, P, P, P},
        {P, P, P, P, P, P, P, P, P, P},
        {P, P, P, P, P, P, P, P, P, P},
    });
    click("-players-Unfilled");
    ginjector.getScheduler().flush();
    if (Scoring.CATEGORIES) {
      assertEquals("AB", getInnerText("-players-table-0-5"));
    } else {
      assertEquals("AB", getInnerText("-players-table-0-6"));
    }
  }
}