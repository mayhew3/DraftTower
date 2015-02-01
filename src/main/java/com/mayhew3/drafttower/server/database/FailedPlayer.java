package com.mayhew3.drafttower.server.database;

public class FailedPlayer extends Exception {
  public int id;
  public String message;

  public FailedPlayer(int id, String message) {
    this.id = id;
    this.message = message;
  }
}
