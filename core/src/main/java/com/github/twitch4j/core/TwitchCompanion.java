/*
 * MIT License
 *
 * Copyright (c) 2021 Philipp Heuer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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
import com.github.twitch4j.core.internal.TwitchCompanionImpl;
import com.github.twitch4j.core.internal.http.engine.Java11HttpEngine;
import com.github.twitch4j.core.internal.http.engine.Java8HttpEngine;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

public interface TwitchCompanion {
  String getClientId();

  @Nullable String getClientSecret();

  EventManager getEventManager();

  TwitchHttpClient getHttpClient();

  TwitchAuthentication authenticate();

  @Setter
  @Getter
  @Accessors(chain = true)
  @NoArgsConstructor(access = AccessLevel.PRIVATE)
  final class Builder {
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

    public <C extends HttpEngineConfig, F extends HttpEngineFactory<C>> Builder install(
      final F factory, final Consumer<C> configure
    ) {
      httpClient = companion -> {
        C config = factory.configure(configure);
        return new TwitchHttpClient(companion, factory.install(config), config);
      };
      return this;
    }

    public Builder setDefaultScope(final TwitchScope... defaultScope) {
      setDefaultScope(Arrays.asList(defaultScope));
      return this;
    }

    public Builder setDefaultScope(final Iterable<TwitchScope> defaultScope) {
      this.defaultScope.clear();
      this.defaultScope.addAll(StreamSupport.stream(defaultScope.spliterator(), false)
        .collect(Collectors.toList()));
      return this;
    }

    public Builder registerEventHandler(final EventHandler eventHandler) {
      handlers.add(eventHandler);
      return this;
    }

    public Builder addDefaultScope(final TwitchScope... defaultScope) {
      return this;
    }

    public Builder addDefaultScope(final Iterable<TwitchScope> defaultScope) {
      return this;
    }

    public TwitchCompanion build() {
      return new TwitchCompanionImpl(this);
    }
  }
}
