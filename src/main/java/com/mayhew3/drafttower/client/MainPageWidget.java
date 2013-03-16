package com.mayhew3.drafttower.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayhew3.drafttower.client.events.LoginEvent;

/**
 * Widget containing the entire UI.
 */
@Singleton
public class MainPageWidget extends Composite
    implements LoginEvent.Handler {

  interface Resources extends ClientBundle {
    interface Css extends CssResource {
      String connectivityIndicator();
      String leftColumn();
      String centerColumn();
      String queue();
      String queueScroller();
      String rightColumn();
      String tableHeader();
      String actionLink();
      String glassPanel();
    }

    @Source("MainPageWidget.css")
    Css css();
  }

  private static final Resources.Css CSS = ((Resources) GWT.create(Resources.class)).css();
  static {
    CSS.ensureInjected();
  }

  interface MyUiBinder extends UiBinder<Widget, MainPageWidget> {}
  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  @UiField(provided = true) final ConnectivityIndicator connectivityIndicator;
  @UiField(provided = true) final LoginWidget loginWidget;
  @UiField(provided = true) final DraftClock clock;
  @UiField(provided = true) final PickWidget pickWidget;
  @UiField(provided = true) final PickHistoryTablePanel pickHistoryTable;
  @UiField(provided = true) final MyRosterTablePanel myRosterTable;
  @UiField(provided = true) final TeamOrderWidget teamOrder;
  @UiField(provided = true) FilledPositionsChart filledPositionsChart;
  @UiField(provided = true) PlayerTablePanel unclaimedPlayers;
  @UiField(provided = true) QueueTable queueTable;
  @UiField(provided = true) AudioController audioController;

  @UiField DivElement mainPage;
  @UiField Label showDepthCharts;
  @UiField Label showBarGraphs;
  @UiField DivElement queueArea;

  private final PopupPanel depthChartsPopup;
  private final PopupPanel barGraphsPopup;

  @Inject
  public MainPageWidget(ConnectivityIndicator connectivityIndicator,
      LoginWidget loginWidget,
      DraftClock clock,
      PickWidget pickWidget,
      PickHistoryTablePanel pickHistoryTable,
      MyRosterTablePanel myRosterTable,
      TeamOrderWidget teamOrder,
      FilledPositionsChart filledPositionsChart,
      PlayerTablePanel unclaimedPlayers,
      QueueTable queueTable,
      DepthChartsTable depthChartsTable,
      BarGraphs barGraphs,
      AudioController audioController,
      EventBus eventBus) {
    this.connectivityIndicator = connectivityIndicator;
    this.loginWidget = loginWidget;
    this.clock = clock;
    this.pickWidget = pickWidget;
    this.pickHistoryTable = pickHistoryTable;
    this.myRosterTable = myRosterTable;
    this.teamOrder = teamOrder;
    this.filledPositionsChart = filledPositionsChart;
    this.unclaimedPlayers = unclaimedPlayers;
    this.queueTable = queueTable;
    this.audioController = audioController;

    initWidget(uiBinder.createAndBindUi(this));

    UIObject.setVisible(mainPage, false);

    depthChartsPopup = new PopupPanel();
    depthChartsPopup.setModal(true);
    depthChartsPopup.setAutoHideEnabled(true);
    depthChartsPopup.setGlassEnabled(true);
    depthChartsPopup.setGlassStyleName(CSS.glassPanel());
    depthChartsPopup.setWidget(depthChartsTable);

    barGraphsPopup = new PopupPanel();
    barGraphsPopup.setModal(true);
    barGraphsPopup.setAutoHideEnabled(true);
    barGraphsPopup.setGlassEnabled(true);
    barGraphsPopup.setGlassStyleName(CSS.glassPanel());
    barGraphsPopup.setWidget(barGraphs);

    eventBus.addHandler(LoginEvent.TYPE, this);
  }

  @Override
  public void onLogin(LoginEvent event) {
    loginWidget.setVisible(false);
    UIObject.setVisible(mainPage, true);
  }

  @UiHandler("showDepthCharts")
  public void handleShowDepthChartsClick(ClickEvent e) {
    depthChartsPopup.center();
    depthChartsPopup.show();
  }

  @UiHandler("showBarGraphs")
  public void handleShowBarGraphsClick(ClickEvent e) {
    barGraphsPopup.center();
    barGraphsPopup.show();
  }

  public int getQueueAreaTop() {
    return queueArea.getAbsoluteTop();
  }
}