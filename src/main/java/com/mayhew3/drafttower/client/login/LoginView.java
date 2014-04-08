package com.mayhew3.drafttower.client.login;

/**
 * View interface for {@link LoginWidget}.
 */
public interface LoginView {
  void setVisible(boolean visible);

  void focusUsername();

  void alreadyLoggedIn();

  void invalidLogin();
}