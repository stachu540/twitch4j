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
import com.github.twitch4j.core.AbstractTwitchSocketObject;
import com.github.twitch4j.core.event.EventManager;
import com.github.twitch4j.core.socket.SocketDispatcher;
import com.github.twitch4j.core.socket.StatusClose;
import com.github.twitch4j.core.socket.event.ConnectionStartEvent;
import com.github.twitch4j.core.socket.event.ConnectionStoppedEvent;
import com.github.twitch4j.core.socket.event.RawMessageEvent;
import java.util.function.BiConsumer;
import org.jetbrains.annotations.Nullable;

public final class BaseSocketDispatcher<C extends AbstractSocketClient<C>>
  extends AbstractTwitchSocketObject<C> implements SocketDispatcher {

  private final BiConsumer<EventManager, String> messageEventHandler;

  protected BaseSocketDispatcher(final C client, final BiConsumer<EventManager, String> messageEventHandler) {
    super(client);
    this.messageEventHandler = messageEventHandler;
  }

  @Override
  public void onConnect() {
    getCompanion().getEventManager().handle(new ConnectionStartEvent<>(getClient()));
  }

  @Override
  public void onMessage(final String raw) {
    getCompanion().getEventManager().handle(new RawMessageEvent<>(getClient(), raw));
    messageEventHandler.accept(getCompanion().getEventManager(), raw);
  }

  @Override
  public void onDisconnect(final int code, final @Nullable String reason) {
    getCompanion().getEventManager()
      .handle(new ConnectionStoppedEvent<>(getClient(), new StatusClose(code, reason)));
  }

  @Override
  public void onError(final Throwable error) {
    // skip
  }
}
