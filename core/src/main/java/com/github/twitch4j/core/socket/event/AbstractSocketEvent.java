package com.github.twitch4j.core.socket.event;

import com.github.twitch4j.core.AbstractSocketClient;
import com.github.twitch4j.core.AbstractTwitchSocketObject;

public abstract class AbstractSocketEvent<C extends AbstractSocketClient<C>> extends AbstractTwitchSocketObject<C> implements SocketEvent<C> {
    protected AbstractSocketEvent(C client) {
        super(client.getCompanion(), client);
    }
}
