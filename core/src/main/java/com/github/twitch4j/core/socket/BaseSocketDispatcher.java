package com.github.twitch4j.core.socket;

import com.github.twitch4j.core.AbstractSocketClient;
import com.github.twitch4j.core.AbstractTwitchSocketObject;
import com.github.twitch4j.core.TwitchCompanion;
import com.github.twitch4j.core.event.EventManager;
import com.github.twitch4j.core.socket.event.ConnectionStartEvent;
import com.github.twitch4j.core.socket.event.ConnectionStoppedEvent;
import com.github.twitch4j.core.socket.event.RawMessageEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public final class BaseSocketDispatcher<C extends AbstractSocketClient<C>> extends AbstractTwitchSocketObject<C> implements SocketDispatcher {

    private final BiConsumer<EventManager, String> messageEventHandler;

    protected BaseSocketDispatcher(C client, BiConsumer<EventManager, String> messageEventHandler) {
        super(client);
        this.messageEventHandler = messageEventHandler;
    }

    @Override
    public void onConnect() {
        getCompanion().getEventManager().handle(new ConnectionStartEvent<>(getClient()));
    }

    @Override
    public void onMessage(String raw) {
        getCompanion().getEventManager().handle(new RawMessageEvent<>(getClient(), raw));
        messageEventHandler.accept(getCompanion().getEventManager(), raw);
    }

    @Override
    public void onDisconnect(int code, @Nullable String reason) {
        getCompanion().getEventManager().handle(new ConnectionStoppedEvent<>(getClient(), new StatusClose(code, reason)));
    }

    @Override
    public void onError(Throwable error) {

    }
}
