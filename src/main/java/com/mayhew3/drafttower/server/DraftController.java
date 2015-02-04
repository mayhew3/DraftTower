package com.mayhew3.drafttower.server;

import com.mayhew3.drafttower.shared.DraftStatus;

/**
 * Class responsible for tracking draft state and handling commands from clients.
 */
public interface DraftController extends
    DraftTowerWebSocket.DraftCommandListener,
    DraftTimer.Listener {
  public interface DraftStatusListener {
    void onDraftStatusChanged(DraftStatus draftStatus);
  }

  void addListener(DraftStatusListener listener);
}