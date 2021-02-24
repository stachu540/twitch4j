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
package com.github.twitch4j.core.http;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.twitch4j.core.AbstractTwitchObject;
import com.github.twitch4j.core.RestCommand;
import com.github.twitch4j.core.TwitchCompanion;
import com.github.twitch4j.core.http.engine.HttpEngine;
import com.github.twitch4j.core.http.engine.HttpEngineConfig;
import com.github.twitch4j.core.http.engine.TwitchRateLimitException;
import com.github.twitch4j.core.internal.http.RestCommandImpl;
import com.github.twitch4j.core.utils.TwitchUtils;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.Getter;
import org.apache.commons.collections4.MultiMapUtils;
import org.jetbrains.annotations.NotNull;

public class TwitchHttpClient extends AbstractTwitchObject {
  private final HttpEngine engine;
  private final Consumer<TwitchRequestSpec> requestAppend;
  @Getter
  private final JsonMapper mapper = TwitchUtils.defaultMapper();
  private final Function<TwitchResponse, Exception> responseErrorHandler;
  private final Duration timeout;

  public TwitchHttpClient(
    final TwitchCompanion companion,
    final HttpEngine engine,
    final HttpEngineConfig config
  ) {
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

  public <T> RestCommand<T> create(
    final TwitchRoute route, final Class<T> resultType,
    final @NotNull Consumer<TwitchRequestSpec> request
  ) {
    return new RestCommandImpl<>(getCompanion(), createRequest(route, request), resultType);
  }

  public <T> T execute(final TwitchRequest request, final Class<T> resultType) throws Exception {
    TwitchResponse response = engine.execute(request);

    if (response.getStatusCode() >= 400 && response.getStatusCode() < 600) {
      try {
        throw responseErrorHandler.apply(response);
      } catch (TwitchRateLimitException e) {
        // hold request until time has been passed
        Duration time = Duration.between(e.getResetAt(), Instant.now());
        if (timeout.getSeconds() < time.getSeconds()) {
          TimeoutException ex = new TimeoutException("Cannot execute this operation");
          ex.initCause(e);
          throw ex;
        } else {
          TimeUnit.SECONDS.sleep(time.getSeconds());
          return execute(e.getRequest(), resultType);
        }
      }
    }

    return mapper.convertValue(engine.execute(request).getBody(), resultType);
  }

  private TwitchRequest createRequest(
    final TwitchRoute route, final @NotNull Consumer<TwitchRequestSpec> request
  ) {
    TwitchRequestSpec spec = new TwitchRequestSpec(route, mapper);
    requestAppend.andThen(request).accept(spec);

    return new TwitchRequest(
      spec.getUrl(), spec.getMethod(),
      MultiMapUtils.unmodifiableMultiValuedMap(spec.getHeaders()),
      spec.getBody()
    );
  }
}
