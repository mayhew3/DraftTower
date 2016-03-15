package com.mayhew3.drafttower.server.database.dataobject;

public class FieldConversionString extends FieldConversion<String> {
  @Override
  String parseFromString(String value) {
    return value;
  }
}
