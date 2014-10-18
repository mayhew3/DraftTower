package com.mayhew3.drafttower.client.teamorder;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mayhew3.drafttower.client.TestBase;
import com.mayhew3.drafttower.shared.DraftPick;
import com.mayhew3.drafttower.shared.DraftStatus;
import com.mayhew3.drafttower.shared.DraftStatusTestUtil;
import com.mayhew3.drafttower.shared.Position;

import static com.mayhew3.drafttower.shared.Position.P;

/**
 * GWT tests for team order widget.
 */
public class TeamOrderWidgetGwtTest extends TestBase {

  public void testStartOfDraft() {
    login(1);
    simulateDraftStart();
    assertEquals("Round 1", getInnerText("-teamOrder-round"));
    assertEquals("Your pick!", getInnerText("-teamOrder-status"));
    assertEquals("1", getInnerText("-teamOrder-0"));
    assertEquals("2", getInnerText("-teamOrder-1"));
    assertEquals("3", getInnerText("-teamOrder-2"));
    assertEquals("4", getInnerText("-teamOrder-3"));
    assertEquals("5", getInnerText("-teamOrder-4"));
    assertEquals("6", getInnerText("-teamOrder-5"));
    assertEquals("7", getInnerText("-teamOrder-6"));
    assertEquals("8", getInnerText("-teamOrder-7"));
    assertEquals("9", getInnerText("-teamOrder-8"));
    assertEquals("10", getInnerText("-teamOrder-9"));
  }

  public void testOnDeck() {
    login(2);
    simulateDraftStart();
    assertEquals("On deck!", getInnerText("-teamOrder-status"));
  }

  public void testNoStatus() {
    login(3);
    simulateDraftStart();
    assertEquals("", getInnerText("-teamOrder-status"));
  }

  public void testRound2() {
    login(1);
    simulateDraftStatus(new Position[][] {
        {P}, {P}, {P}, {P}, {P}, {P}, {P}, {P}, {P}, {P},
    });
    assertEquals("Round 2", getInnerHTML("-teamOrder-round"));
  }

  public void testTeamStatuses() {
    login(1);
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(
        Lists.<DraftPick>newArrayList(), ginjector.getBeanFactory());
    draftStatus.setConnectedTeams(Sets.newHashSet(1, 3, 5, 6));
    draftStatus.setRobotTeams(Sets.newHashSet(3, 5));
    draftStatus.setNextPickKeeperTeams(Sets.newHashSet(4, 5, 6));
    simulateDraftStatus(draftStatus);

    assertTrue(hasStyle("-teamOrder-0", TeamOrderWidget.CSS.me()));
    assertFalse(hasStyle("-teamOrder-1", TeamOrderWidget.CSS.me()));

    assertTrue(hasStyle("-teamOrder-0", TeamOrderWidget.CSS.currentPick()));
    assertFalse(hasStyle("-teamOrder-1", TeamOrderWidget.CSS.currentPick()));

    assertFalse(hasStyle("-teamOrder-0", TeamOrderWidget.CSS.disconnected()));
    assertTrue(hasStyle("-teamOrder-1", TeamOrderWidget.CSS.disconnected()));
    assertFalse(hasStyle("-teamOrder-2", TeamOrderWidget.CSS.disconnected()));

    assertFalse(hasStyle("-teamOrder-1", TeamOrderWidget.CSS.robot()));
    assertTrue(hasStyle("-teamOrder-2", TeamOrderWidget.CSS.robot()));
    assertTrue(hasStyle("-teamOrder-4", TeamOrderWidget.CSS.robot()));

    assertFalse(hasStyle("-teamOrder-2", TeamOrderWidget.CSS.keeper()));
    assertTrue(hasStyle("-teamOrder-3", TeamOrderWidget.CSS.keeper()));
    assertTrue(hasStyle("-teamOrder-4", TeamOrderWidget.CSS.keeper()));
    assertTrue(hasStyle("-teamOrder-5", TeamOrderWidget.CSS.keeper()));

    simulateDraftStatus(new Position[][] {
        {P}, {P}, {P}, {P},
    });

    assertFalse(hasStyle("-teamOrder-0", TeamOrderWidget.CSS.currentPick()));
    assertTrue(hasStyle("-teamOrder-4", TeamOrderWidget.CSS.currentPick()));
  }

  public void testItsOver() {
    login(1);
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(
        Lists.<DraftPick>newArrayList(), ginjector.getBeanFactory());
    draftStatus.setOver(true);
    simulateDraftStatus(draftStatus);
    assertEquals("It's over!", getInnerText("-teamOrder-round"));
    assertEquals("", getInnerText("-teamOrder-status"));
  }
}