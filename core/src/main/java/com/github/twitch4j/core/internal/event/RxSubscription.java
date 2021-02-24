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
import io.reactivex.rxjava3.disposables.Disposable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@RequiredArgsConstructor
public class RxSubscription<E extends Event> implements Subscriber<E>, EventDisposable, Disposable {
  private final Consumer<E> consumer;
  private final AtomicBoolean disposed = new AtomicBoolean(false);
  private Subscription subscription;

  @Override
  public void dispose() {
    if (!disposed.get()) {
      subscription.cancel();
      disposed.set(true);
      subscription = null;
    }
  }

  @Override
  public boolean isDisposed() {
    return disposed.get();
  }

  @Override
  public void onSubscribe(final Subscription subscription) {
    if (!isDisposed()) {
      this.subscription = subscription;
      subscription.request(1);
    }
  }

  @Override
  public void onNext(final E e) {
    if (!isDisposed()) {
      consumer.accept(e);
      subscription.request(1);
    }
  }

  @Override
  public void onError(final Throwable t) {
    // ignore
  }

  @Override
  public void onComplete() {
    // ignore
  }
}
