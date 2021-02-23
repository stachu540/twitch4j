package com.github.twitch4j.core.socket;

import com.github.twitch4j.core.AbstractSocketClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.channels.NotYetConnectedException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class Java11Socket implements TwitchSocket {
    private final WebSocket.Builder client;
    @Getter private final URI url;
    private final SocketDispatcher dispatcher;

    private WebSocket ws;

    @Override
    public boolean isActive() {
        return ws != null && !ws.isInputClosed() && !ws.isOutputClosed();
    }

    @Override
    public void open() throws Exception {
        client.buildAsync(url, new WebSocket.Listener() {
            @Override
            public void onOpen(WebSocket webSocket) {
                Java11Socket.this.ws = webSocket;
            }

            @Override
            public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                webSocket.request(1);
                dispatcher.onMessage(data.toString());
                return new CompletableFuture<>();
            }

            @Override
            public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                dispatcher.onDisconnect(statusCode, reason);
                Java11Socket.this.ws = null;
                return new CompletableFuture<>();
            }

            @Override
            public void onError(WebSocket webSocket, Throwable error) {
                dispatcher.onError(error);
            }
        }).thenRun(dispatcher::onConnect).get();
    }

    @Override
    public void sendRaw(String raw) throws Exception {
        if (!isActive()) throw new NotYetConnectedException();
        else ws.sendText(raw, true).get();
    }

    @Override
    public void close(int code, @Nullable String reason) throws Exception {
        if (!isActive()) throw new NotYetConnectedException();
        else ws.sendClose(code, (reason == null) ? "" : reason).get();
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
            HttpClient.Builder http = HttpClient.newBuilder();
            config.client.accept(http);
            WebSocket.Builder ws = http.build().newWebSocketBuilder();
            config.ws.accept(ws);

            return new Java11Socket(ws, config.getUrl(), new BaseSocketDispatcher<>(client, config.getEventCompose()));
        }
    }

    public static class Config extends TwitchSocketConfig {
        private Consumer<WebSocket.Builder> ws = builder -> {};
        private Consumer<HttpClient.Builder> client = builder -> {};

        private Config(URI url) {
            super(url);
        }

        public void configureWebsocket(Consumer<WebSocket.Builder> builder) {
            ws = ws.andThen(builder);
        }

        public void configureClient(Consumer<HttpClient.Builder> builder) {
            client = client.andThen(builder);
        }
    }
}
