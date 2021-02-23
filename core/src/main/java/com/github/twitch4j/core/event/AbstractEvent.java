package com.github.twitch4j.core.event;

import com.github.twitch4j.core.AbstractTwitchObject;
import com.github.twitch4j.core.TwitchCompanion;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public abstract class AbstractEvent extends AbstractTwitchObject implements Event {
    private final UUID id;
    private final Instant firedAt;

    protected AbstractEvent(TwitchCompanion companion) {
        this(companion, UUID.randomUUID(), Instant.now());
    }

    protected AbstractEvent(TwitchCompanion companion, UUID id, Instant firedAt) {
        super(companion);
        this.id = id;
        this.firedAt = firedAt;
    }
}
