package com.github.twitch4j.core.event;

import java.util.function.Consumer;

public interface EventHandler extends AutoCloseable {
    void handle(Event event);

    <E extends Event> EventDisposable onEvent(Class<E> type, Consumer<E> event);
}
