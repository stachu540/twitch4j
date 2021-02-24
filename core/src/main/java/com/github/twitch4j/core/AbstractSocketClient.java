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
package com.github.twitch4j.core;

import com.github.twitch4j.core.socket.TwitchSocket;
import com.github.twitch4j.core.socket.event.SocketEvent;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public abstract class AbstractSocketClient<C extends AbstractSocketClient<C>> extends AbstractTwitchObject {
  private final TwitchSocket socket;

  protected AbstractSocketClient(final TwitchCompanion companion, final TwitchSocket socket) {
    super(companion);
    this.socket = socket;
  }

  @SuppressWarnings("unchecked")
  public final Future<C> connect() {
    return new FutureTask<>(() -> {
      socket.open();
      return (C) AbstractSocketClient.this;
    });
  }

  public final Future<Void> disconnect() {
    return new FutureTask<>(() -> {
      socket.close();
      return Void.TYPE.newInstance();
    });
  }

  public final Future<Void> disconnect(final int code) {
    return new FutureTask<>(() -> {
      socket.close(code);
      return Void.TYPE.newInstance();
    });
  }

  public final Future<Void> disconnect(final int code, @Nullable final String reason) {
    return new FutureTask<>(() -> {
      socket.close(code, reason);
      return Void.TYPE.newInstance();
    });
  }

  @SuppressWarnings("unchecked")
  protected final Future<C> sendRaw(final String raw) {
    return new FutureTask<>(() -> {
      socket.sendRaw(raw);
      return (C) AbstractSocketClient.this;
    });
  }

  public final <E extends SocketEvent<C>> void onEvent(final Class<E> type, final Consumer<E> result) {
    getCompanion().getEventManager().onEvent(type, result);
  }
}
