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
package com.github.twitch4j.core.internal.http;

import com.github.twitch4j.core.RestCommand;
import com.github.twitch4j.core.TwitchCompanion;
import com.github.twitch4j.core.http.TwitchRequest;
import java.util.function.Consumer;

public class RestCommandImpl<T> extends AbstractRestCommand<T> implements RestCommand<T> {
  private final TwitchRequest request;
  private final Class<T> responseType;

  public RestCommandImpl(final TwitchCompanion companion, final TwitchRequest request, final Class<T> responseType) {
    super(companion);
    this.request = request;
    this.responseType = responseType;
  }

  @Override
  protected final T doExecute() throws Exception {
    return getCompanion().getHttpClient().execute(request, responseType);
  }

  @Override
  protected final void doEnqueue(final Consumer<T> result, final Consumer<Exception> error) {
    try {
      result.accept(getCompanion().getHttpClient().execute(request, responseType));
    } catch (Exception e) {
      error.accept(e);
    }
  }
}
