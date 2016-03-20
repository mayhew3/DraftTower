package com.mayhew3.drafttower.server.database.dataobject;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class FieldValueBigDecimal extends FieldValue<BigDecimal> {
  public FieldValueBigDecimal(String fieldName, FieldConversion<BigDecimal> converter) {
    super(fieldName, converter);
  }

  @Override
  protected void initializeValue(ResultSet resultSet) throws SQLException {
    initializeValue(resultSet.getBigDecimal(getFieldName()));
  }

  @Override
  public void updatePreparedStatement(PreparedStatement preparedStatement, int currentIndex) throws SQLException {
    if (getChangedValue() == null) {
      preparedStatement.setNull(currentIndex, Types.NUMERIC);
    } else {
      preparedStatement.setBigDecimal(currentIndex, getChangedValue());
    }
  }

  public void changeValue(Double newValue) {
    if (newValue == null) {
      changeValue((BigDecimal) null);
    } else {
      changeValue(BigDecimal.valueOf(newValue));
    }
  }
}
