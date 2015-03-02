package com.mayhew3.drafttower.server.database;

public class PlayerField {
  public String fieldName;
  public Object fieldValue;

  public PlayerField(String fieldName, Object fieldValue) {
    this.fieldName = fieldName;
    this.fieldValue = fieldValue;
  }
}