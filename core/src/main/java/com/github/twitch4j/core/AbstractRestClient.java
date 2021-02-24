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

import com.github.twitch4j.core.http.TwitchRequestSpec;
import com.github.twitch4j.core.http.TwitchRoute;
import com.github.twitch4j.core.http.TwitchRouter;
import java.util.function.Consumer;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractRestClient extends AbstractTwitchObject {
  private final TwitchRouter router;
  private final Consumer<TwitchRequestSpec> appender;

  protected AbstractRestClient(final TwitchCompanion companion, final TwitchRouter router) {
    this(companion, router, spec -> {
    });
  }

  protected AbstractRestClient(
    final TwitchCompanion companion, final TwitchRouter router, final Consumer<TwitchRequestSpec> appender
  ) {
    super(companion);
    this.router = router;
    this.appender = appender;
  }

  private <T> RestCommand<T> create(
    final TwitchRoute route, final Class<T> type, @NotNull final Consumer<TwitchRequestSpec> spec
  ) {
    return getCompanion().getHttpClient().create(route, type, appender.andThen(spec));
  }

  protected final <T> RestCommand<T> get(
    final String endpoint, final Class<T> type, @NotNull final Consumer<TwitchRequestSpec> spec
  ) {
    return create(router.get(endpoint), type, spec);
  }

  protected final <T> RestCommand<T> get(final String endpoint, final Class<T> type) {
    return get(endpoint, type, spec -> {
    });
  }

  protected final <T> RestCommand<T> post(
    final String endpoint, final Class<T> type, @NotNull final Consumer<TwitchRequestSpec> spec
  ) {
    return getCompanion().getHttpClient().create(router.post(endpoint), type, spec);
  }

  protected final <T> RestCommand<T> post(final String endpoint, final Class<T> type) {
    return post(endpoint, type, spec -> {
    });
  }

  protected final <T> RestCommand<T> put(
    final String endpoint, final Class<T> type, @NotNull final Consumer<TwitchRequestSpec> spec
  ) {
    return getCompanion().getHttpClient().create(router.put(endpoint), type, spec);
  }

  protected final <T> RestCommand<T> put(final String endpoint, final Class<T> type) {
    return put(endpoint, type, spec -> {
    });
  }

  protected final <T> RestCommand<T> patch(
    final String endpoint, final Class<T> type, @NotNull final Consumer<TwitchRequestSpec> spec
  ) {
    return getCompanion().getHttpClient().create(router.patch(endpoint), type, spec);
  }

  protected final <T> RestCommand<T> patch(final String endpoint, final Class<T> type) {
    return patch(endpoint, type, spec -> {
    });
  }

  protected final <T> RestCommand<T> delete(
    final String endpoint, final Class<T> type, @NotNull final Consumer<TwitchRequestSpec> spec
  ) {
    return getCompanion().getHttpClient().create(router.delete(endpoint), type, spec);
  }

  protected final <T> RestCommand<T> delete(final String endpoint, final Class<T> type) {
    return delete(endpoint, type, spec -> {
    });
  }

  protected final <T> RestCommand<T> options(
    final String endpoint, final Class<T> type, @NotNull final Consumer<TwitchRequestSpec> spec
  ) {
    return getCompanion().getHttpClient().create(router.options(endpoint), type, spec);
  }

  protected final <T> RestCommand<T> options(final String endpoint, final Class<T> type) {
    return options(endpoint, type, spec -> {
    });
  }
}
