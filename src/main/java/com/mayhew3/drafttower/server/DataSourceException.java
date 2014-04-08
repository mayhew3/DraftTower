package com.mayhew3.drafttower.server;

/**
 * Exception occurring in data source.
 */
public class DataSourceException extends Exception {
  public DataSourceException() {
  }

  public DataSourceException(String message) {
    super(message);
  }

  public DataSourceException(String message, Throwable cause) {
    super(message, cause);
  }

  public DataSourceException(Throwable cause) {
    super(cause);
  }
}