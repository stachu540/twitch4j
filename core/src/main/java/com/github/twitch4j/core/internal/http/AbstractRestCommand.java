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
package com.github.twitch4j.core.internal.http;

import com.github.twitch4j.core.AbstractTwitchObject;
import com.github.twitch4j.core.RestCommand;
import com.github.twitch4j.core.TwitchCompanion;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AbstractRestCommand<T> extends AbstractTwitchObject implements RestCommand<T> {
  private final List<Consumer<T>> success = new ArrayList<>();
  private final List<Consumer<Throwable>> error = new ArrayList<>();
  private final List<Runnable> completed = new ArrayList<>();

  protected AbstractRestCommand(final TwitchCompanion companion) {
    super(companion);
  }

  @Override
  public final <R> RestCommand<R> map(final Function<T, R> mapper) {
    return new MapRestCommand<>(this, mapper);
  }

  @Override
  public final <R> RestCommand<R> flatMap(final Function<T, RestCommand<R>> mapper) {
    return new FlatMapRestCommand<>(this, mapper);
  }

  @Override
  public final RestCommand<T> doOnSuccess(final Consumer<T> success) {
    this.success.add(success);
    return this;
  }

  @Override
  public final RestCommand<T> doOnError(final Consumer<Throwable> error) {
    this.error.add(error);
    return this;
  }

  @Override
  public final <X extends Throwable> RestCommand<T> doOnError(final Class<X> type, final Consumer<X> error) {
    return doOnError(err -> {
      if (type.isInstance(err)) {
        error.accept(type.cast(err));
      }
    });
  }

  @Override
  public final RestCommand<T> doOnComplete(final Runnable completed) {
    this.completed.add(completed);
    return this;
  }

  @Override
  public final T execute() throws Exception {
    try {
      T result = doExecute();
      success.forEach(r -> r.accept(result));
      return result;
    } catch (Exception ex) {
      error.forEach(e -> e.accept(ex));
      throw ex;
    } finally {
      completed.forEach(Runnable::run);
    }
  }

  @Override
  public final void enqueue(final Consumer<T> result, final Consumer<Exception> error) {
    try {
      doEnqueue(response -> {
        this.success.forEach(r -> r.accept(response));
        result.accept(response);
      }, err -> {
        this.error.forEach(e -> e.accept(err));
        error.accept(err);
      });
    } finally {
      completed.forEach(Runnable::run);
    }
  }

  @Override
  public final void enqueue(final Consumer<T> result) {
    enqueue(result, ignore -> {
    });
  }

  @Override
  public final void enqueue() {
    enqueue(ignore -> {
    });
  }

  @Override
  public final CompletableFuture<T> complete() {
    CompletableFuture<T> future = new CompletableFuture<>();

    try {
      doEnqueue(response -> {
        success.forEach(r -> r.accept(response));
        future.complete(response);
      }, err -> {
        error.forEach(e -> e.accept(err));
        future.completeExceptionally(err);
      });
    } finally {
      future.whenComplete((response, err) -> completed.forEach(Runnable::run));
    }

    return future;
  }

  protected abstract T doExecute() throws Exception;

  protected abstract void doEnqueue(Consumer<T> result, Consumer<Exception> error);
}
