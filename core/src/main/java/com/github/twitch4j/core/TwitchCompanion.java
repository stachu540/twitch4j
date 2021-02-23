package com.github.twitch4j.core;

import com.github.twitch4j.auth.TokenCache;
import com.github.twitch4j.auth.TwitchAuthentication;
import com.github.twitch4j.auth.TwitchScope;
import com.github.twitch4j.auth.internal.DefaultTokenCache;
import com.github.twitch4j.core.event.EventHandler;
import com.github.twitch4j.core.event.EventManager;
import com.github.twitch4j.core.http.TwitchHttpClient;
import com.github.twitch4j.core.http.engine.HttpEngineConfig;
import com.github.twitch4j.core.http.engine.HttpEngineFactory;
import com.github.twitch4j.core.http.engine.java.Java11HttpEngine;
import com.github.twitch4j.core.http.engine.java.Java8HttpEngine;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Getter
public final class TwitchCompanion {
    private final String clientId;
    @Nullable
    private final String clientSecret;
    private final EventManager eventManager = new EventManager();
    @Getter(AccessLevel.NONE)
    private final TwitchAuthentication authentication;
    private final TwitchHttpClient httpClient;

    private TwitchCompanion(Builder builder) {
        this.clientId = Objects.requireNonNull(builder.clientId, "Client ID is Required!");
        this.clientSecret = builder.clientSecret;
        this.httpClient = builder.httpClient.apply(this);
        this.authentication = new TwitchAuthentication(this, new TwitchAuthentication.Config(
            builder.defaultScope,
            builder.defaultRedirectUri,
            builder.tokenCache
        ));
        builder.handlers.forEach(eventManager::registerHandler);
    }

    public final TwitchAuthentication authenticate() {
        return authentication;
    }

    @Setter
    @Accessors(chain = true)
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {
        private final Set<TwitchScope> defaultScope = new LinkedHashSet<>();
        private final Set<EventHandler> handlers = new CopyOnWriteArraySet<>();
        private String clientId;
        @Nullable
        private String clientSecret;
        @Setter(AccessLevel.NONE)
        private Function<TwitchCompanion, TwitchHttpClient> httpClient; // uses jdk
        private TokenCache tokenCache = new DefaultTokenCache(); // cached in vm session
        private URI defaultRedirectUri = URI.create("https://localhost:8080");

        {
            int version = Integer.parseInt(System.getProperty("java.specification.version").split("\\.")[0]);
            if (version > 8) {
                install(new Java11HttpEngine.Factory(), configure -> {
                });
            } else {
                install(new Java8HttpEngine.Factory(), configure -> {
                });
            }
        }

        public final <C extends HttpEngineConfig, F extends HttpEngineFactory<C>> Builder install(F factory, Consumer<C> configure) {
            httpClient = companion -> {
                C config = factory.configure(configure);
                return new TwitchHttpClient(companion, factory.install(config), config);
            };
            return this;
        }

        public final Builder setDefaultScope(TwitchScope... defaultScope) {
            setDefaultScope(Arrays.asList(defaultScope));
            return this;
        }

        public final Builder setDefaultScope(Iterable<TwitchScope> defaultScope) {
            this.defaultScope.clear();
            this.defaultScope.addAll(StreamSupport.stream(defaultScope.spliterator(), false).collect(Collectors.toList()));
            return this;
        }

        public final Builder registerEventHandler(EventHandler eventHandler) {
            handlers.add(eventHandler);
            return this;
        }

        public final Builder addDefaultScope(TwitchScope... defaultScope) {
            return this;
        }

        public final Builder addDefaultScope(Iterable<TwitchScope> defaultScope) {
            return this;
        }

        public TwitchCompanion build() {
            return new TwitchCompanion(this);
        }
    }
}
