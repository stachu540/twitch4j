package com.github.twitch4j.core.event;

public interface EventDisposable {
    void dispose();

    boolean isDisposed();
}
