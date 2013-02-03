package com.mayhew3.drafttower.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.mayhew3.drafttower.client.events.LoginEvent;
import com.mayhew3.drafttower.shared.BeanFactory;

/**
 * Widget containing the entire UI.
 */
public class MainPageWidget extends Composite
    implements LoginEvent.Handler {

  interface Resources extends ClientBundle {
    interface Css extends CssResource {
      String connectivityIndicator();
      String leftColumn();
      String rightColumn();
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

  private final DraftSocketHandler socketHandler;
  private final BeanFactory beanFactory;
  private final TeamInfo teamInfo;

  @UiField(provided = true) final ConnectivityIndicator connectivityIndicator;
  @UiField(provided = true) final LoginWidget loginWidget;
  @UiField(provided = true) final DraftClock clock;
  @UiField(provided = true) PlayerTablePanel unclaimedPlayers;

  @UiField DivElement mainPage;

  @Inject
  public MainPageWidget(ConnectivityIndicator connectivityIndicator,
      LoginWidget loginWidget,
      DraftClock clock,
      PlayerTablePanel unclaimedPlayers,
      final DraftSocketHandler socketHandler,
      final BeanFactory beanFactory,
      TeamInfo teamInfo,
      EventBus eventBus) {
    this.connectivityIndicator = connectivityIndicator;
    this.loginWidget = loginWidget;
    this.socketHandler = socketHandler;
    this.beanFactory = beanFactory;
    this.clock = clock;
    this.unclaimedPlayers = unclaimedPlayers;
    this.teamInfo = teamInfo;

    initWidget(uiBinder.createAndBindUi(this));

    UIObject.setVisible(mainPage, false);

    eventBus.addHandler(LoginEvent.TYPE, this);
  }

  public void onLogin(LoginEvent event) {
    loginWidget.setVisible(false);
    UIObject.setVisible(mainPage, true);
  }
}