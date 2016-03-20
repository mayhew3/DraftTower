package com.mayhew3.drafttower.server.database;


import com.google.common.collect.Lists;
import com.sun.istack.internal.NotNull;

import java.sql.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConnection {
  private Connection _connection;

  protected final Logger logger = Logger.getLogger(DatabaseUtility.class.getName());

  public DatabaseConnection() {
    initConnection();
  }

  public void initConnection() {
    logger.log(Level.INFO, "Initializing connection.");

    try {
      Class.forName("com.mysql.jdbc.Driver");
    } catch (ClassNotFoundException e) {
      System.out.println("Cannot find MySQL drivers. Exiting.");
      throw new RuntimeException(e.getLocalizedMessage());
    }

    try {
      _connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/uncharted", "root", "m3mysql");
    } catch (SQLException e) {
      System.out.println("Cannot connect to database. Exiting.");
      throw new RuntimeException(e.getLocalizedMessage());
    }
  }

  @NotNull
  protected ResultSet executeQuery(String sql) {
    try {
      Statement statement = _connection.createStatement();
      return statement.executeQuery(sql);
    } catch (SQLException e) {
      System.out.println("Error running SQL select: " + sql);
      e.printStackTrace();
      System.exit(-1);
    }
    return null;
  }

  @NotNull
  protected ResultSet executeQueryWithException(String sql) throws SQLException {
    Statement statement = _connection.createStatement();
    return statement.executeQuery(sql);
  }

  @NotNull
  protected Statement executeUpdate(String sql) {
    try {
      Statement statement = _connection.createStatement();

      statement.executeUpdate(sql);
      return statement;
    } catch (SQLException e) {
      throw new IllegalStateException("Error running SQL select: " + sql);
    }
  }

  @NotNull
  protected Statement executeUpdateWithException(String sql) throws SQLException {
    Statement statement = _connection.createStatement();

    statement.executeUpdate(sql);
    return statement;
  }

  protected boolean hasMoreElements(ResultSet resultSet) {
    try {
      return resultSet.next();
    } catch (SQLException e) {
      throw new IllegalStateException("Error fetching next row from result set.");
    }
  }

  protected int getInt(ResultSet resultSet, String columnName) {
    try {
      return resultSet.getInt(columnName);
    } catch (SQLException e) {
      throw new RuntimeException("Error trying to get integer column " + columnName + ": " + e.getLocalizedMessage());
    }
  }

  protected String getString(ResultSet resultSet, String columnName) {
    try {
      return resultSet.getString(columnName);
    } catch (SQLException e) {
      throw new IllegalStateException("Error trying to get string column " + columnName);
    }
  }

  protected boolean columnExists(String tableName, String columnName) {
    try {
      ResultSet tables = _connection.getMetaData().getColumns(null, null, tableName, columnName);
      return tables.next();
    } catch (SQLException e) {
      throw new IllegalStateException("Error trying to find column " + columnName);
    }
  }

  protected ResultSet prepareAndExecuteStatementFetch(String sql, Object... params) {
    return prepareAndExecuteStatementFetch(sql, Lists.newArrayList(params));
  }

  protected ResultSet prepareAndExecuteStatementFetch(String sql, List<Object> params) {
    PreparedStatement preparedStatement = prepareStatement(sql, params);
    try {
      return preparedStatement.executeQuery();
    } catch (SQLException e) {
      throw new RuntimeException("Error executing prepared statement for SQL: " + sql + ": " + e.getLocalizedMessage());
    }
  }

  protected ResultSet prepareAndExecuteStatementFetchWithException(String sql, List<Object> params) throws SQLException {
    PreparedStatement preparedStatement = prepareStatement(sql, params);
    return preparedStatement.executeQuery();
  }

  protected void prepareAndExecuteStatementUpdate(String sql, Object... params) {
    try {
      PreparedStatement preparedStatement = prepareStatement(sql, Lists.newArrayList(params));

      preparedStatement.executeUpdate();
      preparedStatement.close();
    } catch (SQLException e) {
      throw new RuntimeException("Error preparing statement for SQL: " + sql + ": " + e.getLocalizedMessage());
    }
  }

  protected void prepareAndExecuteStatementUpdateWithException(String sql, List<Object> params) throws SQLException {
    PreparedStatement preparedStatement = prepareStatement(sql, params);

    preparedStatement.executeUpdate();
    preparedStatement.close();
  }

  protected PreparedStatement prepareStatement(String sql, List<Object> params) {
    PreparedStatement preparedStatement = getPreparedStatement(sql);
    try {
      return plugParamsIntoStatement(preparedStatement, params);
    } catch (SQLException e) {
      throw new RuntimeException("Error adding parameters to prepared statement for SQL: " + sql + ": " + e.getLocalizedMessage());
    }
  }

  public PreparedStatement getPreparedStatement(String sql) {
    try {
      return _connection.prepareStatement(sql);
    } catch (SQLException e) {
      throw new RuntimeException("Error preparing statement for SQL: " + sql + ": " + e.getLocalizedMessage());
    }
  }

  protected ResultSet executePreparedStatementAlreadyHavingParameters(PreparedStatement preparedStatement) {
    try {
      return preparedStatement.executeQuery();
    } catch (SQLException e) {
      throw new RuntimeException("Error executing prepared statement. " + e.getLocalizedMessage());
    }
  }

  public ResultSet executePreparedStatementWithParams(PreparedStatement preparedStatement, Object... params) {
    List<Object> paramList = Lists.newArrayList(params);
    return executePreparedStatementWithParams(preparedStatement, paramList);
  }

  public ResultSet executePreparedStatementWithParams(PreparedStatement preparedStatement, List<Object> params) {
    try {
      PreparedStatement statementWithParams = plugParamsIntoStatement(preparedStatement, params);
      return statementWithParams.executeQuery();
    } catch (SQLException e) {
      throw new RuntimeException("Error executing prepared statement with params: " + params + ": " + e.getLocalizedMessage());
    }
  }

  public void executePreparedUpdateWithParams(PreparedStatement preparedStatement, Object... params) {
    List<Object> paramList = Lists.newArrayList(params);
    try {
      PreparedStatement statementWithParams = plugParamsIntoStatement(preparedStatement, paramList);
      statementWithParams.executeUpdate();
      statementWithParams.close();
    } catch (SQLException e) {
      throw new RuntimeException("Error executing prepared statement with params: " + paramList + ": " + e.getLocalizedMessage());
    }
  }
/*

  public void executePreparedUpdateWithParamsWithoutClose(PreparedStatement preparedStatement, Object... params) {
    List<Object> paramList = Lists.newArrayList(params);
    executePreparedUpdateWithParamsWithoutClose(preparedStatement, paramList);
  }
*/

  public void executePreparedUpdateWithParamsWithoutClose(PreparedStatement preparedStatement, List<Object> paramList) {
    try {
      PreparedStatement statementWithParams = plugParamsIntoStatement(preparedStatement, paramList);
      statementWithParams.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Error executing prepared statement with params: " + paramList + ": " + e.getLocalizedMessage());
    }
  }

  private PreparedStatement plugParamsIntoStatement(PreparedStatement preparedStatement, List<Object> params) throws SQLException {
    int i = 1;
    for (Object param : params) {
      if (param instanceof String) {
        preparedStatement.setString(i, (String) param);
      } else if (param instanceof Integer) {
        preparedStatement.setInt(i, (Integer) param);
      } else if (param instanceof java.util.Date) {
        preparedStatement.setDate(i, new Date(((java.util.Date) param).getTime()));
      } else {
        throw new RuntimeException("Unknown type of param: " + param.getClass());
      }
      i++;
    }
    return preparedStatement;
  }

  protected void setString(PreparedStatement preparedStatement, int index, String value) {
    try {
      preparedStatement.setString(index, value);
    } catch (SQLException e) {
      throw new RuntimeException("Error binding parameter " + index + " on statement to value " + value + ": " + e.getLocalizedMessage());
    }
  }

  public boolean hasConnection() {
    boolean isOpen;
    try {
      isOpen = _connection != null && !_connection.isClosed();
    } catch (SQLException e) {
      return false;
    }
    return isOpen;
  }
}

