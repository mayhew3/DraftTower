package com.mayhew3.drafttower.server;

/**
 * Exception thrown when a connection is invalid and should be terminated.
 */
public class TerminateSocketException extends Exception {
  public TerminateSocketException(String message) {
    super(message);
  }
}