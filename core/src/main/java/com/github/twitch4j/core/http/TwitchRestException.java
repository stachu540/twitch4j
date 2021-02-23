package com.github.twitch4j.core.http;

import com.github.twitch4j.core.TwitchException;
import com.github.twitch4j.core.dao.RestError;
import com.github.twitch4j.core.utils.TwitchUtils;
import lombok.Getter;

@Getter
public class TwitchRestException extends TwitchException {
    private final RestError error;
    private final TwitchRequest request;

    public TwitchRestException(TwitchRequest request, RestError error) {
        super(String.format("[%s] %s -> [%d] %s", request.getMethod(), TwitchUtils.stripUrl(request.getUrl()), error.getStatus(), (!error.getMessage().equals("")) ? error.getMessage() : error.getError()));
        this.request = request;
        this.error = error;
    }
}
