package com.mayhew3.drafttower.server;

/**
 * Class responsible for tracking draft state and handling commands from clients.
 */
public interface DraftController extends
    DraftTowerWebSocketServlet.DraftCommandListener,
    DraftTimer.Listener {
}