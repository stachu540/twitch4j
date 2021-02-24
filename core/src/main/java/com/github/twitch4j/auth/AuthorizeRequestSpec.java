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
package com.github.twitch4j.auth;

import java.net.URI;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@Setter
@RequiredArgsConstructor
public final class AuthorizeRequestSpec {
  private final String clientId;
  private final Set<TwitchScope> scope = new LinkedHashSet<>();
  private URI redirectUri;
  private ResponseType responseType;
  @Nullable
  private String state;
  private boolean forceVerify;

  public void addScope(final TwitchScope... scopes) {
    addScope(Arrays.asList(scopes));
  }

  public void addScope(final Collection<TwitchScope> scopes) {
    scope.addAll(scopes);
  }

  private Map<String, String> createQuery() {
    Map<String, String> query = new LinkedHashMap<>();
    query.put("response_type", Objects.requireNonNull(responseType).name().toLowerCase());
    query.put("client_id", clientId);
    query.put("redirect_uri", Objects.requireNonNull(redirectUri).toString());
    query.put("scope", scope.stream().map(TwitchScope::getValue).collect(Collectors.joining(" ")));

    if (state != null) {
      query.put("state", state);
    }

    if (forceVerify) {
      query.put("force_verify", "true");
    }

    return query;
  }

  @SuppressWarnings("deprecation")
  URI build() {
    return URI.create("https://id.twitch.tv/oauth2/authorize"
      + createQuery().entrySet().stream().map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue()))
      .collect(Collectors.joining("&", "?", ""))
    );
  }
}
