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
import com.mayhew3.drafttower.client.DraftTowerGinModule.LoginUrl;
import com.mayhew3.drafttower.client.DraftTowerGinModule.TeamToken;
import com.mayhew3.drafttower.client.events.LoginEvent;

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
  private final StringHolder teamToken;
  private final EventBus eventBus;

  @UiField TextBox username;
  @UiField PasswordTextBox password;
  @UiField Button login;
  @UiField DivElement error;

  @Inject
  public LoginWidget(@LoginUrl String loginUrl,
      @TeamToken StringHolder teamToken,
      EventBus eventBus) {
    this.loginUrl = loginUrl;
    this.teamToken = teamToken;
    this.eventBus = eventBus;
    initWidget(uiBinder.createAndBindUi(this));
    UIObject.setVisible(error, false);
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
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
            public void onResponseReceived(Request request, Response response) {
              if (response.getStatusCode() == 200) {
                teamToken.setValue(response.getText());
                eventBus.fireEvent(new LoginEvent());
              } else {
                UIObject.setVisible(error, true);
              }
            }

            public void onError(Request request, Throwable exception) {
              UIObject.setVisible(error, true);
            }
          });
    } catch (RequestException e) {
      UIObject.setVisible(error, true);
    }
  }
}