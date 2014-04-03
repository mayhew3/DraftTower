package com.mayhew3.drafttower.client;

import com.google.common.base.Function;
import com.mayhew3.drafttower.shared.LoginResponse;
import com.mayhew3.drafttower.shared.SocketTerminationReason;

/**
 * Interface for all HTTP-based server requests.
 */
public interface ServerRpc {

  public void sendLoginRequest(String username,
      String password,
      Function<LoginResponse, Void> successCallback,
      Function<SocketTerminationReason, Void> failureCallback);
}