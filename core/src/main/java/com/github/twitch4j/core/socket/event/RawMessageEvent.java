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
package com.github.twitch4j.core.socket.event;

import com.github.twitch4j.core.AbstractSocketClient;
import java.io.IOException;
import lombok.Getter;

@Getter
public final class RawMessageEvent<C extends AbstractSocketClient<C>> extends AbstractSocketEvent<C> {
  private final String messageRaw;

  public RawMessageEvent(final C client, final String messageRaw) {
    super(client);
    this.messageRaw = messageRaw;
  }

  /**
   * Map received JSON message to requested type.
   *
   * @param type type of message
   * @param <T>  received message type
   * @return message parsed to requested type
   * @throws IOException           could not parse message
   * @throws IllegalStateException the message is not a JSON
   */
  public <T> T mapMessageAs(final Class<T> type) throws IOException {
    if (messageRaw.matches("^(\\{(.+)}|\\[(.+)])$")) {
      return getClient().getCompanion().getHttpClient().getMapper().readValue(messageRaw, type);
    } else {
      throw new IllegalStateException("Cannot process message when it is not a \"JSON\"!");
    }
  }
}
