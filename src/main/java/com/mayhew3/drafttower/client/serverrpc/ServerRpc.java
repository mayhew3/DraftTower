package com.mayhew3.drafttower.client.serverrpc;

import com.google.common.base.Function;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.mayhew3.drafttower.shared.*;

/**
 * Interface for all HTTP-based server requests.
 */
public interface ServerRpc {

  void sendLoginRequest(String username,
      String password,
      Function<LoginResponse, Void> successCallback,
      Function<SocketTerminationReason, Void> failureCallback);

  void sendGetPlayerQueueRequest(AutoBean<GetPlayerQueueRequest> requestBean,
      Function<GetPlayerQueueResponse, Void> callback);

  void sendEnqueueOrDequeueRequest(String action,
      AutoBean<EnqueueOrDequeuePlayerRequest> requestBean,
      Runnable callback);

  void sendReorderQueueRequest(AutoBean<ReorderPlayerQueueRequest> requestBean,
      Runnable callback);

  void sendGraphsRequest(AutoBean<GetGraphsDataRequest> requestBean,
      Function<GraphsData, Void> callback);

  void sendPlayerListRequest(AutoBean<UnclaimedPlayerListRequest> requestBean,
      Function<UnclaimedPlayerListResponse, Void> callback);

  void sendChangePlayerRankRequest(AutoBean<ChangePlayerRankRequest> requestBean,
      Runnable callback);

  void sendCopyRanksRequest(AutoBean<CopyAllPlayerRanksRequest> requestBean,
      Runnable callback);

  void sendSetWizardTableRequest(AutoBean<SetWizardTableRequest> requestBean,
      Runnable callback);

  void sendSetCloserLimitsRequest(AutoBean<SetCloserLimitRequest> requestBean,
      Runnable callback);

  void sendAddOrRemoveFavoriteRequest(AutoBean<AddOrRemoveFavoriteRequest> requestBean,
      Runnable callback);
}