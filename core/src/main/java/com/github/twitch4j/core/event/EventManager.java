package com.github.twitch4j.core.event;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Slf4j
public final class EventManager implements AutoCloseable {
    private final List<EventHandler> handlers = new ArrayList<>();
    private final AtomicBoolean stopped = new AtomicBoolean(false);
    private final AtomicInteger sequenceConsumed = new AtomicInteger(1);
    private final Map<String, EventSubscription<?>> activeSubscriptions = Collections.synchronizedMap(new HashMap<>());
    @Setter
    private Class<EventHandler> defaultEventHandler;

    public final void handle(Event event) {
        if (stopped.get()) {
            return;
        }

        handlers.forEach(handler -> handler.handle(event));
    }

    public final void registerHandler(EventHandler handler) {
        if (!handlers.contains(handler)) {
            handlers.add(handler);
        }
    }

    @SuppressWarnings("unchecked")
    private synchronized <E extends Event> EventDisposable onEvent(String id, Class<E> eventType, Consumer<E> event) {
        if (activeSubscriptions.containsKey(id)) {
            return null;
        }

        Class<EventHandler> eventHandler = defaultEventHandler;
        if (eventHandler == null) {
            if (handlers.size() == 1) {
                eventHandler = (Class<EventHandler>) handlers.get(0).getClass();
            } else {
                throw new RuntimeException("There is more EventHandler's have been register. Please specify defaultEventHandler using \"EventManager#setDefaultEventHandler\"!");
            }
        }

        Optional<EventDisposable> disposable = getHandler(eventHandler).map(handler -> handler.onEvent(eventType, event));
        if (disposable.isPresent()) {
            return new EventSubscription<>(disposable.get(), id, eventType, event, activeSubscriptions);
        } else {
            throw new RuntimeException("EventHandler \"" + eventHandler.getCanonicalName() + "\" is not register");
        }
    }

    public final <E extends Event> EventDisposable onEvent(Class<E> eventType, Consumer<E> event) {
        return onEvent(event.getClass().getCanonicalName() + "/" + sequenceConsumed.getAndIncrement(), eventType, event);
    }

    public final boolean isHandlerExist(Class<? extends EventHandler> handlerType) {
        return handlers.stream().anyMatch(handlerType::isInstance);
    }

    public final <H extends EventHandler> Optional<H> getHandler(Class<H> handlerType) {
        return handlers.stream().filter(handlerType::isInstance).findFirst().map(handlerType::cast);
    }

    public final Collection<EventSubscription<?>> getActiveSubscription() {
        return Collections.unmodifiableCollection(activeSubscriptions.values());
    }

    @Override
    public void close() throws Exception {
        stopped.set(true);

        getActiveSubscription().forEach(EventDisposable::dispose);

        for (EventHandler handler : handlers) {
            handler.close();
        }
    }
}
