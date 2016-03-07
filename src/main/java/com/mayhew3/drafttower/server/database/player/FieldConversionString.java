package com.mayhew3.drafttower.server.database.player;

public class FieldConversionString extends FieldConversion<String> {
  @Override
  String parseFromString(String value) {
    return value;
  }
}
