package com.github.twitch4j.core;

import com.github.twitch4j.core.socket.TwitchSocket;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public abstract class AbstractSocketClient<C extends AbstractSocketClient<C>> extends AbstractTwitchObject {
    protected final TwitchSocket socket = null;

    protected AbstractSocketClient(TwitchCompanion companion, URI url) {
        super(companion);
//        this.socket = companion.getHttpClient().newWebSocket;
    }

    @SuppressWarnings("unchecked")
    public Future<C> connect() {
        return new FutureTask<>(() -> {
            socket.open();
            return (C) AbstractSocketClient.this;
        });
    }

    public Future<Void> disconnect() {
        return new FutureTask<>(() -> {
            socket.close();
            return Void.TYPE.newInstance();
        });
    }

    public Future<Void> disconnect(int code) {
        return new FutureTask<>(() -> {
            socket.close(code);
            return Void.TYPE.newInstance();
        });
    }

    public Future<Void> disconnect(int code, @Nullable String reason) {
        return new FutureTask<>(() -> {
            socket.close(code, reason);
            return Void.TYPE.newInstance();
        });
    }

//    public <E extends SocketEvent<C>> void onEvent(Class<E> type, Consumer<E> result) {
//        getCompanion().getEventManager().onEvent(type, result);
//    }
}
