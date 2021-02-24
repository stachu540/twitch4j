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
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.NotYetConnectedException;
import java.util.Objects;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
public class OkHttpWebSocket implements TwitchSocket {
  private final OkHttpClient httpClient;
  @Getter
  private final URI url;
  private final SocketDispatcher dispatcher;

  private WebSocket ws = null;

  @Override
  public final boolean isActive() {
    return ws != null;
  }

  @Override
  public final void open() throws Exception {
    if (isActive()) {
      throw new AlreadyConnectedException();
    } else {
      Request request = new Request.Builder().url(Objects.requireNonNull(HttpUrl.get(url))).build();
      httpClient.newWebSocket(request, new WebSocketListener() {
        @Override
        public void onClosed(
          final @NotNull WebSocket webSocket, final int code, final @NotNull String reason
        ) {
          dispatcher.onDisconnect(code, reason);
          OkHttpWebSocket.this.ws = null;
        }

        @Override
        public void onFailure(
          final @NotNull WebSocket webSocket, final @NotNull Throwable t, final @Nullable Response response
        ) {
          dispatcher.onError(t);
        }

        @Override
        public void onMessage(final @NotNull WebSocket webSocket, final @NotNull String text) {
          dispatcher.onMessage(text);
        }

        @Override
        public void onOpen(final @NotNull WebSocket webSocket, final @NotNull Response response) {
          dispatcher.onConnect();
          OkHttpWebSocket.this.ws = webSocket;
        }
      });
    }
  }

  @Override
  public final void sendRaw(final String raw) throws Exception {
    if (!isActive()) {
      throw new NotYetConnectedException();
    } else {
      ws.send(raw);
    }
  }

  @Override
  public final void close(final int code, final @Nullable String reason) throws Exception {
    if (!isActive()) {
      throw new NotYetConnectedException();
    } else {
      ws.close(code, reason);
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
      OkHttpClient.Builder http = new OkHttpClient.Builder();
      config.client.accept(http);

      return new OkHttpWebSocket(
        http.build(), config.getUrl(),
        new BaseSocketDispatcher<>(client, config.getEventCompose())
      );
    }
  }

  public static final class Config extends TwitchSocketConfig {
    private Consumer<OkHttpClient.Builder> client = builder -> {
    };

    private Config(final URI url) {
      super(url);
    }

    public void configure(final Consumer<OkHttpClient.Builder> builder) {
      client = client.andThen(builder);
    }
  }
}
