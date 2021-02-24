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

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MultiValuedMap;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class TwitchRoute {
  private final TwitchRouter router;
  @Getter
  private final Method method;
  private final String endpoint;

  public String getRawUri() {
    return URI.create(router.baseUrl + endpoint).toString();
  }

  public URI buildUri(
    final Map<String, String> pathParameters,
    final MultiValuedMap<String, String> queryParameters
  ) {
    String uri = getRawUri();

    for (Map.Entry<String, String> entry : pathParameters.entrySet()) {
      if (uri.contains("{" + entry.getKey() + "}")) {
        uri = uri.replaceFirst("\\{" + entry.getKey() + "}", entry.getValue());
      }
    }

    uri = uri + queryParameters.entries().stream()
      .map(e -> e.getKey() + "=" + e.getValue())
      .collect(Collectors.joining("&", "?", ""));

    return URI.create(uri);
  }

  public enum Method {
    GET, POST, PUT, PATCH, DELETE, OPTIONS
  }
}
