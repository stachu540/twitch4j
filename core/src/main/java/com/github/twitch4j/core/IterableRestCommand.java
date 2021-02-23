package com.github.twitch4j.core;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public interface IterableRestCommand<T> extends RestCommand<List<T>>, Iterable<T> {
    IterableRestCommand<T> doOnSuccess(Consumer<List<T>> success);

    IterableRestCommand<T> doOnEach(Consumer<T> success);

    IterableRestCommand<T> doOnError(Consumer<Throwable> error);

    <X extends Throwable> IterableRestCommand<T> doOnError(Class<X> type, Consumer<X> error);

    IterableRestCommand<T> doOnComplete(Runnable completed);

    <R> IterableRestCommand<R> mapEach(Function<T, R> mapper);

    <R> IterableRestCommand<R> flatMapEach(Function<T, Iterable<R>> mapper);
}
