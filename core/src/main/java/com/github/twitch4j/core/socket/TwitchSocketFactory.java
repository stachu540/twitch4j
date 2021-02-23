package com.github.twitch4j.core.socket;

import com.github.twitch4j.core.AbstractSocketClient;

import java.net.URI;
import java.util.function.Consumer;

public interface TwitchSocketFactory<C extends TwitchSocketConfig> {
    C configure(URI url, Consumer<C> configure);

    <E extends AbstractSocketClient<E>> TwitchSocket install(E client, C config);
}
