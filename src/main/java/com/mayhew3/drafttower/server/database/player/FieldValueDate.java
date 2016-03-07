package com.mayhew3.drafttower.server.database.player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

public class FieldValueDate extends FieldValue<Date> {
  public FieldValueDate(String fieldName, FieldConversion<Date> converter) {
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

  public void changeValueFromXMLString(String xmlString) {
    if (xmlString != null) {
      long numberOfSeconds = Long.decode(xmlString);
      changeValue(new Date(numberOfSeconds * 1000));
    }
  }
}
