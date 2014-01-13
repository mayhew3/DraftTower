package com.mayhew3.drafttower.client;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;

/**
 * {@link RequestCallback} with exponential backoff retry.
 */
public abstract class RequestCallbackWithBackoff implements RequestCallback {

  private static final int INITIAL_BACKOFF_MS = 5;
  private static final int MAX_BACKOFF_MS = 5000;

  private RequestBuilder requestBuilder;
  private String requestData;
  private int backoff = INITIAL_BACKOFF_MS;

  public static void sendRequest(RequestBuilder builder,
      String requestData,
      RequestCallbackWithBackoff callback) {
    callback.requestBuilder = builder;
    callback.requestData = requestData;
    int sendRequestBackoff = INITIAL_BACKOFF_MS;
    RequestException lastException = null;
    while (sendRequestBackoff <= MAX_BACKOFF_MS) {
      try {
        builder.sendRequest(requestData, callback);
        return;
      } catch (RequestException e) {
        lastException = e;
        sendRequestBackoff *= 2;
      }
    }
    throw new RuntimeException("Sending request failed after maximum retries.", lastException);
  }

  @Override
  final public void onError(Request request, Throwable exception) {
    if (requestBuilder != null && requestData != null) {
      sendRequest(requestBuilder, requestData, this);
      backoff = Math.max(backoff * 2, MAX_BACKOFF_MS);
    }
  }
}