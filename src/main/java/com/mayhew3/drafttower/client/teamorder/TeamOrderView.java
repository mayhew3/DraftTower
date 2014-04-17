package com.mayhew3.drafttower.client.teamorder;

/**
 * View interface for team order widget.
 */
public interface TeamOrderView {
  void setTeamName(int teamNum, String teamName);

  void setRoundText(String text);

  void setMe(int teamNum, boolean me);

  void setCurrent(int teamNum, boolean current);

  void setDisconnected(int teamNum, boolean disconnected);

  void setRobot(int teamNum, boolean robot);

  void setKeeper(int teamNum, boolean keeper);

  void setStatus(String status);
}