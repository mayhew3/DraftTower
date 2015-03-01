package com.mayhew3.drafttower.shared;

/**
 * Request object sent when user sets min or max closers.
 */
public interface SetCloserLimitRequest {

  String getTeamToken();
  void setTeamToken(String teamToken);

  int getMinClosers();
  void setMinClosers(int minClosers);

  int getMaxClosers();
  void setMaxClosers(int maxClosers);
}