package com.mayhew3.drafttower.client.login;

import com.google.common.base.Function;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Cookies;
import com.mayhew3.drafttower.client.TeamsInfo;
import com.mayhew3.drafttower.client.events.LoginEvent;
import com.mayhew3.drafttower.client.serverrpc.ServerRpc;
import com.mayhew3.drafttower.shared.LoginResponse;
import com.mayhew3.drafttower.shared.ServletEndpoints;
import com.mayhew3.drafttower.shared.SocketTerminationReason;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LoginPresenter {

  private final ServerRpc serverRpc;
  private final TeamsInfo teamsInfo;
  private final EventBus eventBus;
  private LoginView loginView;

  @Inject
  public LoginPresenter(ServerRpc serverRpc,
      TeamsInfo teamsInfo,
      EventBus eventBus) {
    this.serverRpc = serverRpc;
    this.teamsInfo = teamsInfo;
    this.eventBus = eventBus;
  }

  public void setLoginView(LoginView loginView) {
    this.loginView = loginView;
  }

  void setStoredTeamToken(String storedTeamToken) {
    if (storedTeamToken != null) {
      doAutoLogin();
    } else {
      Scheduler.get().scheduleDeferred(new ScheduledCommand() {
        @Override
        public void execute() {
          loginView.focusUsername();
        }
      });
    }
  }

  void doLogin(String username, String password) {
    doLogin(username, password,
        new Function<SocketTerminationReason, Void>() {
          @Override
          public Void apply(SocketTerminationReason reason) {
            loginFailed(reason);
            return null;
          }
        });
  }

  void doGuestLogin() {
    doLogin(ServletEndpoints.LOGIN_GUEST, "");
  }

  private void doAutoLogin() {
    loginView.setVisible(false);
    doLogin("", "",
        new Function<SocketTerminationReason, Void>() {
          @Override
          public Void apply(SocketTerminationReason reason) {
            autoLoginFailed();
            return null;
          }
        });
  }

  private void doLogin(String username, String password,
      final Function<SocketTerminationReason, Void> failureCallback) {
    serverRpc.sendLoginRequest(username, password,
        new Function<LoginResponse, Void>() {
          @Override
          public Void apply(LoginResponse loginResponse) {
            teamsInfo.setLoginResponse(loginResponse);
            eventBus.fireEvent(new LoginEvent(loginResponse));
            return null;
          }
        },
        failureCallback);
  }

  private void autoLoginFailed() {
    Cookies.removeCookie(LoginResponse.TEAM_TOKEN_COOKIE);
    loginView.setVisible(true);
    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
      @Override
      public void execute() {
        loginView.focusUsername();
      }
    });
  }

  private void loginFailed(SocketTerminationReason reason) {
    Cookies.removeCookie(LoginResponse.TEAM_TOKEN_COOKIE);
    if (reason == SocketTerminationReason.TEAM_ALREADY_CONNECTED) {
      loginView.alreadyLoggedIn();
    } else {
      loginView.invalidLogin();
    }
  }

  public void logout() {
    Cookies.removeCookie(LoginResponse.TEAM_TOKEN_COOKIE);
  }
}