package com.mayhew3.drafttower.server.database;

import com.google.common.collect.Lists;
import com.mayhew3.drafttower.server.database.player.FieldValue;
import com.sun.istack.internal.NotNull;

import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

public class MySQLConnection implements SQLConnection {

  private Connection _connection;

  MySQLConnection(Connection connection) {
    _connection = connection;
  }


  // Simple executes without use of PreparedStatement

  @Override
  @NotNull
  public ResultSet executeQuery(String sql) throws SQLException {
    Statement statement = _connection.createStatement();
    return statement.executeQuery(sql);
  }

  @Override
  @NotNull
  public Statement executeUpdate(String sql) throws SQLException {
    Statement statement = _connection.createStatement();

    statement.executeUpdate(sql);
    return statement;
  }

  @Override
  public void closeConnection() throws SQLException {
    _connection.close();
  }


  // Full lifecycle operations using PreparedStatement



  @Override
  public ResultSet prepareAndExecuteStatementFetch(String sql, Object... params) throws SQLException {
    return prepareAndExecuteStatementFetch(sql, Lists.newArrayList(params));
  }

  @Override
  public ResultSet prepareAndExecuteStatementFetch(String sql, List<Object> params) throws SQLException {
    PreparedStatement preparedStatement = prepareStatementWithParams(sql, params);
    return preparedStatement.executeQuery();
  }


  @Override
  public void prepareAndExecuteStatementUpdate(String sql, Object... params) throws SQLException {
    prepareAndExecuteStatementUpdate(sql, Lists.newArrayList(params));
  }

  @Override
  public void prepareAndExecuteStatementUpdate(String sql, List<Object> params) throws SQLException {
    PreparedStatement preparedStatement = prepareStatementWithParams(sql, params);

    preparedStatement.executeUpdate();
    preparedStatement.close();
  }





  // Operations with user handle on PreparedStatement

  @Override
  public PreparedStatement prepareStatementWithParams(String sql, List<Object> params) throws SQLException {
    PreparedStatement preparedStatement = _connection.prepareStatement(sql);
    return plugParamsIntoStatement(preparedStatement, params);
  }


  @Override
  public ResultSet executePreparedStatementWithParams(PreparedStatement preparedStatement, Object... params) throws SQLException {
    List<Object> paramList = Lists.newArrayList(params);
    return executePreparedStatementWithParams(preparedStatement, paramList);
  }

  @Override
  public ResultSet executePreparedStatementWithParams(PreparedStatement preparedStatement, List<Object> params) throws SQLException {
    PreparedStatement statementWithParams = plugParamsIntoStatement(preparedStatement, params);
    return statementWithParams.executeQuery();
  }

  public void executePreparedUpdateWithParams(PreparedStatement preparedStatement, List<Object> paramList) throws SQLException {
    PreparedStatement statementWithParams = plugParamsIntoStatement(preparedStatement, paramList);
    statementWithParams.executeUpdate();
  }



  // Using FieldValue


  @Override
  public PreparedStatement prepareStatementWithFields(String sql, List<FieldValue> fields) throws SQLException {
    PreparedStatement preparedStatement = _connection.prepareStatement(sql);
    return plugFieldsIntoStatement(preparedStatement, fields);
  }

  @Override
  public void prepareAndExecuteStatementUpdateWithFields(String sql, List<FieldValue> fields) throws SQLException {
    PreparedStatement preparedStatement = prepareStatementWithFields(sql, fields);

    preparedStatement.executeUpdate();
    preparedStatement.close();
  }

  @Override
  public Integer prepareAndExecuteStatementInsertReturnId(String sql, List<FieldValue> fieldValues) throws SQLException {
    PreparedStatement preparedStatement = prepareStatementForInsertId(sql);
    plugFieldsIntoStatement(preparedStatement, fieldValues);

    preparedStatement.executeUpdate();

    ResultSet generatedKeys = preparedStatement.getGeneratedKeys();

    if (!generatedKeys.next()) {
      throw new RuntimeException("No rows in ResultSet from Inserted object!");
    }

    int id = generatedKeys.getInt(1);
    preparedStatement.close();
    return id;
  }


  @Override
  public void executePreparedUpdateWithFields(PreparedStatement preparedStatement, List<FieldValue> fieldValues) throws SQLException {
    plugFieldsIntoStatement(preparedStatement, fieldValues);
    preparedStatement.executeUpdate();
  }


  // unused but useful

  public boolean columnExists(String tableName, String columnName) throws SQLException {
    return _connection.getMetaData().getColumns(null, null, tableName, columnName).next();
  }



  // utility methods

  private PreparedStatement plugFieldsIntoStatement(PreparedStatement preparedStatement, List<FieldValue> fieldValues) throws SQLException {
    int i = 1;
    for (FieldValue fieldValue : fieldValues) {
      fieldValue.updatePreparedStatement(preparedStatement, i);
      i++;
    }
    return preparedStatement;
  }

  private PreparedStatement plugParamsIntoStatement(PreparedStatement preparedStatement, List<Object> params) throws SQLException {
    int i = 1;
    for (Object param : params) {
      if (param instanceof String) {
        preparedStatement.setString(i, (String) param);
      } else if (param instanceof Integer) {
        preparedStatement.setInt(i, (Integer) param);
      } else if (param instanceof BigDecimal) {
        preparedStatement.setBigDecimal(i, (BigDecimal) param);
      } else if (param instanceof Double) {
        preparedStatement.setDouble(i, (Double) param);
      } else if (param instanceof Timestamp) {
        preparedStatement.setTimestamp(i, (Timestamp) param);
      } else if (param instanceof Boolean) {
        preparedStatement.setBoolean(i, (Boolean) param);
      } else {
        throw new RuntimeException("Unknown type of param: " + param.getClass());
      }
      i++;
    }
    return preparedStatement;
  }

  PreparedStatement prepareStatementForInsertId(String sql) throws SQLException {
    return _connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
  }

}
