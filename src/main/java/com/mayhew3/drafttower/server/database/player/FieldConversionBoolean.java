package com.mayhew3.drafttower.server.database.player;

public class FieldConversionBoolean extends FieldConversion<Boolean> {
  @Override
  Boolean parseFromString(String value) {
    return value == null ? false : Boolean.valueOf(value);
  }
}