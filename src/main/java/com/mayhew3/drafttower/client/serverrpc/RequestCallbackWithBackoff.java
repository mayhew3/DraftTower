package com.mayhew3.drafttower.client.serverrpc;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;

/**
 * {@link RequestCallback} with exponential backoff retry.
 */
abstract class RequestCallbackWithBackoff implements RequestCallback {

  private static final int INITIAL_BACKOFF_MS = 5;
  private static final int MAX_BACKOFF_MS = 5000;

  private final SchedulerWrapper scheduler;
  private RequestBuilder requestBuilder;
  private String requestData;
  private int backoff = INITIAL_BACKOFF_MS;
  private Throwable lastException;

  protected RequestCallbackWithBackoff(SchedulerWrapper scheduler) {
    this.scheduler = scheduler;
  }

  public void sendRequest(final RequestBuilder builder,
      final String requestData) {
    this.requestBuilder = builder;
    this.requestData = requestData;
    if (backoff <= MAX_BACKOFF_MS) {
      try {
        builder.sendRequest(requestData, this);
      } catch (RequestException e) {
        lastException = e;
        retry();
      }
    } else {
      throw new RuntimeException("Sending request failed after maximum retries.", lastException);
    }
  }

  @Override
  final public void onError(Request request, Throwable exception) {
    lastException = exception;
    retry();
  }

  private void retry() {
    int scheduleDelay = backoff;
    backoff *= 2;
    scheduler.schedule(new Runnable() {
      @Override
      public void run() {
        sendRequest(requestBuilder, requestData);
      }
    }, scheduleDelay);
  }
}