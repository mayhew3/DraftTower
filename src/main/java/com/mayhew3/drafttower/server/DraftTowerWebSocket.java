package com.mayhew3.drafttower.server;

import com.google.common.base.Function;
import com.mayhew3.drafttower.shared.DraftCommand;

/**
 * Interface for server side of websocket-based client-server communication.
 */
public interface DraftTowerWebSocket {
  interface DraftCommandListener {
    void onDraftCommand(DraftCommand cmd) throws TerminateSocketException;
    void onClientDisconnected(String playerToken);
  }

  void addListener(DraftCommandListener listener);

  void sendMessage(Function<? super String, String> messageForTeamToken);

  void forceDisconnect(String teamToken);
}