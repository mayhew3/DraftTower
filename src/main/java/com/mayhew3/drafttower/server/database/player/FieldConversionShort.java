package com.mayhew3.drafttower.server.database.player;

public class FieldConversionShort extends FieldConversion<Short> {
  @Override
  Short parseFromString(String value) {
    if (value == null) {
      return null;
    }
    return Short.valueOf(value);
  }
}
