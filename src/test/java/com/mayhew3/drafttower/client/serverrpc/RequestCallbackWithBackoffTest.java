package com.mayhew3.drafttower.client.serverrpc;

import com.google.common.collect.Lists;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

/**
 * Test for {@link RequestCallbackWithBackoff}.
 */
public class RequestCallbackWithBackoffTest {

  private RequestCallbackWithBackoff callback;
  private boolean callbackCalled;
  private RequestBuilder requestBuilder;
  private List<Integer> scheduleDelays;
  private RequestException expectedException;

  @Before
  public void setUp() throws Exception {
    scheduleDelays = new ArrayList<>();
    SchedulerWrapper scheduler = Mockito.mock(SchedulerWrapper.class);
    final ArgumentCaptor<Runnable> runnableArgumentCaptor = ArgumentCaptor.forClass(Runnable.class);
    final ArgumentCaptor<Integer> delayArgumentCaptor = ArgumentCaptor.forClass(Integer.class);
    Mockito.doAnswer(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        scheduleDelays.add(delayArgumentCaptor.getValue());
        runnableArgumentCaptor.getValue().run();
        return null;
      }
    }).when(scheduler).schedule(runnableArgumentCaptor.capture(), delayArgumentCaptor.capture());
    requestBuilder = Mockito.mock(RequestBuilder.class);
    callback = new RequestCallbackWithBackoff(scheduler) {
      @Override
      public void onResponseReceived(Request request, Response response) {
        callbackCalled = true;
      }
    };
  }

  @Test
  public void testSuccessfulRequest() throws RequestException {
    Mockito.when(requestBuilder.sendRequest("", callback)).then(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        callback.onResponseReceived(null, null);
        return null;
      }
    });
    callback.sendRequest(requestBuilder, "");
    Assert.assertTrue(callbackCalled);
    Assert.assertEquals(0, scheduleDelays.size());
  }

  @Test
  public void testRequestTimesOutWithRequestExceptions() throws RequestException {
    Mockito.when(requestBuilder.sendRequest("", callback)).then(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        expectedException = new RequestException();
        throw expectedException;
      }
    });
    try {
      callback.sendRequest(requestBuilder, "");
      Assert.fail("Expected exception");
    } catch (RuntimeException e) {
      Assert.assertEquals(expectedException, e.getCause());
    }
    Assert.assertFalse(callbackCalled);
    Assert.assertEquals(Lists.newArrayList(5, 10, 20, 40, 80, 160, 320, 640, 1280, 2560), scheduleDelays);
  }

  @Test
  public void testRequestTimesOutWithErrors() throws RequestException {
    Mockito.when(requestBuilder.sendRequest("", callback)).then(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        expectedException = new RequestException();
        callback.onError(null, expectedException);
        return null;
      }
    });
    try {
      callback.sendRequest(requestBuilder, "");
      Assert.fail("Expected exception");
    } catch (RuntimeException e) {
      Assert.assertEquals(expectedException, e.getCause());
    }
    Assert.assertFalse(callbackCalled);
    Assert.assertEquals(Lists.newArrayList(5, 10, 20, 40, 80, 160, 320, 640, 1280, 2560), scheduleDelays);
  }

  @Test
  public void testRequestErrorsFiveTimes() throws RequestException {
    Mockito.when(requestBuilder.sendRequest("", callback)).then(new Answer<Void>() {
      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        if (scheduleDelays.size() < 5) {
          expectedException = new RequestException();
          callback.onError(null, expectedException);
        } else {
          callback.onResponseReceived(null, null);
        }
        return null;
      }
    });
    callback.sendRequest(requestBuilder, "");
    Assert.assertTrue(callbackCalled);
    Assert.assertEquals(Lists.newArrayList(5, 10, 20, 40, 80), scheduleDelays);
  }
}