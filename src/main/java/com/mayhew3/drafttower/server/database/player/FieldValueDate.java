package com.mayhew3.drafttower.server.database.player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

public class FieldValueDate extends FieldValue<Date> {
  public FieldValueDate(String fieldName, FieldConversion<Date> converter) {
    super(fieldName, converter);
  }

  @Override
  protected void initializeValue(ResultSet resultSet) throws SQLException {
    initializeValue(resultSet.getDate(getFieldName()));
  }

  @Override
  public void updatePreparedStatement(PreparedStatement preparedStatement, int currentIndex) throws SQLException {
    if (getChangedValue() == null) {
      preparedStatement.setNull(currentIndex, Types.DATE);
    } else {
      java.sql.Date sqlDate = new java.sql.Date(getChangedValue().getTime());
      preparedStatement.setDate(currentIndex, sqlDate);
    }
  }

  public void changeValueFromXMLString(String xmlString) {
    if (xmlString != null) {
      long numberOfSeconds = Long.decode(xmlString);
      changeValue(new Date(numberOfSeconds * 1000));
    }
  }
}
