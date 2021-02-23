package com.github.twitch4j.core.http.engine;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.twitch4j.core.dao.RestError;
import com.github.twitch4j.core.http.TwitchRequestSpec;
import com.github.twitch4j.core.http.TwitchResponse;
import com.github.twitch4j.core.http.TwitchRestException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class HttpEngineConfig {
    @Nullable
    private InetSocketAddress proxyAddress = null;
    private Duration timeout = Duration.ofSeconds(30L);
    @Setter(AccessLevel.NONE)
    private Function<TwitchResponse, Throwable> responseErrorHandler = response -> {
        if (response.getStatusCode() == 429) {
            String key = response.getHeaders().keySet().stream().filter(k -> k.equalsIgnoreCase("Ratelimit-Reset")).findFirst().get();
            return new TwitchRateLimitException(response.getRequest(), Instant.ofEpochMilli(Long.parseLong(new ArrayList<>(response.getHeaders().get(key)).get(1))));
        }
        return new TwitchRestException(response.getRequest(), new JsonMapper().convertValue(Objects.requireNonNull(response.getBody(), "Body is empty in this status code: " + response.getStatusCode()), RestError.class));
    };
    @Setter(AccessLevel.NONE)
    private Consumer<TwitchRequestSpec> requestAppender = request -> {
    };

    public void handleErrorResponse(Function<TwitchResponse, Throwable> responseErrorHandler) {
        this.responseErrorHandler = response -> {
            Throwable ex = responseErrorHandler.apply(response);
            return (ex != null) ? ex : this.responseErrorHandler.apply(response);
        };
    }

    public void appendRequest(Consumer<TwitchRequestSpec> appendRequest) {
        requestAppender = requestAppender.andThen(appendRequest);
    }
}
