package com.mayhew3.drafttower.server.database;

public enum PlayerType {
  BATTER("batter"),
  PITCHER("pitcher");

  public String name;

  PlayerType(String batter) {
    name = batter;
  }
}
