package com.mayhew3.drafttower.client;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.mayhew3.drafttower.shared.DraftPick;
import com.mayhew3.drafttower.shared.DraftStatus;
import com.mayhew3.drafttower.shared.DraftStatusTestUtil;
import com.mayhew3.drafttower.shared.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Base class for client tests.
 */
public abstract class TestBase extends GWTTestCase {

  private static final Logger logger = Logger.getLogger(TestBase.class.getName());

  protected static final String LOGIN_WIDGET = "-login";
  protected static final String USERNAME = "-login-username";
  protected static final String PASSWORD = "-login-password";
  protected static final String LOGIN_BUTTON = "-login-login";
  protected static final String INVALID_LOGIN = "-login-invalid";
  protected static final String ALREADY_LOGGED_IN = "-login-already";
  protected static final String LOGOUT_LINK = "-logout";

  protected static final String CONNECTIVITY_INDICATOR = "-conn";

  protected DraftTowerTestGinjector ginjector;
  protected MainPageWidget mainPageWidget;

  @Override
  public String getModuleName() {
    return "com.mayhew3.drafttower.DraftTowerJUnit";
  }

  @Override
  protected void gwtSetUp() {
    ginjector = GWT.create(DraftTowerTestGinjector.class);
    ((TestSchedulerImpl) Scheduler.get()).setScheduler(ginjector.getScheduler());
    reset();
    ginjector.getScheduler().flush();
  }

  protected void reset() {
    if (mainPageWidget != null) {
      RootPanel.get().remove(mainPageWidget);
    }
    mainPageWidget = ginjector.getMainPageWidget();
    RootPanel.get().add(mainPageWidget);
  }

  @Override
  protected void gwtTearDown() throws Exception {
    RootPanel.get().remove(mainPageWidget);
  }

  protected void type(String debugId, String text) {
    Element element = ensureDebugIdAndGetElement(debugId, true);
    InputElement.as(element).setValue(text);
  }

  protected void pressKey(String debugId, int keyCode) {
    Element element = ensureDebugIdAndGetElement(debugId, true);
    element.dispatchEvent(
        Document.get().createKeyDownEvent(false, false, false, false, keyCode));
    element.dispatchEvent(
        Document.get().createKeyUpEvent(false, false, false, false, keyCode));
    element.dispatchEvent(
        Document.get().createKeyPressEvent(false, false, false, false, keyCode));
  }

  protected void click(String debugId) {
    Element element = ensureDebugIdAndGetElement(debugId, true);
    int x = element.getAbsoluteLeft() + element.getOffsetWidth() / 2;
    int y = element.getAbsoluteTop() + element.getOffsetHeight() / 2;
    element.dispatchEvent(
      Document.get().createMouseOverEvent(
          0, x, y, x, y, false, false, false, false, 0, null));
    element.dispatchEvent(
      Document.get().createMouseDownEvent(
          0, x, y, x, y, false, false, false, false, NativeEvent.BUTTON_LEFT));
    element.dispatchEvent(
      Document.get().createMouseUpEvent(
          0, x, y, x, y, false, false, false, false, NativeEvent.BUTTON_LEFT));
    element.dispatchEvent(
        Document.get().createClickEvent(
            0, x, y, x, y, false, false, false, false));
  }

  protected void selectTableRow(String tableDebugId, int rowNum) {
    click(tableDebugId + "-" + rowNum + "-0");
    // Selection model schedules firing the event, so we need to flush.
    ginjector.getScheduler().flush();
  }

  protected void dragToTop(String sourceDebugId, String targetDebugId) {
    drag(sourceDebugId, targetDebugId, 0.25);
  }

  protected void dragToBottom(String sourceDebugId, String targetDebugId) {
    drag(sourceDebugId, targetDebugId, 0.75);
  }

  private void drag(String sourceDebugId, String targetDebugId,
      double targetYProportion) {
    startDrag(sourceDebugId);
    finishDrag(sourceDebugId, targetDebugId, targetYProportion);
  }

  protected void startDrag(String sourceDebugId) {
    Element source = ensureDebugIdAndGetElement(sourceDebugId, true);
    int sourceX = source.getAbsoluteLeft() + source.getOffsetWidth() / 2;
    int sourceY = source.getAbsoluteTop() + source.getOffsetHeight() / 2;

    source.dispatchEvent(
      Document.get().createMouseOverEvent(
          0, sourceX, sourceY, sourceX, sourceY, false, false, false, false, 0, null));
    source.dispatchEvent(
        Document.get().createMouseDownEvent(
            0, sourceX, sourceY, sourceX, sourceY, false, false, false, false, NativeEvent.BUTTON_LEFT));
  }

  protected void finishDragToTop(String sourceDebugId, String targetDebugId) {
    finishDrag(sourceDebugId, targetDebugId, 0.25);
  }

  protected void finishDragToBottom(String sourceDebugId, String targetDebugId) {
    finishDrag(sourceDebugId, targetDebugId, 0.75);
  }

  private void finishDrag(String sourceDebugId, String targetDebugId, double targetYProportion) {
    Element source = ensureDebugIdAndGetElement(sourceDebugId, true);
    int sourceX = source.getAbsoluteLeft() + source.getOffsetWidth() / 2;
    int sourceY = source.getAbsoluteTop() + source.getOffsetHeight() / 2;
    Element target = ensureDebugIdAndGetElement(targetDebugId, true);
    int targetX = target.getAbsoluteLeft() + target.getOffsetWidth() / 2;
    int targetY = target.getAbsoluteTop() + (int) (target.getOffsetHeight() * targetYProportion);
    source.dispatchEvent(
        Document.get().createMouseMoveEvent(
            0, sourceX + 1, sourceY + 1, sourceX + 1, sourceY + 1, false, false, false, false, 0));
    target.dispatchEvent(
        Document.get().createMouseMoveEvent(
            0, targetX, targetY, targetX, targetY, false, false, false, false, 0));
    target.dispatchEvent(
        Document.get().createMouseUpEvent(
            0, targetX, targetY, targetX, targetY, false, false, false, false, NativeEvent.BUTTON_LEFT));
    ginjector.getScheduler().flush();
  }

  protected boolean isVisible(String debugId) {
    Element element = ensureDebugIdAndGetElement(debugId, false);
    return element != null && UIObject.isVisible(element);
  }

  protected boolean isFocused(String debugId) {
    ensureDebugIdAndGetElement(debugId, true);
    return (UIObject.DEBUG_ID_PREFIX + debugId).equals(getFocusedElementId());
  }

  protected boolean isEnabled(String debugId) {
    Element element = ensureDebugIdAndGetElement(debugId, true);
    return !element.hasAttribute("disabled");
  }

  protected boolean hasStyle(String debugId, String style) {
    Element element = ensureDebugIdAndGetElement(debugId, true);
    return element.hasClassName(style);
  }

  protected void login(int team) {
    type(USERNAME, Integer.toString(team));
    type(PASSWORD, Integer.toString(team));
    pressKey(PASSWORD, KeyCodes.KEY_ENTER);
  }

  protected native String getFocusedElementId() /*-{
    return $doc.activeElement.id;
  }-*/;

  protected String getInnerText(String debugId) {
    Element element = ensureDebugIdAndGetElement(debugId, true);
    return element.getInnerText();
  }

  protected String getInnerHTML(String debugId) {
    Element element = ensureDebugIdAndGetElement(debugId, true);
    return element.getInnerHTML();
  }

  protected Element ensureDebugIdAndGetElement(String debugId, boolean assertElementPresent) {
    mainPageWidget.ensureDebugId("");
    Element element = Document.get().getElementById(UIObject.DEBUG_ID_PREFIX + debugId);
    if (assertElementPresent && element == null) {
      String innerHTML = Document.get().getBody().getInnerHTML();
      logger.severe(innerHTML);
      throw new AssertionError("Couldn't find element " + debugId);
    }
    return element;
  }

  protected void simulateDraftStatus(DraftStatus newStatus) {
    DraftStatus status = ginjector.getDraftStatus();
    status.setConnectedTeams(newStatus.getConnectedTeams());
    status.setCurrentPickDeadline(newStatus.getCurrentPickDeadline());
    status.setCurrentTeam(newStatus.getCurrentTeam());
    status.setNextPickKeeperTeams(newStatus.getNextPickKeeperTeams());
    status.setOver(newStatus.isOver());
    status.setPaused(newStatus.isPaused());
    status.setPicks(newStatus.getPicks());
    status.setRobotTeams(newStatus.getRobotTeams());
    long oldSerialId = status.getSerialId();
    status.setSerialId(newStatus.getSerialId());

    // Diff new status with copied status to protect against forgetting to add new fields here.
    Map<String, Object> diff = AutoBeanUtils.diff(
        AutoBeanUtils.getAutoBean(status),
        AutoBeanUtils.getAutoBean(newStatus));
    assertTrue(diff.keySet().toString(), diff.isEmpty());

    status.setSerialId(oldSerialId + 1);

    ginjector.getPlayerDataSource().setDraftPicks(status.getPicks());
    ginjector.getDraftController().sendStatusUpdates();
  }

  protected void simulateDraftStatus(Position[][] positions) {
    List<DraftPick> picks = new ArrayList<>();
    for (int round = 0; round < positions[0].length; round++) {
      for (int team = 1; team <= positions.length; team++) {
        Position position = positions[team - 1][round];
        if (position != null) {
          picks.add(DraftStatusTestUtil.createAndPostDraftPick(
              team, "Guy " + round + team, false, position, ginjector.getBeanFactory(), ginjector.getPlayerDataSource()));
        }
      }
    }
    simulateDraftStatus(DraftStatusTestUtil.createDraftStatus(
        picks, ginjector.getBeanFactory()));
  }

  protected void simulateDraftStart() {
    simulateDraftStatus(DraftStatusTestUtil.createDraftStatus(
        Lists.<DraftPick>newArrayList(), ginjector.getBeanFactory()));
  }
}