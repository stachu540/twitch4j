package com.github.twitch4j.core.http.engine;

import com.github.twitch4j.core.TwitchException;
import com.github.twitch4j.core.http.TwitchRequest;
import com.github.twitch4j.core.utils.TwitchUtils;
import lombok.Getter;

import java.time.Instant;

@Getter
public class TwitchRateLimitException extends TwitchException {
    private final TwitchRequest request;
    private final Instant resetAt;

    public TwitchRateLimitException(TwitchRequest request, Instant resetAt) {
        super(String.format("[%s] %s -> Rate Limit has been reach. Next request: %s", request.getMethod(), TwitchUtils.stripUrl(request.getUrl()), resetAt));
        this.request = request;
        this.resetAt = resetAt;
    }
}
