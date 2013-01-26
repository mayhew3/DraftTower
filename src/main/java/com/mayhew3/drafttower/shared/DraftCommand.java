package com.mayhew3.drafttower.shared;

/**
 * Message object for sending draft actions from client to server.
 */
public interface DraftCommand {

  public enum Command {
    START_DRAFT,
    DO_PICK,
    PAUSE,
    RESUME,
  }

  Command getCommandType();
  void setCommandType(Command command);
}