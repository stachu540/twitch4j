package com.github.twitch4j.core.socket;

import com.github.twitch4j.core.AbstractSocketClient;
import com.github.twitch4j.core.event.EventManager;
import com.github.twitch4j.core.socket.event.AbstractSocketEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Setter
@Getter
@RequiredArgsConstructor
public abstract class TwitchSocketConfig {
    private final URI url;
    private BiConsumer<EventManager, String> eventCompose = (manager, rawMessage) -> {};
}
