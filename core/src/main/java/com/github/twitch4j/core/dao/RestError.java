package com.github.twitch4j.core.dao;

import lombok.Value;

@Value
public class RestError {
    String error;
    int status;
    String message;
}
