package com.mayhew3.drafttower.server.database.dataobject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class FieldValueBoolean extends FieldValue<Boolean> {
  private Boolean allowNulls = false;

  public FieldValueBoolean(String fieldName, FieldConversion<Boolean> converter) {
    super(fieldName, converter);
  }
  public FieldValueBoolean(String fieldName, FieldConversion<Boolean> converter, Boolean allowNulls) {
    super(fieldName, converter);
    this.allowNulls = allowNulls;
  }

  @Override
  public void initializeValue(Boolean value) {
    if (allowNulls) {
      super.initializeValue(value);
    } else {
      super.initializeValue((value == null) ? false : value);
    }
  }

  @Override
  protected void initializeValue(ResultSet resultSet) throws SQLException {
    initializeValue(resultSet.getBoolean(getFieldName()));
  }

  @Override
  public void updatePreparedStatement(PreparedStatement preparedStatement, int currentIndex) throws SQLException {
    if (getChangedValue() == null) {
      preparedStatement.setNull(currentIndex, Types.BOOLEAN);
    } else {
      preparedStatement.setBoolean(currentIndex, getChangedValue());
    }
  }
}
