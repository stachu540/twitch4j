package com.github.twitch4j.core.event;

import lombok.AccessLevel;
import lombok.Getter;

import java.util.Map;
import java.util.function.Consumer;

@Getter
public final class EventSubscription<E extends Event> implements EventDisposable {
    private final String id;
    private final Class<E> eventType;
    private final Consumer<E> consumer;
    @Getter(AccessLevel.NONE)
    private final EventDisposable disposable;

    private final Map<String, EventSubscription<?>> activeSubscriptions;

    public EventSubscription(EventDisposable disposable, String id, Class<E> eventType, Consumer<E> consumer, Map<String, EventSubscription<?>> activeSubscriptions) {
        this.disposable = disposable;
        this.id = id;
        this.eventType = eventType;
        this.consumer = consumer;
        this.activeSubscriptions = activeSubscriptions;

        activeSubscriptions.put(id, this);
    }

    @Override
    public void dispose() {
        if (!disposable.isDisposed()) {
            disposable.dispose();
            activeSubscriptions.remove(id);
        }
    }

    @Override
    public boolean isDisposed() {
        return disposable.isDisposed();
    }
}
