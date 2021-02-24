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
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.scheduler.forkjoin.ForkJoinPoolScheduler;

@RequiredArgsConstructor
public class ReactorEventHandler implements EventHandler {

  private final Sinks.Many<Event> sinks;
  private final Scheduler scheduler;

  public ReactorEventHandler() {
    sinks = Sinks.many().multicast().onBackpressureBuffer(8192, true);
    scheduler = ForkJoinPoolScheduler.create("twitch4j-events",
      Math.max(Runtime.getRuntime().availableProcessors(), 4)
    );
  }

  @Override
  public final void handle(final Event event) {
    sinks.emitNext(event, Sinks.EmitFailureHandler.FAIL_FAST);
  }

  @Override
  public final <E extends Event> EventDisposable onEvent(final Class<E> type, final Consumer<E> event) {
    Flux<E> flux = sinks.asFlux()
      .publishOn(scheduler)
      .ofType(type)
      .limitRequest(15);

    ReactorSubscription<E> subscription = new ReactorSubscription<>(event);
    flux.subscribe(subscription);

    return subscription;
  }

  @Override
  public final void close() throws Exception {
    // complete sink
    sinks.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);

    // delayed
    Thread.sleep(1000);

    // dispose scheduler
    if (!scheduler.isDisposed()) {
      scheduler.dispose();
    }
  }
}
