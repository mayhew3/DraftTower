package com.mayhew3.drafttower.client.pickcontrols;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;

/**
 * Widget for making picks.
 */
public class PickControlsWidget extends Composite implements PickControlsView {

  interface Resources extends ClientBundle {
    interface Css extends CssResource {
      String container();
      String pickButton();
      String commish();
      String showCommish();
    }

    @Source("PickControlsWidget.css")
    Css css();
  }

  private static final Resources.Css CSS = ((Resources) GWT.create(Resources.class)).css();
  static {
    CSS.ensureInjected();
  }

  interface MyUiBinder extends UiBinder<Widget, PickControlsWidget> {}
  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private final PickControlsPresenter presenter;

  @UiField HTML selectedPlayerLabel;
  @UiField Button pick;
  @UiField Button enqueue;
  @UiField Button forcePick;
  @UiField Button wakeUp;
  @UiField Button reset;
  @UiField Button clearCaches;
  @UiField DivElement commishTools;
  @UiField DivElement showCommishToolsContainer;
  @UiField Anchor showCommishTools;

  @Inject
  public PickControlsWidget(PickControlsPresenter presenter) {
    this.presenter = presenter;

    initWidget(uiBinder.createAndBindUi(this));

    UIObject.setVisible(commishTools, false);
    pick.setEnabled(false);

    presenter.setView(this);
  }

  @UiHandler("pick")
  public void handlePick(ClickEvent e) {
    presenter.pick();
  }

  @UiHandler("enqueue")
  public void handleEnqueue(ClickEvent e) {
    presenter.enqueue();
  }

  @UiHandler("forcePick")
  public void handleForcePick(ClickEvent e) {
    presenter.forcePick();
  }

  @UiHandler("wakeUp")
  public void handleWakeUp(ClickEvent e) {
    presenter.wakeUp();
  }

  @UiHandler("reset")
  public void handleReset(ClickEvent e) {
    presenter.resetDraft();
  }

  @UiHandler("clearCaches")
  public void handleClearCaches(ClickEvent e) {
    presenter.clearCaches();
  }

  @UiHandler("showCommishTools")
  public void toggleCommishTools(ClickEvent e) {
    UIObject.setVisible(commishTools, !UIObject.isVisible(commishTools));
  }

  @Override
  public void setSelectedPlayerName(String name) {
    selectedPlayerLabel.setText(name);
  }

  @Override
  public void clearSelectedPlayerName() {
    selectedPlayerLabel.setHTML("&nbsp");
  }

  @Override
  public void setPickEnabled(boolean enabled) {
    pick.setEnabled(enabled);
  }

  @Override
  public void setEnqueueEnabled(boolean enabled) {
    enqueue.setEnabled(enabled);
  }

  @Override
  public void setForcePickEnabled(boolean enabled) {
    forcePick.setEnabled(enabled);
  }

  @Override
  public void setCommishToolsVisible(boolean visible) {
    UIObject.setVisible(showCommishToolsContainer, visible);
  }

  @Override
  public void setWakeUpVisible(boolean visible) {
    wakeUp.setVisible(visible);
  }

  @Override
  protected void onEnsureDebugId(String baseID) {
    super.onEnsureDebugId(baseID);
    pick.ensureDebugId(baseID + "-pick");
    enqueue.ensureDebugId(baseID + "-enqueue");
    UIObject.ensureDebugId(showCommishToolsContainer, baseID + "-showCommishContainer");
    showCommishTools.ensureDebugId(baseID + "-showCommish");
    forcePick.ensureDebugId(baseID + "-force");
    wakeUp.ensureDebugId(baseID + "-wakeUp");
    reset.ensureDebugId(baseID + "-reset");
    clearCaches.ensureDebugId(baseID + "-clearCaches");
  }
}