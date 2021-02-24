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
package com.github.twitch4j.core.internal;

import com.github.twitch4j.auth.TwitchAuthentication;
import com.github.twitch4j.core.TwitchCompanion;
import com.github.twitch4j.core.event.EventManager;
import com.github.twitch4j.core.http.TwitchHttpClient;
import com.github.twitch4j.core.internal.event.SimpleEventHandler;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public final class TwitchCompanionImpl implements TwitchCompanion {
  private final String clientId;
  @Nullable
  private final String clientSecret;
  private final EventManager eventManager = new EventManager();
  @Getter(AccessLevel.NONE)
  private final TwitchAuthentication authentication;
  private final TwitchHttpClient httpClient;

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  public TwitchCompanionImpl(final TwitchCompanion.Builder builder) {
    this.clientId = Objects.requireNonNull(builder.getClientId(), "Client ID is Required!");
    this.clientSecret = builder.getClientSecret();
    this.httpClient = builder.getHttpClient().apply(this);
    this.authentication = new TwitchAuthentication(this, new TwitchAuthentication.Config(
      builder.getDefaultScope(),
      builder.getDefaultRedirectUri(),
      builder.getTokenCache()
    ));
    if (builder.getHandlers().isEmpty()) {
      eventManager.registerHandler(new SimpleEventHandler());
      eventManager.setDefaultEventHandler(SimpleEventHandler.class);
    } else {
      builder.getHandlers().forEach(eventManager::registerHandler);
      eventManager.setDefaultEventHandler(builder.getHandlers().stream().findFirst().get().getClass());
    }

  }

  public TwitchAuthentication authenticate() {
    return authentication;
  }

}
