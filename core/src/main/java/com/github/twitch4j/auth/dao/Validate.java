package com.github.twitch4j.auth.dao;

import com.github.twitch4j.auth.TwitchScope;
import lombok.Value;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@Value
public class Validate {
    String clientId;
    String login;
    Set<TwitchScope> scopes;
    String userId;
    int expiresIn;

    public final Duration getExpiresInDuration() {
        return Duration.ofSeconds(expiresIn);
    }

    public final Instant getExpiredAt() {
        return Instant.now().plus(getExpiresInDuration());
    }

    public final Number getUserIdNumeric() {
        return Integer.parseInt(userId);
    }
}
