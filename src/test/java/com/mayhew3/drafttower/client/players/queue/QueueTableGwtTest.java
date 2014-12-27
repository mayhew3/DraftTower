package com.mayhew3.drafttower.client.players.queue;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.KeyCodes;
import com.mayhew3.drafttower.client.TestBase;
import com.mayhew3.drafttower.server.TestPlayerDataSource;
import com.mayhew3.drafttower.shared.DraftStatusTestUtil;
import com.mayhew3.drafttower.shared.Position;

/**
 * GWT test for queue table widget.
 */
public class QueueTableGwtTest extends TestBase {

  public void testDragPlayerToEmptyQueueTop() {
    login(1);
    dragToTop("-players-table-1", "-queue-1");
    assertEquals("0000000000", getInnerText("-queue-1-1"));
  }

  public void testDragPlayerToEmptyQueueBottom() {
    login(1);
    dragToBottom("-players-table-1", "-queue-1");
    assertEquals("0000000000", getInnerText("-queue-1-1"));
  }

  public void testDragPlayerAboveFirstEntry() {
    login(1);
    dragToTop("-players-table-1", "-queue-1");
    dragToTop("-players-table-2", "-queue-1");
    assertEquals("1111111111", getInnerText("-queue-1-1"));
    assertEquals("0000000000", getInnerText("-queue-2-1"));
  }

  public void testDragPlayerBelowLastEntry() {
    login(1);
    dragToBottom("-players-table-1", "-queue-1");
    dragToBottom("-players-table-2", "-queue-1");
    assertEquals("0000000000", getInnerText("-queue-1-1"));
    assertEquals("1111111111", getInnerText("-queue-2-1"));
  }

  public void testReorderDownToTop() {
    login(1);
    dragToBottom("-players-table-1", "-queue-1");
    dragToBottom("-players-table-2", "-queue-1");
    dragToBottom("-players-table-3", "-queue-2");

    dragToTop("-queue-1", "-queue-3");
    assertEquals("1111111111", getInnerText("-queue-1-1"));
    assertEquals("0000000000", getInnerText("-queue-2-1"));
    assertEquals("2222222222", getInnerText("-queue-3-1"));
  }

  public void testReorderDownToBottom() {
    login(1);
    dragToBottom("-players-table-1", "-queue-1");
    dragToBottom("-players-table-2", "-queue-1");
    dragToBottom("-players-table-3", "-queue-2");

    dragToBottom("-queue-1", "-queue-3");
    assertEquals("1111111111", getInnerText("-queue-1-1"));
    assertEquals("2222222222", getInnerText("-queue-2-1"));
    assertEquals("0000000000", getInnerText("-queue-3-1"));
  }

  public void testReorderUpToBottom() {
    login(1);
    dragToBottom("-players-table-1", "-queue-1");
    dragToBottom("-players-table-2", "-queue-1");
    dragToBottom("-players-table-3", "-queue-2");

    dragToBottom("-queue-3", "-queue-1");
    assertEquals("0000000000", getInnerText("-queue-1-1"));
    assertEquals("2222222222", getInnerText("-queue-2-1"));
    assertEquals("1111111111", getInnerText("-queue-3-1"));
  }

  public void testReorderUpToTop() {
    login(1);
    dragToBottom("-players-table-1", "-queue-1");
    dragToBottom("-players-table-2", "-queue-1");
    dragToBottom("-players-table-3", "-queue-2");

    dragToTop("-queue-3", "-queue-1");
    assertEquals("2222222222", getInnerText("-queue-1-1"));
    assertEquals("0000000000", getInnerText("-queue-2-1"));
    assertEquals("1111111111", getInnerText("-queue-3-1"));
  }

  public void testReorderDownNoOp() {
    login(1);
    dragToBottom("-players-table-1", "-queue-1");
    dragToBottom("-players-table-2", "-queue-1");
    dragToBottom("-players-table-3", "-queue-2");

    dragToTop("-queue-1", "-queue-2");
    assertEquals("0000000000", getInnerText("-queue-1-1"));
    assertEquals("1111111111", getInnerText("-queue-2-1"));
    assertEquals("2222222222", getInnerText("-queue-3-1"));
  }

  public void testReorderUpNoOp() {
    login(1);
    dragToBottom("-players-table-1", "-queue-1");
    dragToBottom("-players-table-2", "-queue-1");
    dragToBottom("-players-table-3", "-queue-2");

    dragToBottom("-queue-3", "-queue-2");
    assertEquals("0000000000", getInnerText("-queue-1-1"));
    assertEquals("1111111111", getInnerText("-queue-2-1"));
    assertEquals("2222222222", getInnerText("-queue-3-1"));
  }

  public void testRemove() {
    login(1);
    dragToBottom("-players-table-1", "-queue-1");
    dragToBottom("-players-table-2", "-queue-1");
    dragToBottom("-players-table-3", "-queue-2");

    click("-queue-2-3-button");
    assertEquals("0000000000", getInnerText("-queue-1-1"));
    assertEquals("2222222222", getInnerText("-queue-2-1"));
  }

  public void testCancelDragWithEsc() {
    login(1);
    startDrag("-players-table-1");
    pressKey("-players", KeyCodes.KEY_ESCAPE);
    finishDragToBottom("-players-table-1", "-queue-1");
    assertEquals("Drag players here", getInnerText("-queue-1-1"));
  }

  public void testPickDuringDrag() {
    login(2);
    startDrag("-players-table-2");
    simulateDraftStatus(DraftStatusTestUtil.createDraftStatus(
        Lists.newArrayList(DraftStatusTestUtil.createAndPostDraftPick(
            1, "0000000000", false, Position.P, ginjector.getBeanFactory(),
            (TestPlayerDataSource) ginjector.getPlayerDataSource())),
        ginjector.getBeanFactory()));
    finishDragToBottom("-players-table-1", "-queue-1");
    assertEquals("1111111111", getInnerText("-queue-1-1"));
  }

  public void testDraggedPlayerPickedDuringDrag() {
    login(2);
    startDrag("-players-table-1");
    simulateDraftStatus(DraftStatusTestUtil.createDraftStatus(
        Lists.newArrayList(DraftStatusTestUtil.createAndPostDraftPick(
            1, "0000000000", false, Position.P, ginjector.getBeanFactory(),
            (TestPlayerDataSource) ginjector.getPlayerDataSource())),
        ginjector.getBeanFactory()));
    finishDragToBottom("-players-table-1", "-queue-1");
    assertEquals("Drag players here", getInnerText("-queue-1-1"));
  }
}