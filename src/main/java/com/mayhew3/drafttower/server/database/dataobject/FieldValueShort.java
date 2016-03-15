package com.mayhew3.drafttower.server.database.dataobject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FieldValueShort extends FieldValue<Short> {
  public FieldValueShort(String fieldName, FieldConversion<Short> converter) {
    super(fieldName, converter);
  }

  @Override
  protected void initializeValue(ResultSet resultSet) {
    throw new IllegalStateException("Cannot select Postgres DB with Mongo value.");
  }

  @Override
  public void updatePreparedStatement(PreparedStatement preparedStatement, int currentIndex) {
    throw new IllegalStateException("Cannot update Postgres DB with Mongo value.");
  }

  public void increment(Short numberToAdd) {
    Short value = getValue();
    if (value == null) {
      value = 0;
    }
    value = (short) (value + numberToAdd);
    changeValue(value);
  }
}
