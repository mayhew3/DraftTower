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
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.mayhew3.drafttower.client.events.LoginEvent;
import com.mayhew3.drafttower.client.events.ShowPlayerPopupEvent;
import com.mayhew3.drafttower.shared.LoginResponse;
import com.mayhew3.drafttower.shared.Player;

/**
 * Widget containing the entire UI.
 */
@Singleton
public class MainPageWidget extends Composite implements
    LoginEvent.Handler,
    ShowPlayerPopupEvent.Handler {

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
  @UiField Label logout;
  @UiField DivElement queueArea;

  private final PopupPanel depthChartsPopup;
  private final PopupPanel barGraphsPopup;
  private final PopupPanel playerPopup;
  private final Frame playerPopupFrame;

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

    playerPopup = new PopupPanel();
    playerPopup.setModal(true);
    playerPopup.setAutoHideEnabled(true);
    playerPopup.setGlassEnabled(true);
    playerPopup.setGlassStyleName(CSS.glassPanel());
    playerPopupFrame = new Frame();
    playerPopupFrame.setSize("810px", "450px");
    playerPopupFrame.getElement().setAttribute("seamless", "true");
    playerPopup.setWidget(playerPopupFrame);

    eventBus.addHandler(LoginEvent.TYPE, this);
    eventBus.addHandler(ShowPlayerPopupEvent.TYPE, this);
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
    Cookies.removeCookie(LoginResponse.TEAM_TOKEN_COOKIE);
    Window.Location.reload();
  }

  public int getQueueAreaTop() {
    return queueArea.getAbsoluteTop();
  }

  @Override
  public void showPlayerPopup(ShowPlayerPopupEvent event) {
    Player player = event.getPlayer();
    long cbsId = player.getCBSId();
    playerPopupFrame.setUrl("http://uncharted.baseball.cbssports.com/players/playerpage/snippet/"
        + cbsId
        + "?loc=snippet&selected_tab=news&selected_subtab=");
    playerPopup.center();
    playerPopup.show();
  }
}