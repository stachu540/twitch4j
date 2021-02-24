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
package com.github.twitch4j.core.internal.event;

import com.github.twitch4j.core.event.Event;
import com.github.twitch4j.core.event.EventDisposable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SimpleEventHandlerDisposal implements EventDisposable {

  private final AtomicBoolean disposed = new AtomicBoolean(false);

  private final SimpleEventHandler eventHandler;
  private final Class<? extends Event> type;
  private final Consumer<? extends Event> consumer;

  @Override
  public void dispose() {
    if (!isDisposed()) {
      if (consumer != null) {
        eventHandler.getHandlers().get(type).remove(consumer);
      }

      disposed.set(true);
    }
  }

  @Override
  public boolean isDisposed() {
    return disposed.get();
  }
}
