package com.github.twitch4j.core.socket.event;


import com.github.twitch4j.core.AbstractSocketClient;
import com.github.twitch4j.core.socket.StatusClose;
import lombok.Getter;

@Getter
public final class ConnectionStoppedEvent<C extends AbstractSocketClient<C>> extends AbstractSocketEvent<C> {
    private final StatusClose status;

    public ConnectionStoppedEvent(C client, StatusClose status) {
        super(client);
        this.status = status;
    }
}
