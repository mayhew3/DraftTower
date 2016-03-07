package com.mayhew3.drafttower.server.database.player;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FieldConversionDate extends FieldConversion<Date> {
  @Override
  Date parseFromString(String value) {
    if (value == null) {
      return null;
    }

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    try {
      return simpleDateFormat.parse(value);
    } catch (ParseException e) {
      e.printStackTrace();
      throw new RuntimeException("Unable to parse date " + value);
    }
  }
}
