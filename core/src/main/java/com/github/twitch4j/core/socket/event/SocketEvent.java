package com.github.twitch4j.core.socket.event;

import com.github.twitch4j.core.AbstractSocketClient;
import com.github.twitch4j.core.TwitchSocketObject;
import com.github.twitch4j.core.event.Event;

public interface SocketEvent<C extends AbstractSocketClient<C>> extends Event, TwitchSocketObject<C> {

}
