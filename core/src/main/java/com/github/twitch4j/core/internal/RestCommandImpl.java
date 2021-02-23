package com.github.twitch4j.core.internal;

import com.github.twitch4j.core.RestCommand;
import com.github.twitch4j.core.TwitchCompanion;
import com.github.twitch4j.core.http.TwitchRequest;

import java.util.function.Consumer;

public class RestCommandImpl<T> extends AbstractRestCommand<T> implements RestCommand<T> {
    private final TwitchRequest request;
    private final Class<T> responseType;

    public RestCommandImpl(TwitchCompanion companion, TwitchRequest request, Class<T> responseType) {
        super(companion);
        this.request = request;
        this.responseType = responseType;
    }

    @Override
    protected T doExecute() throws Throwable {
        return getCompanion().getHttpClient().execute(request, responseType);
    }

    @Override
    protected void doEnqueue(Consumer<T> result, Consumer<Throwable> error) {
        try {
            result.accept(getCompanion().getHttpClient().execute(request, responseType));
        } catch (Throwable e) {
            error.accept(e);
        }
    }
}
