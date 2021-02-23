package com.github.twitch4j.core.socket.event;

import com.github.twitch4j.core.AbstractSocketClient;

public final class ConnectionStartEvent<C extends AbstractSocketClient<C>> extends AbstractSocketEvent<C> {
    public ConnectionStartEvent(C client) {
        super(client);
    }
}
