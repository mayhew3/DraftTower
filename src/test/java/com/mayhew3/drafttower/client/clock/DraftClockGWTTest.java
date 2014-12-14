package com.mayhew3.drafttower.client.clock;

import com.google.common.collect.Lists;
import com.mayhew3.drafttower.client.TestBase;
import com.mayhew3.drafttower.shared.DraftPick;
import com.mayhew3.drafttower.shared.DraftStatus;
import com.mayhew3.drafttower.shared.DraftStatusTestUtil;

/**
 * Tests draft clock widget.
 */
public class DraftClockGwtTest extends TestBase {

  public void testScheduledTaskAdvancesClock() {
    login(1);
    ginjector.getCurrentTimeProvider().setCurrentTimeMillis(0);
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(
        Lists.<DraftPick>newArrayList(), ginjector.getBeanFactory());
    draftStatus.setCurrentPickDeadline(60000);
    simulateDraftStatus(draftStatus);
    assertEquals("1:00", getInnerText("-clock-display"));

    ginjector.getCurrentTimeProvider().setCurrentTimeMillis(1000);
    ginjector.getScheduler().runRepeating();
    assertEquals("0:59", getInnerText("-clock-display"));
  }

  public void testClockClearedOnEndOfDraft() {
    login(1);
    ginjector.getCurrentTimeProvider().setCurrentTimeMillis(0);
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(
        Lists.<DraftPick>newArrayList(), ginjector.getBeanFactory());
    draftStatus.setCurrentPickDeadline(60000);
    simulateDraftStatus(draftStatus);
    assertEquals("1:00", getInnerText("-clock-display"));

    draftStatus.setOver(true);
    simulateDraftStatus(draftStatus);
    assertTrue(getInnerText("-clock-display").trim().isEmpty());
  }

  public void testClockClearedOnDisconnect() {
    login(1);
    ginjector.getCurrentTimeProvider().setCurrentTimeMillis(0);
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(
        Lists.<DraftPick>newArrayList(), ginjector.getBeanFactory());
    draftStatus.setCurrentPickDeadline(60000);
    simulateDraftStatus(draftStatus);
    assertEquals("1:00", getInnerText("-clock-display"));

    ginjector.getWebSocket().close();
    assertTrue(getInnerText("-clock-display").trim().isEmpty());
  }

  public void testPlayPauseVisibleForCommissioner() {
    login(1);
    assertTrue(isVisible("-clock-playPause"));
  }

  public void testPlayPauseInvisibleForNonCommissioner() {
    login(2);
    assertFalse(isVisible("-clock-playPause"));
  }

  public void testPlayButtonToStartDraft() {
    login(1);
    assertTrue(getInnerText("-clock-display").trim().isEmpty());
    assertEquals("▸", getInnerText("-clock-playPause"));
    click("-clock-playPause");
    assertEquals("1:15", getInnerText("-clock-display"));
    assertEquals("❙❙", getInnerText("-clock-playPause"));
    assertFalse(ginjector.getDraftStatus().isPaused());
  }

  public void testPauseButton() {
    login(1);
    ginjector.getCurrentTimeProvider().setCurrentTimeMillis(0);
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(
        Lists.<DraftPick>newArrayList(), ginjector.getBeanFactory());
    draftStatus.setCurrentPickDeadline(60000);
    simulateDraftStatus(draftStatus);
    assertEquals("❙❙", getInnerText("-clock-playPause"));
    click("-clock-playPause");
    assertTrue(ginjector.getDraftStatus().isPaused());
    assertEquals("▸", getInnerText("-clock-playPause"));
  }

  public void testPlayButtonToResume() {
    login(1);
    // Current time used to set deadline on resume, so it can't be zero.
    ginjector.getCurrentTimeProvider().setCurrentTimeMillis(1000);
    DraftStatus draftStatus = DraftStatusTestUtil.createDraftStatus(
        Lists.<DraftPick>newArrayList(), ginjector.getBeanFactory());
    draftStatus.setCurrentPickDeadline(60000);
    draftStatus.setPaused(true);
    simulateDraftStatus(draftStatus);
    assertEquals("▸", getInnerText("-clock-playPause"));
    click("-clock-playPause");
    assertFalse(ginjector.getDraftStatus().isPaused());
    assertEquals("❙❙", getInnerText("-clock-playPause"));
  }
}