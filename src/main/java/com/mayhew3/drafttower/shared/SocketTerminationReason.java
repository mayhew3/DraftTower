package com.mayhew3.drafttower.shared;

/**
 * Enumerates reasons why a socket might have been closed by the server.
 */
public enum SocketTerminationReason {
  BAD_TEAM_TOKEN(4001, "Bad team token"),
  TEAM_ALREADY_CONNECTED(4002, "Team already connected"),
  UNKNOWN_REASON(-1, "");

  private final int closeCode;
  private final String msg;

  SocketTerminationReason(int closeCode, String msg) {
    this.closeCode = closeCode;
    this.msg = msg;
  }

  public int getCloseCode() {
    return closeCode;
  }

  public String getMessage() {
    return msg;
  }

  public static SocketTerminationReason fromCloseCode(int closeCode) {
    for (SocketTerminationReason reason : values()) {
      if (reason.closeCode == closeCode) {
        return reason;
      }
    }
    return UNKNOWN_REASON;
  }
}
