package com.github.twitch4j.core.socket;

import lombok.Value;

@Value
public class StatusClose {
    int code;
    String reason;
}
