package com.mayhew3.drafttower.server;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Callback which operates on a {@link ResultSet}.
 */
public interface ResultSetCallback {
  void onResultSet(ResultSet resultSet) throws SQLException, DataSourceException;
}