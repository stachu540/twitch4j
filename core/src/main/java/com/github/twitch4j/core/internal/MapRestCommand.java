package com.github.twitch4j.core.internal;

import com.github.twitch4j.core.RestCommand;

import java.util.function.Consumer;
import java.util.function.Function;

public class MapRestCommand<T, R> extends AbstractRestCommand<R> {
    private final RestCommand<T> command;
    private final Function<T, R> mapper;

    public MapRestCommand(RestCommand<T> command, Function<T, R> mapper) {
        super(command.getCompanion());
        this.command = command;
        this.mapper = mapper;
    }

    @Override
    public <U> RestCommand<U> map(java.util.function.Function<R, U> mapper) {
        return new MapRestCommand<>(this, mapper);
    }

    @Override
    public <U> RestCommand<U> flatMap(java.util.function.Function<R, RestCommand<U>> mapper) {
        return new FlatMapRestCommand<>(this, mapper);
    }

    @Override
    protected R doExecute() throws Throwable {
        return mapper.apply(command.execute());
    }

    @Override
    protected void doEnqueue(Consumer<R> result, Consumer<Throwable> error) {
        command.enqueue(response -> result.accept(mapper.apply(response)), error);
    }
}
