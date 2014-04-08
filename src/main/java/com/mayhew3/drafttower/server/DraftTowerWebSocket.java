package com.mayhew3.drafttower.server;

import com.mayhew3.drafttower.shared.DraftCommand;

/**
 * Interface for server side of websocket-based client-server communication.
 */
public interface DraftTowerWebSocket {
  public interface DraftCommandListener {
    void onClientConnected();
    void onDraftCommand(DraftCommand cmd) throws TerminateSocketException;
    void onClientDisconnected(String playerToken);
  }

  void addListener(DraftCommandListener listener);

  void sendMessage(String message);
}