package com.mayhew3.drafttower.shared;

/**
 * Message object for sending draft actions from client to server.
 */
public interface DraftCommand {

  public enum Command {
    IDENTIFY(false),
    START_DRAFT(true),
    DO_PICK(false),
    PAUSE(true),
    RESUME(true),
    BACK_OUT(true),
    FORCE_PICK(true);

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

  long getPlayerId();
  void setPlayerId(long playerId);
}