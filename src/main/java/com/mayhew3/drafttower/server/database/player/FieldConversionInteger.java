package com.mayhew3.drafttower.server.database.player;

public class FieldConversionInteger extends FieldConversion<Integer> {
  @Override
  Integer parseFromString(String value) {
    if (value == null) {
      return null;
    }
    return Integer.valueOf(value);
  }
}
