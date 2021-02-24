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

import com.github.twitch4j.core.dao.PaginateCollection;
import java.util.function.Consumer;
import java.util.function.Function;

public interface PaginatedRestCommand<T> extends RestCommand<PaginateCollection<T>> {
  int getLimit();

  void setLimit(int limit);

  PaginatedRestCommand<T> doOnSuccess(Consumer<PaginateCollection<T>> success);

  PaginatedRestCommand<T> doOnEach(Consumer<T> success);

  PaginatedRestCommand<T> doOnError(Consumer<Throwable> error);

  <X extends Throwable> PaginatedRestCommand<T> doOnError(Class<X> type, Consumer<X> error);

  PaginatedRestCommand<T> doOnComplete(Runnable completed);

  <R> PaginatedRestCommand<R> mapEach(Function<T, R> mapper);

  <R> PaginatedRestCommand<R> flatMapEach(Function<T, Iterable<R>> mapper);
}
