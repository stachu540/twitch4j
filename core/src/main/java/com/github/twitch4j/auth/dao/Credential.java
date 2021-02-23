package com.github.twitch4j.auth.dao;

import com.github.twitch4j.auth.TwitchScope;
import lombok.Value;

import java.time.Instant;
import java.util.Set;

@Value
public class Credential {
    String accessToken;
    String refreshToken;
    Instant expiredAt;
    Set<TwitchScope> scopes;
    String clientId;
    String login;
    int id;
}
