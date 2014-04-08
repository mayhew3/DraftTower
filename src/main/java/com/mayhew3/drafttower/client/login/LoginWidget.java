package com.mayhew3.drafttower.client.login;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.mayhew3.drafttower.shared.LoginResponse;

import javax.inject.Singleton;

/**
 * Login form.
 */
@Singleton
public class LoginWidget extends Composite implements LoginView {

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

  private final LoginPresenter presenter;

  @UiField TextBox username;
  @UiField PasswordTextBox password;
  @UiField Button login;
  @UiField DivElement invalidLogin;
  @UiField DivElement alreadyLoggedIn;

  @Inject
  public LoginWidget(LoginPresenter presenter) {
    initWidget(uiBinder.createAndBindUi(this));
    UIObject.setVisible(invalidLogin, false);
    UIObject.setVisible(alreadyLoggedIn, false);

    this.presenter = presenter;

    presenter.setLoginView(this);
    presenter.setStoredTeamToken(Cookies.getCookie(LoginResponse.TEAM_TOKEN_COOKIE));
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

  void doLogin() {
    UIObject.setVisible(invalidLogin, false);
    UIObject.setVisible(alreadyLoggedIn, false);
    presenter.doLogin(username.getValue(), password.getValue());
  }

  @Override
  public void focusUsername() {
    username.setFocus(true);
  }

  @Override
  public void alreadyLoggedIn() {
    UIObject.setVisible(alreadyLoggedIn, true);
  }

  @Override
  public void invalidLogin() {
    UIObject.setVisible(invalidLogin, true);
  }


  @Override
  protected void onEnsureDebugId(String baseID) {
    super.onEnsureDebugId(baseID);
    username.ensureDebugId(baseID + "-username");
    password.ensureDebugId(baseID + "-password");
    login.ensureDebugId(baseID + "-login");
    UIObject.ensureDebugId(invalidLogin, baseID + "-invalid");
    UIObject.ensureDebugId(alreadyLoggedIn, baseID + "-already");
  }
}