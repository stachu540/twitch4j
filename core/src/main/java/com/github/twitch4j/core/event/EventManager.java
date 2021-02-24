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
package com.github.twitch4j.core.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class EventManager implements AutoCloseable {
  private final List<EventHandler> handlers = new ArrayList<>();
  private final AtomicBoolean stopped = new AtomicBoolean(false);
  private final AtomicInteger sequenceConsumed = new AtomicInteger(1);
  private final Map<String, EventSubscription<?>> activeSubscriptions = Collections.synchronizedMap(new HashMap<>());
  @Setter
  private Class<? extends EventHandler> defaultEventHandler;

  public void handle(final Event event) {
    if (stopped.get()) {
      return;
    }

    handlers.forEach(handler -> handler.handle(event));
  }

  public void registerHandler(final EventHandler handler) {
    if (!handlers.contains(handler)) {
      handlers.add(handler);
    }
  }

  private synchronized <E extends Event> EventDisposable onEvent(
    final String id, final Class<E> eventType, final Consumer<E> event
  ) {
    if (activeSubscriptions.containsKey(id)) {
      return null;
    }

    Class<? extends EventHandler> eventHandler = defaultEventHandler;
    if (eventHandler == null) {
      if (handlers.size() == 1) {
        eventHandler = handlers.get(0).getClass();
      } else {
        throw new RuntimeException(
          "There is more EventHandler's have been register."
            + " Please specify defaultEventHandler using \"EventManager#setDefaultEventHandler\"!"
        );
      }
    }

    Optional<EventDisposable> disposable = getHandler(eventHandler)
      .map(handler -> handler.onEvent(eventType, event));
    if (disposable.isPresent()) {
      return new EventSubscription<>(disposable.get(), id, eventType, event, activeSubscriptions);
    } else {
      throw new RuntimeException("EventHandler \"" + eventHandler.getCanonicalName() + "\" is not register");
    }
  }

  public <E extends Event> EventDisposable onEvent(final Class<E> eventType, final Consumer<E> event) {
    return onEvent(
      event.getClass().getCanonicalName() + "/" + sequenceConsumed.getAndIncrement(), eventType, event
    );
  }

  public boolean isHandlerExist(final Class<? extends EventHandler> handlerType) {
    return handlers.stream().anyMatch(handlerType::isInstance);
  }

  public <H extends EventHandler> Optional<H> getHandler(final Class<H> handlerType) {
    return handlers.stream().filter(handlerType::isInstance).findFirst().map(handlerType::cast);
  }

  public Collection<EventSubscription<? extends Event>> getActiveSubscriptions() {
    return Collections.unmodifiableCollection(activeSubscriptions.values());
  }

  @Override
  public void close() throws Exception {
    stopped.set(true);

    getActiveSubscriptions().forEach(EventDisposable::dispose);

    for (EventHandler handler : handlers) {
      handler.close();
    }
  }
}
