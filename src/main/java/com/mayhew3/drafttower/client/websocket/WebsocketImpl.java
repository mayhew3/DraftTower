package com.mayhew3.drafttower.client.websocket;

import com.google.inject.Provider;
import com.mayhew3.drafttower.client.GinBindingAnnotations.DraftSocketUrl;
import com.mayhew3.drafttower.shared.SocketTerminationReason;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

/**
 * Adapted from com.sksamuel.gwt.websockets.
 */
public class WebsocketImpl implements Websocket {

  private static int counter = 1;

  public static native boolean isSupported() /*-{
    return ("WebSocket" in window);
  }-*/;

  private final Set<WebsocketListener> listeners = new HashSet<>();

  private final String name;
  private final Provider<String> urlProvider;

  @Inject
  public WebsocketImpl(@DraftSocketUrl Provider<String> urlProvider) {
    this.urlProvider = urlProvider;
    this.name = "dtws-" + counter++;
  }

  public native void _close(String s) /*-{
      $wnd.s.close();
  }-*/;

  private native void _open(Websocket ws, String s, String url) /*-{
      $wnd.s = new WebSocket(url);
      $wnd.s.onopen = function () {
        ws.@com.mayhew3.drafttower.client.websocket.WebsocketImpl::onOpen()();
      };
      $wnd.s.onclose = function (evt) {
        ws.@com.mayhew3.drafttower.client.websocket.WebsocketImpl::onClose(I)(evt.code);
      };
      $wnd.s.onmessage = function (msg) {
        ws.@com.mayhew3.drafttower.client.websocket.WebsocketImpl::onMessage(Ljava/lang/String;)(msg.data);
      }
  }-*/;

  public native void _send(String s, String msg) /*-{
      $wnd.s.send(msg);
  }-*/;

  private native int _state(String s) /*-{
      return $wnd.s.readyState;
  }-*/;

  @Override
  public void addListener(WebsocketListener listener) {
    listeners.add(listener);
  }

  @Override
  public void close() {
    _close(name);
  }

  @Override
  public int getState() {
    return _state(name);
  }

  protected void onClose(int closeCode) {
    for (WebsocketListener listener : listeners)
      listener.onClose(SocketTerminationReason.fromCloseCode(closeCode));
  }

  protected void onMessage(String msg) {
    for (WebsocketListener listener : listeners)
      listener.onMessage(msg);
  }

  protected void onOpen() {
    for (WebsocketListener listener : listeners)
      listener.onOpen();
  }

  @Override
  public void open() {
    _open(this, name, urlProvider.get());
  }

  @Override
  public void send(String msg) {
    _send(name, msg);
  }
}
