package com.github.twitch4j.core;

public class TwitchException extends RuntimeException {

    public TwitchException() {
        super();
    }

    public TwitchException(String message) {
        super(message);
    }

    public TwitchException(String message, Throwable cause) {
        super(message, cause);
    }

    public TwitchException(Throwable cause) {
        super(cause);
    }
}
