package com.github.twitch4j.core;

import com.github.twitch4j.core.dao.Paginate;

import java.util.function.Consumer;
import java.util.function.Function;

public interface PaginatedRestCommand<T> extends RestCommand<Paginate<T>> {
    int getLimit();

    void setLimit(int limit);

    PaginatedRestCommand<T> doOnSuccess(Consumer<Paginate<T>> success);

    PaginatedRestCommand<T> doOnEach(Consumer<T> success);

    PaginatedRestCommand<T> doOnError(Consumer<Throwable> error);

    <X extends Throwable> PaginatedRestCommand<T> doOnError(Class<X> type, Consumer<X> error);

    PaginatedRestCommand<T> doOnComplete(Runnable completed);

    <R> PaginatedRestCommand<R> mapEach(Function<T, R> mapper);

    <R> PaginatedRestCommand<R> flatMapEach(Function<T, Iterable<R>> mapper);
}
