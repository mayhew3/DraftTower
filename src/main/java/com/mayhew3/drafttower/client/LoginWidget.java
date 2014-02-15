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
import com.google.gwt.user.client.Cookies;
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
      String container();
      String box();
      String title();
      String error();
      String fieldLabel();
      String field();
      String loginButton();
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
  private final TeamsInfo teamsInfo;
  private final BeanFactory beanFactory;
  private final EventBus eventBus;

  @UiField TextBox username;
  @UiField PasswordTextBox password;
  @UiField Button login;
  @UiField DivElement error;

  @Inject
  public LoginWidget(@LoginUrl String loginUrl,
      TeamsInfo teamsInfo,
      BeanFactory beanFactory,
      EventBus eventBus) {
    this.loginUrl = loginUrl;
    this.teamsInfo = teamsInfo;
    this.beanFactory = beanFactory;
    this.eventBus = eventBus;
    initWidget(uiBinder.createAndBindUi(this));
    UIObject.setVisible(error, false);

    String storedTeamToken = Cookies.getCookie(LoginResponse.TEAM_TOKEN_COOKIE);
    if (storedTeamToken != null) {
      doAutoLogin();
    } else {
      Scheduler.get().scheduleDeferred(new ScheduledCommand() {
        @Override
        public void execute() {
          username.setFocus(true);
        }
      });
    }
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

  private void doAutoLogin() {
    setVisible(false);
    doLogin("",
        new Runnable() {
          @Override
          public void run() {
            autoLoginFailed();
          }
        });
  }

  private void doLogin() {
    UIObject.setVisible(error, false);
    doLogin("username=" + username.getValue() + "&password=" + password.getValue(),
        new Runnable() {
          @Override
          public void run() {
            loginFailed();
          }
        });
  }

  private void doLogin(String requestParams, final Runnable failureCallback) {
    RequestBuilder requestBuilder = new RequestBuilder(POST, loginUrl);
    requestBuilder.setHeader(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded");
    try {
      requestBuilder.sendRequest(requestParams,
          new RequestCallback() {
            @Override
            public void onResponseReceived(Request request, Response response) {
              if (response.getStatusCode() == 200) {
                LoginResponse loginResponse =
                    AutoBeanCodex.decode(beanFactory, LoginResponse.class, response.getText()).as();
                teamsInfo.setLoginResponse(loginResponse);
                eventBus.fireEvent(new LoginEvent(loginResponse));
              } else {
                failureCallback.run();
              }
            }

            @Override
            public void onError(Request request, Throwable exception) {
              failureCallback.run();
            }
          });
    } catch (RequestException e) {
      failureCallback.run();
    }
  }

  private void autoLoginFailed() {
    setVisible(true);
    username.setFocus(true);
  }

  private void loginFailed() {
    UIObject.setVisible(error, true);
  }
}