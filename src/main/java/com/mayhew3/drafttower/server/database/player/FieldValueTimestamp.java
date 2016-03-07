package com.mayhew3.drafttower.server.database.player;

import com.sun.istack.internal.Nullable;

import java.sql.*;
import java.util.Date;

public class FieldValueTimestamp extends FieldValue<Timestamp> {
  public FieldValueTimestamp(String fieldName, FieldConversion<Timestamp> converter) {
    super(fieldName, converter);
  }

  @Override
  protected void initializeValue(ResultSet resultSet) throws SQLException {
    initializeValue(resultSet.getTimestamp(getFieldName()));
  }

  @Override
  public void updatePreparedStatement(PreparedStatement preparedStatement, int currentIndex) throws SQLException {
    if (getChangedValue() == null) {
      preparedStatement.setNull(currentIndex, Types.TIMESTAMP);
    } else {
      preparedStatement.setTimestamp(currentIndex, getChangedValue());
    }
  }

  public void changeValue(@Nullable Date date) {
    if (date == null) {
      changeValue(null);
    } else {
      Timestamp timestamp = new Timestamp(date.getTime());
      changeValue(timestamp);
    }
  }

  public void changeValueFromXMLString(String xmlString) {
    if (xmlString != null) {
      long numberOfSeconds = Long.decode(xmlString);
      changeValue(new Timestamp(numberOfSeconds * 1000));
    }
  }
}
