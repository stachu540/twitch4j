package com.github.twitch4j.auth.dao;

import lombok.Value;

@Value
public class ProvidedToken implements Token {
    String accessToken;
    String refreshToken;
}
