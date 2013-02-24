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
import com.mayhew3.drafttower.client.events.LoginEvent;

/**
 * Widget containing the entire UI.
 */
public class MainPageWidget extends Composite
    implements LoginEvent.Handler {

  interface Resources extends ClientBundle {
    interface Css extends CssResource {
      String connectivityIndicator();
      String leftColumn();
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
  @UiField(provided = true) PlayerTablePanel unclaimedPlayers;
  @UiField(provided = true) QueueTable queueTable;

  @UiField DivElement mainPage;
  @UiField Label showDepthCharts;

  private final PopupPanel depthChartsPopup;

  @Inject
  public MainPageWidget(ConnectivityIndicator connectivityIndicator,
      LoginWidget loginWidget,
      DraftClock clock,
      PickWidget pickWidget,
      PickHistoryTablePanel pickHistoryTable,
      MyRosterTablePanel myRosterTable,
      TeamOrderWidget teamOrder,
      PlayerTablePanel unclaimedPlayers,
      QueueTable queueTable,
      DepthChartsTable depthChartsTable,
      EventBus eventBus) {
    this.connectivityIndicator = connectivityIndicator;
    this.loginWidget = loginWidget;
    this.clock = clock;
    this.pickWidget = pickWidget;
    this.pickHistoryTable = pickHistoryTable;
    this.myRosterTable = myRosterTable;
    this.teamOrder = teamOrder;
    this.unclaimedPlayers = unclaimedPlayers;
    this.queueTable = queueTable;

    initWidget(uiBinder.createAndBindUi(this));

    UIObject.setVisible(mainPage, false);

    depthChartsPopup = new PopupPanel();
    depthChartsPopup.setModal(true);
    depthChartsPopup.setAutoHideEnabled(true);
    depthChartsPopup.setGlassEnabled(true);
    depthChartsPopup.setGlassStyleName(CSS.glassPanel());
    depthChartsPopup.setWidget(depthChartsTable);

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
}