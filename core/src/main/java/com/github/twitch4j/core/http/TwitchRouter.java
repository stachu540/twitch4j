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
import java.net.URL;
import java.util.regex.Pattern;

public final class TwitchRouter {
  public static final TwitchRouter OAUTH = new TwitchRouter("https://id.twitch.tv/oauth2/");
  public static final TwitchRouter API = new TwitchRouter("https://api.twitch.tv/helix");
  public static final TwitchRouter TMI = new TwitchRouter("https://tmi.twitch.tv/");

  public static final TwitchRouter EVENTSUB = API.append("/eventsub/subscriptions");
  public static final TwitchRouter WEBSUB = API.append("/webhooks/hub");

  private static final Pattern URL_MATCHES = Pattern.compile(
    "^http[s]?://([a-z0-9]+\\.)+[a-z0-9]+(/.+)?", Pattern.CASE_INSENSITIVE
  );

  final String baseUrl;

  private TwitchRouter(final URL baseUrl) {
    this(baseUrl.toString());
  }

  private TwitchRouter(final URI baseUrl) {
    this(baseUrl.toString());
  }

  private TwitchRouter(final String baseUrl) {
    if (!URL_MATCHES.matcher(baseUrl).matches()) {
      throw new IllegalArgumentException("Current base url is not allowed.");
    }
    this.baseUrl = baseUrl.toLowerCase();
  }

  public static TwitchRouter custom(final String baseUrl) {
    return new TwitchRouter(baseUrl);
  }

  public static TwitchRouter custom(final URL baseUrl) {
    return new TwitchRouter(baseUrl);
  }

  public static TwitchRouter custom(final URI baseUrl) {
    return new TwitchRouter(baseUrl);
  }

  private TwitchRoute create(final TwitchRoute.Method method, final String endpoint) {
    return new TwitchRoute(this, method, endpoint);
  }

  public TwitchRoute get(final String endpoint) {
    return create(TwitchRoute.Method.GET, endpoint);
  }

  public TwitchRoute post(final String endpoint) {
    return create(TwitchRoute.Method.POST, endpoint);
  }

  public TwitchRoute put(final String endpoint) {
    return create(TwitchRoute.Method.PUT, endpoint);
  }

  public TwitchRoute patch(final String endpoint) {
    return create(TwitchRoute.Method.PATCH, endpoint);
  }

  public TwitchRoute delete(final String endpoint) {
    return create(TwitchRoute.Method.DELETE, endpoint);
  }

  public TwitchRoute options(final String endpoint) {
    return create(TwitchRoute.Method.OPTIONS, endpoint);
  }

  private TwitchRouter append(final String endpoint) {
    return new TwitchRouter(URI.create(baseUrl + endpoint));
  }
}
