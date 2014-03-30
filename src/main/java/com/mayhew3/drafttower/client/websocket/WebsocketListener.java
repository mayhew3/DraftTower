package com.mayhew3.drafttower.client.websocket;

import com.mayhew3.drafttower.shared.SocketTerminationReason;

/**
 * Adapted from com.sksamuel.gwt.websockets.
 */
public interface WebsocketListener {

	void onClose(SocketTerminationReason reason);

	void onMessage(String msg);

	void onOpen();
}
