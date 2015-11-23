package com.mayhew3.drafttower.shared;

/**
 * Message object for sending draft actions from client to server.
 */
public interface DraftCommand {

  enum Command {
    IDENTIFY(false),
    START_DRAFT(true),
    DO_PICK(false),
    PAUSE(true),
    RESUME(true),
    BACK_OUT(true),
    FORCE_PICK(true),
    WAKE_UP(false),
    RESET_DRAFT(true),
    CLEAR_CACHES(true),
    DISCONNECT_CLIENT(true);

    private final boolean commissionerOnly;

    Command(boolean commissionerOnly) {
      this.commissionerOnly = commissionerOnly;
    }

    public boolean isCommissionerOnly() {
      return commissionerOnly;
    }
  }

  Command getCommandType();
  void setCommandType(Command command);

  String getTeamToken();
  void setTeamToken(String teamToken);

  Long getPlayerId();
  void setPlayerId(Long playerId);
}