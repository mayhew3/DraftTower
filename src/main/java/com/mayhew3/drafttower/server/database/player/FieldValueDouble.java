package com.mayhew3.drafttower.server.database.player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class FieldValueDouble extends FieldValue<Double> {
  public FieldValueDouble(String fieldName, FieldConversion<Double> converter) {
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
}
