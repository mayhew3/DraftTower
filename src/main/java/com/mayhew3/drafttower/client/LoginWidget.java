package com.mayhew3.drafttower.client;

import com.google.common.net.HttpHeaders;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.*;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.mayhew3.drafttower.client.DraftTowerGinModule.LoginUrl;
import com.mayhew3.drafttower.client.events.LoginEvent;
import com.mayhew3.drafttower.shared.BeanFactory;
import com.mayhew3.drafttower.shared.LoginResponse;

import static com.google.gwt.http.client.RequestBuilder.POST;

/**
 * Login form.
 */
public class LoginWidget extends Composite {

  interface Resources extends ClientBundle {
    interface Css extends CssResource {
    }

    @Source("LoginWidget.css")
    Css css();
  }

  private static final Resources.Css CSS = ((Resources) GWT.create(Resources.class)).css();
  static {
    CSS.ensureInjected();
  }

  interface MyUiBinder extends UiBinder<Widget, LoginWidget> {}
  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private final String loginUrl;
  private final TeamInfo teamInfo;
  private final BeanFactory beanFactory;
  private final EventBus eventBus;

  @UiField TextBox username;
  @UiField PasswordTextBox password;
  @UiField Button login;
  @UiField DivElement error;

  @Inject
  public LoginWidget(@LoginUrl String loginUrl,
      TeamInfo teamInfo,
      BeanFactory beanFactory,
      EventBus eventBus) {
    this.loginUrl = loginUrl;
    this.teamInfo = teamInfo;
    this.beanFactory = beanFactory;
    this.eventBus = eventBus;
    initWidget(uiBinder.createAndBindUi(this));
    UIObject.setVisible(error, false);
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      @Override
      public void execute() {
        username.setFocus(true);
      }
    });
  }

  @UiHandler("password")
  public void handleEnter(KeyDownEvent event) {
    if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
      doLogin();
    }
  }

  @UiHandler("login")
  public void handleLogin(ClickEvent event) {
    doLogin();
  }

  private void doLogin() {
    UIObject.setVisible(error, false);
    RequestBuilder requestBuilder = new RequestBuilder(POST, loginUrl);
    requestBuilder.setHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
    try {
      requestBuilder.sendRequest("username=" + username.getValue() + "&password=" + password.getValue(),
          new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
              if (response.getStatusCode() == 200) {
                teamInfo.setLoginResponse(AutoBeanCodex.decode(beanFactory, LoginResponse.class, response.getText()).as());
                eventBus.fireEvent(new LoginEvent());
              } else {
                UIObject.setVisible(error, true);
              }
            }

            @Override
            public void onError(Request request, Throwable exception) {
              UIObject.setVisible(error, true);
            }
          });
    } catch (RequestException e) {
      UIObject.setVisible(error, true);
    }
  }
}