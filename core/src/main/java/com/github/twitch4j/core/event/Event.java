package com.github.twitch4j.core.event;

import com.github.twitch4j.core.TwitchObject;

import java.time.Instant;
import java.util.UUID;

public interface Event extends TwitchObject {
    UUID getId();

    Instant getFiredAt();
}
