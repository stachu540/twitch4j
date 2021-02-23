package com.github.twitch4j.core.socket;

import org.jetbrains.annotations.Nullable;

public interface SocketDispatcher {
    void onConnect();

    void onMessage(String raw);

    void onDisconnect(int code, @Nullable String reason);

    void onError(Throwable error);
}
