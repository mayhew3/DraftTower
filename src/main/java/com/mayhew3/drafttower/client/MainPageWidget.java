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
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.mayhew3.drafttower.client.GinBindingAnnotations.PlayerPopupUrlPrefix;
import com.mayhew3.drafttower.client.audio.AudioWidget;
import com.mayhew3.drafttower.client.audio.SpeechControlWidget;
import com.mayhew3.drafttower.client.clock.DraftClock;
import com.mayhew3.drafttower.client.depthcharts.DepthChartsTable;
import com.mayhew3.drafttower.client.events.LoginEvent;
import com.mayhew3.drafttower.client.events.ReloadWindowEvent;
import com.mayhew3.drafttower.client.events.ShowPlayerPopupEvent;
import com.mayhew3.drafttower.client.filledpositions.FilledPositionsChart;
import com.mayhew3.drafttower.client.graphs.BarGraphsWidget;
import com.mayhew3.drafttower.client.login.LoginPresenter;
import com.mayhew3.drafttower.client.login.LoginWidget;
import com.mayhew3.drafttower.client.myroster.MyRosterTablePanel;
import com.mayhew3.drafttower.client.pickcontrols.PickControlsWidget;
import com.mayhew3.drafttower.client.pickhistory.PickHistoryTablePanel;
import com.mayhew3.drafttower.client.players.queue.QueueTable;
import com.mayhew3.drafttower.client.players.unclaimed.UnclaimedPlayerTablePanel;
import com.mayhew3.drafttower.client.teamorder.TeamOrderWidget;
import com.mayhew3.drafttower.client.websocket.ConnectivityIndicator;
import com.mayhew3.drafttower.shared.Player;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Widget containing the entire UI.
 */
public class MainPageWidget extends Composite implements
    LoginEvent.Handler,
    ShowPlayerPopupEvent.Handler,
    ReloadWindowEvent.Handler {

  interface Resources extends ClientBundle {
    interface Css extends CssResource {
      String speechControl();
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

  @UiField(provided = true) final SpeechControlWidget speechControl;
  @UiField(provided = true) final ConnectivityIndicator connectivityIndicator;
  @UiField(provided = true) final LoginWidget loginWidget;
  @UiField(provided = true) final DraftClock clock;
  @UiField(provided = true) final PickControlsWidget pickControlsWidget;
  @UiField(provided = true) final PickHistoryTablePanel pickHistoryTable;
  @UiField(provided = true) final MyRosterTablePanel myRosterTable;
  @UiField(provided = true) final TeamOrderWidget teamOrder;
  @UiField(provided = true) final FilledPositionsChart filledPositionsChart;
  @UiField(provided = true) final UnclaimedPlayerTablePanel unclaimedPlayers;
  @UiField(provided = true) final QueueTable queueTable;
  @UiField(provided = true) final AudioWidget audioWidget;

  @UiField DivElement mainPage;
  @UiField Label showDepthCharts;
  @UiField Label showBarGraphs;
  @UiField Label logout;
  @UiField DivElement queueArea;

  private final PopupPanel depthChartsPopup;
  private final DepthChartsTable depthChartsTable;
  private final PopupPanel barGraphsPopup;
  private final BarGraphsWidget barGraphsWidget;
  private final PopupPanel playerPopup;
  private final Frame playerPopupFrame;

  private final LoginPresenter loginPresenter;
  private final String playerPopupUrlPrefix;

  @Inject
  public MainPageWidget(SpeechControlWidget speechControl,
      ConnectivityIndicator connectivityIndicator,
      LoginWidget loginWidget,
      DraftClock clock,
      PickControlsWidget pickControlsWidget,
      PickHistoryTablePanel pickHistoryTable,
      MyRosterTablePanel myRosterTable,
      TeamOrderWidget teamOrder,
      FilledPositionsChart filledPositionsChart,
      UnclaimedPlayerTablePanel unclaimedPlayers,
      QueueTable queueTable,
      DepthChartsTable depthChartsTable,
      BarGraphsWidget barGraphsWidget,
      AudioWidget audioWidget,
      EventBus eventBus,
      LoginPresenter loginPresenter,
      @PlayerPopupUrlPrefix String playerPopupUrlPrefix) {
    this.speechControl = speechControl;
    this.connectivityIndicator = connectivityIndicator;
    this.loginWidget = loginWidget;
    this.clock = clock;
    this.pickControlsWidget = pickControlsWidget;
    this.pickHistoryTable = pickHistoryTable;
    this.myRosterTable = myRosterTable;
    this.teamOrder = teamOrder;
    this.filledPositionsChart = filledPositionsChart;
    this.unclaimedPlayers = unclaimedPlayers;
    this.queueTable = queueTable;
    this.audioWidget = audioWidget;
    this.depthChartsTable = depthChartsTable;
    this.barGraphsWidget = barGraphsWidget;
    this.loginPresenter = loginPresenter;
    this.playerPopupUrlPrefix = playerPopupUrlPrefix;

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
    barGraphsPopup.setWidget(barGraphsWidget);

    playerPopup = new PopupPanel();
    playerPopup.setModal(true);
    playerPopup.setAutoHideEnabled(true);
    playerPopup.setGlassEnabled(true);
    playerPopup.setGlassStyleName(CSS.glassPanel());
    playerPopupFrame = new Frame();
    playerPopupFrame.setSize("980px", "450px");
    playerPopupFrame.getElement().setAttribute("seamless", "true");
    playerPopup.setWidget(playerPopupFrame);

    unclaimedPlayers.setQueueAreaTopProvider(new Provider<Integer>() {
      @Override
      public Integer get() {
        return getQueueAreaTop();
      }
    });

    eventBus.addHandler(LoginEvent.TYPE, this);
    eventBus.addHandler(ShowPlayerPopupEvent.TYPE, this);
    eventBus.addHandler(ReloadWindowEvent.TYPE, this);
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

  @UiHandler("logout")
  public void handleLogoutClick(ClickEvent e) {
    loginPresenter.logout();
    if (GWT.isProdMode()) {
      Window.Location.reload();
    }
  }

  public int getQueueAreaTop() {
    return queueArea.getAbsoluteTop();
  }

  @Override
  public void showPlayerPopup(ShowPlayerPopupEvent event) {
    Player player = event.getPlayer();
    long cbsId = player.getCBSId();
    playerPopupFrame.setUrl(playerPopupUrlPrefix
        + cbsId
        + "?loc=snippet&selected_tab=news&selected_subtab=");
    playerPopup.center();
    playerPopup.show();
  }

  @Override
  public void onReload(ReloadWindowEvent event) {
    if (GWT.isProdMode()) {
      Window.Location.reload();
    }
  }

  @Override
  protected void onEnsureDebugId(String baseID) {
    super.onEnsureDebugId(baseID);
    loginWidget.ensureDebugId(baseID + "-login");
    logout.ensureDebugId(baseID + "-logout");
    speechControl.ensureDebugId(baseID + "-speech");
    connectivityIndicator.ensureDebugId(baseID + "-conn");
    audioWidget.ensureDebugId(baseID + "-audio");
    clock.ensureDebugId(baseID + "-clock");
    showDepthCharts.ensureDebugId(baseID + "-showDepthCharts");
    depthChartsTable.ensureDebugId(baseID + "-depthCharts");
    showBarGraphs.ensureDebugId(baseID + "-showBarGraphs");
    barGraphsWidget.ensureDebugId(baseID + "-barGraphs");
    filledPositionsChart.ensureDebugId(baseID + "-filledPositions");
    myRosterTable.ensureDebugId(baseID + "-myRoster");
    pickControlsWidget.ensureDebugId(baseID + "-pickControls");
    unclaimedPlayers.ensureDebugId(baseID + "-players");
    queueTable.ensureDebugId(baseID + "-queue");
    pickHistoryTable.ensureDebugId(baseID + "-pickHistory");
    teamOrder.ensureDebugId(baseID + "-teamOrder");
    playerPopupFrame.ensureDebugId(baseID + "-playerPopup");
  }
}