package com.github.twitch4j.core.http.engine;

import com.github.twitch4j.core.AbstractSocketClient;
import com.github.twitch4j.core.http.TwitchRequest;
import com.github.twitch4j.core.http.TwitchResponse;
import com.github.twitch4j.core.socket.SocketDispatcher;
import com.github.twitch4j.core.socket.TwitchSocket;

import java.io.IOException;
import java.net.URI;

public interface HttpEngine {
    TwitchResponse execute(TwitchRequest request) throws Exception;
}
