package com.mayhew3.drafttower.shared;

/**
 * Enumerates reasons why a socket might have been closed by the server.
 */
public enum SocketTerminationReason {
  BAD_TEAM_TOKEN(4001, "Bad team token", true),
  TEAM_ALREADY_CONNECTED(4002, "Team already connected", true),
  COMMISH_FORCED(4003, "Disconnected by commissioner", true),
  UNKNOWN_REASON(-1, "", false);

  private final int closeCode;
  private final String msg;
  private final boolean shouldReload;

  SocketTerminationReason(int closeCode, String msg, boolean shouldReload) {
    this.closeCode = closeCode;
    this.msg = msg;
    this.shouldReload = shouldReload;
  }

  public int getCloseCode() {
    return closeCode;
  }

  public String getMessage() {
    return msg;
  }

  public boolean shouldReload() {
    return shouldReload;
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
