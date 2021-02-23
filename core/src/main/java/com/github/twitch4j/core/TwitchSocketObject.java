package com.github.twitch4j.core;

public interface TwitchSocketObject<C extends AbstractSocketClient<C>> extends TwitchObject {
    C getClient();
}
