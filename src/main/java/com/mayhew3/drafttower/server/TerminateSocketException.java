package com.mayhew3.drafttower.server;

import com.mayhew3.drafttower.shared.SocketTerminationReason;

/**
 * Exception thrown when a connection is invalid and should be terminated.
 */
public class TerminateSocketException extends Exception {
  private final SocketTerminationReason reason;

  public TerminateSocketException(SocketTerminationReason reason) {
    super(reason.getMessage());
    this.reason = reason;
  }

  public SocketTerminationReason getReason() {
    return reason;
  }
}