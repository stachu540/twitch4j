package com.github.twitch4j.core;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

public interface RestCommand<T> extends TwitchObject {
    RestCommand<T> doOnSuccess(Consumer<T> success);

    RestCommand<T> doOnError(Consumer<Throwable> error);

    <X extends Throwable> RestCommand<T> doOnError(Class<X> type, Consumer<X> error);

    RestCommand<T> doOnComplete(Runnable completed);

    <R> RestCommand<R> map(Function<T, R> mapper);

    <R> RestCommand<R> flatMap(Function<T, RestCommand<R>> mapper);

    T execute() throws Throwable;

    void enqueue(Consumer<T> result, Consumer<Throwable> error);

    void enqueue(Consumer<T> result);

    void enqueue();

    CompletableFuture<T> complete();
}
