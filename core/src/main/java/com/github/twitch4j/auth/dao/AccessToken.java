package com.github.twitch4j.auth.dao;

import com.github.twitch4j.auth.TwitchScope;
import lombok.Value;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@Value
public class AccessToken implements Token {
    String accessToken;
    String refreshToken;
    int expiresIn;
    Set<TwitchScope> scope;
    String tokenType;
    @Nullable String idToken;

    public final Duration getExpiresInDuration() {
        return Duration.ofSeconds(expiresIn);
    }

    public final Instant getExpiredAt() {
        return Instant.now().plus(getExpiresInDuration());
    }
}
