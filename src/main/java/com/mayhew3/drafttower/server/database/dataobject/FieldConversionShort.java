package com.mayhew3.drafttower.server.database.dataobject;

public class FieldConversionShort extends FieldConversion<Short> {
  @Override
  Short parseFromString(String value) {
    if (value == null) {
      return null;
    }
    return Short.valueOf(value);
  }
}
