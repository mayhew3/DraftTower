package com.mayhew3.drafttower.server.database.player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class FieldValueInteger extends FieldValue<Integer> {
  public FieldValueInteger(String fieldName, FieldConversion<Integer> converter) {
    super(fieldName, converter);
  }

  @Override
  protected void initializeValue(ResultSet resultSet) throws SQLException {
    Integer resultSetInt = resultSet.getInt(getFieldName());
    if (resultSet.wasNull()) {
      resultSetInt = null;
    }
    initializeValue(resultSetInt);
  }

  @Override
  public void updatePreparedStatement(PreparedStatement preparedStatement, int currentIndex) throws SQLException {
    if (getChangedValue() == null) {
      preparedStatement.setNull(currentIndex, Types.INTEGER);
    } else {
      preparedStatement.setInt(currentIndex, getChangedValue());
    }
  }

  public void increment(Integer numberToAdd) {
    Integer value = getValue();
    if (value == null) {
      value = 0;
    }
    value += numberToAdd;
    changeValue(value);
  }
}
