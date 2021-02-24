/*
 * MIT License
 *
 * Copyright (c) 2021 Philipp Heuer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.twitch4j.core.internal.socket;

import com.github.twitch4j.core.AbstractSocketClient;
import com.github.twitch4j.core.socket.SocketDispatcher;
import com.github.twitch4j.core.socket.TwitchSocket;
import com.github.twitch4j.core.socket.TwitchSocketConfig;
import com.github.twitch4j.core.socket.TwitchSocketFactory;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.channels.NotYetConnectedException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class Java11Socket implements TwitchSocket {
  private final WebSocket.Builder client;
  @Getter
  private final URI url;
  private final SocketDispatcher dispatcher;

  private WebSocket ws;

  @Override
  public boolean isActive() {
    return ws != null && !ws.isInputClosed() && !ws.isOutputClosed();
  }

  @Override
  public void open() throws Exception {
    client.buildAsync(url, new WebSocket.Listener() {
      @Override
      public void onOpen(final WebSocket webSocket) {
        Java11Socket.this.ws = webSocket;
      }

      @Override
      public CompletionStage<?> onText(final WebSocket webSocket, final CharSequence data, final boolean last) {
        webSocket.request(1);
        dispatcher.onMessage(data.toString());
        return new CompletableFuture<>();
      }

      @Override
      public CompletionStage<?> onClose(final WebSocket webSocket, final int statusCode, final String reason) {
        dispatcher.onDisconnect(statusCode, reason);
        Java11Socket.this.ws = null;
        return new CompletableFuture<>();
      }

      @Override
      public void onError(final WebSocket webSocket, final Throwable error) {
        dispatcher.onError(error);
      }
    }).thenRun(dispatcher::onConnect).get();
  }

  @Override
  public void sendRaw(final String raw) throws Exception {
    if (!isActive()) {
      throw new NotYetConnectedException();
    } else {
      ws.sendText(raw, true).get();
    }
  }

  @Override
  public void close(final int code, final @Nullable String reason) throws Exception {
    if (!isActive()) {
      throw new NotYetConnectedException();
    } else {
      ws.sendClose(code, (reason == null) ? "" : reason).get();
    }
  }

  public static final class Factory implements TwitchSocketFactory<Config> {

    @Override
    public Config configure(final URI url, final Consumer<Config> configure) {
      Config config = new Config(url);
      configure.accept(config);
      return config;
    }

    @Override
    public <E extends AbstractSocketClient<E>> TwitchSocket install(final E client, final Config config) {
      HttpClient.Builder http = HttpClient.newBuilder();
      config.client.accept(http);
      WebSocket.Builder ws = http.build().newWebSocketBuilder();
      config.ws.accept(ws);

      return new Java11Socket(ws, config.getUrl(), new BaseSocketDispatcher<>(client, config.getEventCompose()));
    }
  }

  public static final class Config extends TwitchSocketConfig {
    private Consumer<WebSocket.Builder> ws = builder -> {
    };
    private Consumer<HttpClient.Builder> client = builder -> {
    };

    private Config(final URI url) {
      super(url);
    }

    public void configureWebsocket(final Consumer<WebSocket.Builder> builder) {
      ws = ws.andThen(builder);
    }

    public void configureClient(final Consumer<HttpClient.Builder> builder) {
      client = client.andThen(builder);
    }
  }
}
