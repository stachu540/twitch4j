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
import com.github.twitch4j.core.event.EventHandler;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import lombok.AccessLevel;
import lombok.Getter;

public class SimpleEventHandler implements EventHandler {
  @Getter(AccessLevel.PACKAGE)
  private final Map<Class<? extends Event>, Collection<Consumer<Event>>> handlers = new LinkedHashMap<>();

  @Override
  public void handle(final Event event) {
    final Collection<Consumer<Event>> consumers = handlers.get(event.getClass());
    if (consumers != null) {
      consumers.forEach(c -> c.accept(event));
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <E extends Event> EventDisposable onEvent(final Class<E> type, final Consumer<E> event) {
    handlers.computeIfAbsent(type, s -> new CopyOnWriteArrayList<>())
      .add((Consumer<Event>) event);

    return new SimpleEventHandlerDisposal(this, type, event);
  }

  @Override
  public void close() throws Exception {
    // ignore
  }
}
