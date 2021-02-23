package com.github.twitch4j.core.event.simple;

import com.github.twitch4j.core.event.Event;
import com.github.twitch4j.core.event.EventDisposable;
import com.github.twitch4j.core.event.EventHandler;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class SimpleEventHandler implements EventHandler {
    @Getter(AccessLevel.PACKAGE)
    private final ListValuedMap<Class<? extends Event>, Consumer<? extends Event>> handlers = new ArrayListValuedHashMap<>();

    @Override
    public void handle(Event event) {

    }

    @Override
    public <E extends Event> EventDisposable onEvent(Class<E> type, Consumer<E> event) {
        final List<Consumer<? extends Event>> eventConsumers = new CopyOnWriteArrayList<>(handlers.asMap().computeIfAbsent(type, s -> new CopyOnWriteArrayList<>()));
        eventConsumers.add(event);

        return new SimpleEventHandlerDisposal(this, type, event);
    }

    @Override
    public void close() throws Exception {

    }
}
