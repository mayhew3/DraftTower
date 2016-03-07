package com.mayhew3.drafttower.server.database.player;

import java.math.BigDecimal;

public class FieldConversionBigDecimal extends FieldConversion<BigDecimal> {
  @Override
  BigDecimal parseFromString(String value) {
    return value == null ? null : new BigDecimal(value);
  }
}
