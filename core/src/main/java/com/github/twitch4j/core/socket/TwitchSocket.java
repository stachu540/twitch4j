package com.github.twitch4j.core.socket;

import org.jetbrains.annotations.Nullable;

import java.net.URI;

public interface TwitchSocket {
    URI getUrl();

    boolean isActive();

    void open() throws Exception;

    void sendRaw(String raw) throws Exception;

    default void close() throws Exception {
        close(1000);
    }

    default void close(int code) throws Exception {
        close(code, null);
    }

    void close(int code, @Nullable String reason) throws Exception;
}
