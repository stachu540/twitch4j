package com.github.twitch4j.core;

import com.github.twitch4j.core.http.TwitchRequestSpec;
import com.github.twitch4j.core.http.TwitchRouter;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public abstract class AbstractRestClient extends AbstractTwitchObject {
    private final TwitchRouter router;
    private final Consumer<TwitchRequestSpec> appender;

    protected AbstractRestClient(TwitchCompanion companion, TwitchRouter router) {
        this(companion, router, spec -> {
        });
    }

    protected AbstractRestClient(TwitchCompanion companion, TwitchRouter router, Consumer<TwitchRequestSpec> appender) {
        super(companion);
        this.router = router;
        this.appender = appender;
    }

    protected final <T> RestCommand<T> get(String endpoint, Class<T> type, @NotNull Consumer<TwitchRequestSpec> spec) {
        return getCompanion().getHttpClient().create(router.get(endpoint), type, spec);
    }

    protected final <T> RestCommand<T> get(String endpoint, Class<T> type) {
        return get(endpoint, type, spec -> {
        });
    }

    protected final <T> RestCommand<T> post(String endpoint, Class<T> type, @NotNull Consumer<TwitchRequestSpec> spec) {
        return getCompanion().getHttpClient().create(router.post(endpoint), type, spec);
    }

    protected final <T> RestCommand<T> post(String endpoint, Class<T> type) {
        return post(endpoint, type, spec -> {
        });
    }

    protected final <T> RestCommand<T> put(String endpoint, Class<T> type, @NotNull Consumer<TwitchRequestSpec> spec) {
        return getCompanion().getHttpClient().create(router.put(endpoint), type, spec);
    }

    protected final <T> RestCommand<T> put(String endpoint, Class<T> type) {
        return put(endpoint, type, spec -> {
        });
    }

    protected final <T> RestCommand<T> patch(String endpoint, Class<T> type, @NotNull Consumer<TwitchRequestSpec> spec) {
        return getCompanion().getHttpClient().create(router.patch(endpoint), type, spec);
    }

    protected final <T> RestCommand<T> patch(String endpoint, Class<T> type) {
        return patch(endpoint, type, spec -> {
        });
    }

    protected final <T> RestCommand<T> delete(String endpoint, Class<T> type, @NotNull Consumer<TwitchRequestSpec> spec) {
        return getCompanion().getHttpClient().create(router.delete(endpoint), type, spec);
    }

    protected final <T> RestCommand<T> delete(String endpoint, Class<T> type) {
        return delete(endpoint, type, spec -> {
        });
    }

    protected final <T> RestCommand<T> options(String endpoint, Class<T> type, @NotNull Consumer<TwitchRequestSpec> spec) {
        return getCompanion().getHttpClient().create(router.options(endpoint), type, spec);
    }

    protected final <T> RestCommand<T> options(String endpoint, Class<T> type) {
        return options(endpoint, type, spec -> {
        });
    }
}
