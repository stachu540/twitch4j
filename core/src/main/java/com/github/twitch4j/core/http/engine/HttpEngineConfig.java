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
package com.github.twitch4j.core.http.engine;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.twitch4j.core.dao.RestError;
import com.github.twitch4j.core.http.TwitchRequestSpec;
import com.github.twitch4j.core.http.TwitchResponse;
import com.github.twitch4j.core.http.TwitchRestException;
import java.net.Proxy;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class HttpEngineConfig {
  private Proxy proxy = Proxy.NO_PROXY;
  private Duration timeout = Duration.ofSeconds(30);
  @Setter(AccessLevel.NONE)
  private Function<TwitchResponse, Exception> responseErrorHandler = response -> {
    if (response.getStatusCode() == 429) {
      String key = response.getHeaders().keySet().stream()
        .filter(k -> k.equalsIgnoreCase("Ratelimit-Reset")).findFirst().get();
      return new TwitchRateLimitException(
        response.getRequest(), Instant.ofEpochMilli(
        Long.parseLong(new ArrayList<>(response.getHeaders().get(key)).get(1))
      )
      );
    }
    return new TwitchRestException(
      response.getRequest(), new JsonMapper().convertValue(
      Objects.requireNonNull(response.getBody(),
        "Body is empty in this status code: " + response.getStatusCode()
      ), RestError.class)
    );
  };
  @Setter(AccessLevel.NONE)
  private Consumer<TwitchRequestSpec> requestAppender = request -> {
  };

  public void handleErrorResponse(final Function<TwitchResponse, Exception> responseErrorHandler) {
    this.responseErrorHandler = response -> {
      Exception ex = responseErrorHandler.apply(response);
      return (ex != null) ? ex : this.responseErrorHandler.apply(response);
    };
  }

  public void appendRequest(final Consumer<TwitchRequestSpec> appendRequest) {
    requestAppender = requestAppender.andThen(appendRequest);
  }
}
