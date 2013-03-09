package com.mayhew3.drafttower.shared;

/**
 * Display info about a team.
 */
public interface Team {

  String getShortName();
  void setShortName(String shortName);

  String getLongName();
  void setLongName(String longName);
}