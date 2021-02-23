package com.github.twitch4j.auth.dao;

import com.github.twitch4j.auth.TwitchScope;
import lombok.Value;

import java.time.Instant;
import java.util.Set;

@Value
public class ApplicationCredential {
    String accessToken;
    Instant expiredAt;
    Set<TwitchScope> scopes;
    String clientId;
}
