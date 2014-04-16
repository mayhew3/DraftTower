package com.mayhew3.drafttower.client.serverrpc;

import com.google.common.base.Function;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.mayhew3.drafttower.shared.*;

/**
 * Interface for all HTTP-based server requests.
 */
public interface ServerRpc {

  public void sendLoginRequest(String username,
      String password,
      Function<LoginResponse, Void> successCallback,
      Function<SocketTerminationReason, Void> failureCallback);

  public void sendGetPlayerQueueRequest(AutoBean<GetPlayerQueueRequest> requestBean,
      Function<GetPlayerQueueResponse, Void> callback);

  public void sendEnqueueOrDequeueRequest(String action,
      AutoBean<EnqueueOrDequeuePlayerRequest> requestBean,
      Runnable callback);

  public void sendReorderQueueRequest(AutoBean<ReorderPlayerQueueRequest> requestBean,
      Runnable callback);

  public void sendGraphsRequest(AutoBean<GetGraphsDataRequest> requestBean,
      Function<GraphsData, Void> callback);

  public void sendPlayerListRequest(AutoBean<UnclaimedPlayerListRequest> requestBean,
      Function<UnclaimedPlayerListResponse, Void> callback);

  public void sendChangePlayerRankRequest(AutoBean<ChangePlayerRankRequest> requestBean,
      Runnable callback);

  public void sendCopyRanksRequest(AutoBean<CopyAllPlayerRanksRequest> requestBean,
      Runnable callback);

  public void sendSetWizardTableRequest(AutoBean<SetWizardTableRequest> requestBean,
      Runnable callback);
}