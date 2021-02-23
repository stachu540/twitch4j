package com.github.twitch4j.core;

import com.github.twitch4j.core.event.AbstractEvent;
import lombok.Getter;

@Getter
public abstract class AbstractTwitchSocketObject<C extends AbstractSocketClient<C>> extends AbstractEvent implements TwitchSocketObject<C> {
    private final C client;

    protected AbstractTwitchSocketObject(C client) {
        super(client.getCompanion());
        this.client = client;
    }
}
