package com.github.twitch4j.core.http;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.twitch4j.core.AbstractTwitchObject;
import com.github.twitch4j.core.RestCommand;
import com.github.twitch4j.core.TwitchCompanion;
import com.github.twitch4j.core.http.engine.HttpEngine;
import com.github.twitch4j.core.http.engine.HttpEngineConfig;
import com.github.twitch4j.core.http.engine.TwitchRateLimitException;
import com.github.twitch4j.core.internal.RestCommandImpl;
import com.github.twitch4j.core.utils.TwitchUtils;
import lombok.Getter;
import org.apache.commons.collections4.MultiMapUtils;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

public class TwitchHttpClient extends AbstractTwitchObject {
    private final HttpEngine engine;
    private final Consumer<TwitchRequestSpec> requestAppend;
    @Getter
    private final JsonMapper mapper = TwitchUtils.defaultMapper();
    private final Function<TwitchResponse, Throwable> responseErrorHandler;
    private final Duration timeout;

    public TwitchHttpClient(TwitchCompanion companion, HttpEngine engine, HttpEngineConfig config) {
        super(companion);
        if (config.getRequestAppender() != null) {
            this.requestAppend = TwitchUtils.defaultRequest().andThen(config.getRequestAppender());
        } else {
            this.requestAppend = TwitchUtils.defaultRequest();
        }
        this.engine = engine;
        this.responseErrorHandler = config.getResponseErrorHandler();
        this.timeout = config.getTimeout();
    }

    public <T> RestCommand<T> create(TwitchRoute route, Class<T> resultType, @NotNull Consumer<TwitchRequestSpec> request) {
        return new RestCommandImpl<>(getCompanion(), createRequest(route, request), resultType);
    }

    public <T> T execute(TwitchRequest request, Class<T> resultType) throws Throwable {
        TwitchResponse response = engine.execute(request);

        if (response.getStatusCode() >= 400 && response.getStatusCode() < 600) {
            try {
                throw responseErrorHandler.apply(response);
            } catch (TwitchRateLimitException e) {
                // hold request until time has been passed
                Duration time = Duration.between(e.getResetAt(), Instant.now());
                if (timeout.getSeconds() < time.getSeconds()) {
                    throw new TimeoutException("Cannot execute this operation").initCause(e);
                } else {
                    TimeUnit.SECONDS.sleep(time.getSeconds());
                    return execute(e.getRequest(), resultType);
                }
            }
        }

        return mapper.convertValue(engine.execute(request).getBody(), resultType);
    }

    private TwitchRequest createRequest(TwitchRoute route, @NotNull Consumer<TwitchRequestSpec> request) {
        TwitchRequestSpec spec = new TwitchRequestSpec(route, mapper);
        requestAppend.andThen(request).accept(spec);

        return new TwitchRequest(spec.getUrl(), spec.getMethod(), MultiMapUtils.unmodifiableMultiValuedMap(spec.getHeaders()), spec.getBody());
    }
}
