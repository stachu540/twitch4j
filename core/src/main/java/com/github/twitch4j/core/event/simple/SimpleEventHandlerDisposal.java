package com.github.twitch4j.core.event.simple;

import com.github.twitch4j.core.event.Event;
import com.github.twitch4j.core.event.EventDisposable;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class SimpleEventHandlerDisposal implements EventDisposable {

    private final AtomicBoolean disposed = new AtomicBoolean(false);

    private final SimpleEventHandler eventHandler;
    private final Class<? extends Event> type;
    private final Consumer<? extends Event> consumer;

    @Override
    public void dispose() {
        if (!isDisposed()) {
            if (consumer != null) {
                eventHandler.getHandlers().get(type).remove(consumer);
            }

            disposed.set(true);
        }
    }

    @Override
    public boolean isDisposed() {
        return disposed.get();
    }
}
