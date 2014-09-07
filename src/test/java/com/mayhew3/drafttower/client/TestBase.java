package com.mayhew3.drafttower.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.UIObject;

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
    reset();
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
          0, x, y, x, y, false, false, false, false, 0));
    element.dispatchEvent(
      Document.get().createMouseUpEvent(
          0, x, y, x, y, false, false, false, false, 0));
    element.dispatchEvent(
        Document.get().createClickEvent(
            0, x, y, x, y, false, false, false, false));
  }

  protected boolean isVisible(String debugId) {
    Element element = ensureDebugIdAndGetElement(debugId, false);
    return element != null && UIObject.isVisible(element);
  }

  protected boolean isFocused(String debugId) {
    ensureDebugIdAndGetElement(debugId, true);
    return (UIObject.DEBUG_ID_PREFIX + debugId).equals(getFocusedElementId());
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

  private Element ensureDebugIdAndGetElement(String debugId, boolean assertElementPresent) {
    mainPageWidget.ensureDebugId("");
    Element element = Document.get().getElementById(UIObject.DEBUG_ID_PREFIX + debugId);
    if (assertElementPresent && element == null) {
      logger.severe(Document.get().getBody().getInnerHTML());
      throw new AssertionError("Couldn't find element " + debugId);
    }
    return element;
  }
}