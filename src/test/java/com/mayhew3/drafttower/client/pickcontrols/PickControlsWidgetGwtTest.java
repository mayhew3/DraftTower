package com.mayhew3.drafttower.client.pickcontrols;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mayhew3.drafttower.client.TestBase;
import com.mayhew3.drafttower.shared.DraftPick;
import com.mayhew3.drafttower.shared.DraftStatus;
import com.mayhew3.drafttower.shared.DraftStatusTestUtil;

/**
 * GWT test for pick controls.
 */
public class PickControlsWidgetGwtTest extends TestBase {

  public void testPickDisabledDraftNotStarted() {
    login(1);
    assertFalse(isEnabled("-pickControls-pick"));
  }

  public void testPickDisabledNoPlayerSelected() {
    login(1);
    simulateDraftStart();
    assertFalse(isEnabled("-pickControls-pick"));
  }

  public void testPickDisabledNotMyTurn() {
    login(1);
    simulateDraftStatus(DraftStatusTestUtil.createDraftStatus(
        Lists.newArrayList(
            DraftStatusTestUtil.createDraftPick(1, "", false, ginjector.getBeanFactory())),
        ginjector.getBeanFactory()));
    assertFalse(isEnabled("-pickControls-pick"));
  }

  public void testPick() {
    login(1);
    simulateDraftStart();
    selectTableRow("-players-table", 1);
    assertTrue(isEnabled("-pickControls-pick"));
    click("-pickControls-pick");
    assertEquals(1, ginjector.getDraftStatus().getPicks().size());
    assertEquals(0, ginjector.getDraftStatus().getPicks().get(0).getPlayerId());
  }

  public void testEnqueueDisabledNoPlayerSelected() {
    login(1);
    assertFalse(isEnabled("-pickControls-enqueue"));
  }

  public void testEnqueue() {
    login(1);
    selectTableRow("-players-table", 1);
    assertTrue(isEnabled("-pickControls-enqueue"));
    click("-pickControls-enqueue");
    assertEquals("0000000000", getInnerText("-queue-1-1"));

    // Check button disabled when player already queued.
    selectTableRow("-players-table", 1);
    assertFalse(isEnabled("-pickControls-enqueue"));
  }

  public void testForcePickDisabledDraftNotStarted() {
    login(1);
    assertTrue(isVisible("-pickControls-force"));
    assertFalse(isEnabled("-pickControls-force"));
  }

  public void testForcePickInvisibleNotCommish() {
    login(2);
    simulateDraftStart();
    assertFalse(isVisible("-pickControls-force"));
  }

  public void testForcePick() {
    login(1);
    simulateDraftStatus(DraftStatusTestUtil.createDraftStatus(
        Lists.<DraftPick>newArrayList(
            DraftStatusTestUtil.createDraftPick(1, "", false, ginjector.getBeanFactory())),
        ginjector.getBeanFactory()));
    assertTrue(isEnabled("-pickControls-force"));
    click("-pickControls-force");
    assertEquals(2, ginjector.getDraftStatus().getPicks().size());
  }

  public void testWakeUpInvisibleNotRobot() {
    login(1);
    assertFalse(isVisible("-pickControls-wakeUp"));
  }

  public void testWakeUp() {
    login(1);
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(
        Lists.<DraftPick>newArrayList(), ginjector.getBeanFactory());
    draftStatus.setRobotTeams(Sets.newHashSet(1));
    simulateDraftStatus(draftStatus);
    assertTrue(isEnabled("-pickControls-wakeUp"));
    click("-pickControls-wakeUp");
    assertFalse(ginjector.getDraftStatus().getRobotTeams().contains(1));
  }
}