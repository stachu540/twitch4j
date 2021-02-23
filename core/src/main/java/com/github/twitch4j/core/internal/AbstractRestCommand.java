package com.github.twitch4j.core.internal;

import com.github.twitch4j.core.AbstractTwitchObject;
import com.github.twitch4j.core.RestCommand;
import com.github.twitch4j.core.TwitchCompanion;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class AbstractRestCommand<T> extends AbstractTwitchObject implements RestCommand<T> {
    private final List<Consumer<T>> success = new ArrayList<>();
    private final List<Consumer<Throwable>> error = new ArrayList<>();
    private final List<Runnable> completed = new ArrayList<>();

    protected AbstractRestCommand(TwitchCompanion companion) {
        super(companion);
    }

    @Override
    public RestCommand<T> doOnSuccess(Consumer<T> success) {
        this.success.add(success);
        return this;
    }

    @Override
    public RestCommand<T> doOnError(Consumer<Throwable> error) {
        this.error.add(error);
        return this;
    }

    @Override
    public <X extends Throwable> RestCommand<T> doOnError(Class<X> type, Consumer<X> error) {
        return doOnError(err -> {
            if (type.isInstance(err)) {
                error.accept(type.cast(err));
            }
        });
    }

    @Override
    public RestCommand<T> doOnComplete(Runnable completed) {
        this.completed.add(completed);
        return this;
    }

    @Override
    public final T execute() throws Throwable {
        try {
            T result = doExecute();
            success.forEach(r -> r.accept(result));
            return result;
        } catch (Throwable ex) {
            error.forEach(e -> e.accept(ex));
            throw ex;
        } finally {
            completed.forEach(Runnable::run);
        }
    }

    @Override
    public void enqueue(Consumer<T> result, Consumer<Throwable> error) {
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
    public final void enqueue(Consumer<T> result) {
        enqueue(result, error -> {
        });
    }

    @Override
    public final void enqueue() {
        enqueue(result -> {
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

    protected abstract T doExecute() throws Throwable;

    protected abstract void doEnqueue(Consumer<T> result, Consumer<Throwable> error);
}
