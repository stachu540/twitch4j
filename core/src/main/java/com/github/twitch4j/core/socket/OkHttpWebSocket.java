package com.github.twitch4j.core.socket;

import com.github.twitch4j.core.AbstractSocketClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpClient;
import java.nio.channels.AlreadyConnectedException;
import java.nio.channels.NotYetConnectedException;
import java.util.Objects;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class OkHttpWebSocket implements TwitchSocket {
    private final OkHttpClient httpClient;
    @Getter private final URI url;
    private final SocketDispatcher dispatcher;

    private WebSocket ws = null;

    @Override
    public boolean isActive() {
        return ws != null;
    }

    @Override
    public void open() throws Exception {
        if (isActive()) throw new AlreadyConnectedException();
        else {
            httpClient.newWebSocket(new Request.Builder().url(Objects.requireNonNull(HttpUrl.get(url))).build(), new WebSocketListener() {
                @Override
                public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                    dispatcher.onDisconnect(code, reason);
                    OkHttpWebSocket.this.ws = null;
                }

                @Override
                public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
                    dispatcher.onError(t);
                }

                @Override
                public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                    dispatcher.onMessage(text);
                }

                @Override
                public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                    dispatcher.onConnect();
                    OkHttpWebSocket.this.ws = webSocket;
                }
            });
        }
    }

    @Override
    public void sendRaw(String raw) throws Exception {
        if (!isActive()) throw new NotYetConnectedException();
    }

    @Override
    public void close(int code, @Nullable String reason) throws Exception {
        if (!isActive()) throw new NotYetConnectedException();
        else {
            ws.close(code, reason);
        }
    }


    public static class Factory implements TwitchSocketFactory<Config> {

        @Override
        public Config configure(URI url, Consumer<Config> configure) {
            Config config = new Config(url);
            configure.accept(config);
            return config;
        }

        @Override
        public <E extends AbstractSocketClient<E>> TwitchSocket install(E client, Config config) {
            OkHttpClient.Builder http = new OkHttpClient.Builder();
            config.client.accept(http);

            return new OkHttpWebSocket(http.build(), config.getUrl(), new BaseSocketDispatcher<>(client, config.getEventCompose()));
        }
    }

    public static class Config extends TwitchSocketConfig {
        private Consumer<OkHttpClient.Builder> client = builder -> {};

        private Config(URI url) {
            super(url);
        }

        public void configure(Consumer<OkHttpClient.Builder> builder) {
            client = client.andThen(builder);
        }
    }
}
