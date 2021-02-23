package com.github.twitch4j.auth.dao;

public interface Token {
    String getAccessToken();

    String getRefreshToken();
}
