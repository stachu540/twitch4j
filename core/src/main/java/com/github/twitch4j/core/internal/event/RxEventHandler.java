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
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;
import java.util.function.Consumer;

public final class RxEventHandler implements EventHandler {

  private final PublishSubject<Event> publishSubject = PublishSubject.create();
  private final Scheduler scheduler;
  private final BackpressureStrategy backpressureStrategy;
  private final int bufferSize;

  public RxEventHandler() {
    scheduler = Schedulers.newThread();
    backpressureStrategy = BackpressureStrategy.BUFFER;
    bufferSize = 8192;
  }

  public RxEventHandler(
    final Scheduler scheduler, final BackpressureStrategy backpressureStrategy, final int bufferSize
  ) {
    this.scheduler = scheduler;
    this.backpressureStrategy = backpressureStrategy;
    this.bufferSize = bufferSize;
  }

  @Override
  public void handle(final Event event) {
    publishSubject.onNext(event);
  }

  @Override
  public <E extends Event> EventDisposable onEvent(final Class<E> type, final Consumer<E> event) {
    Flowable<E> observable = publishSubject
      .observeOn(scheduler, false, bufferSize)
      .toFlowable(backpressureStrategy)
      .ofType(type);

    RxSubscription<E> subscription = new RxSubscription<>(event);

    observable.subscribe(subscription);

    return subscription;
  }

  @Override
  public void close() throws Exception {
    if (!publishSubject.hasComplete()) {
      publishSubject.onComplete();
      scheduler.shutdown();
    }
  }
}
