package com.mayhew3.drafttower.server.database.player;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class FieldConversionTimestamp extends FieldConversion<Timestamp> {
  @Override
  Timestamp parseFromString(String value) {
    if (value == null) {
      return null;
    }

    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    try {
      return new Timestamp(simpleDateFormat.parse(value).getTime());
    } catch (ParseException e) {
      e.printStackTrace();
      throw new RuntimeException("Unable to parse date " + value);
    }
  }
}
