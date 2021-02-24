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

import com.github.twitch4j.auth.dao.AccessToken;
import com.github.twitch4j.auth.dao.ApplicationCredential;
import com.github.twitch4j.auth.dao.Credential;
import com.github.twitch4j.auth.dao.ProvidedToken;
import com.github.twitch4j.auth.dao.Token;
import com.github.twitch4j.auth.dao.Validate;
import com.github.twitch4j.core.AbstractRestClient;
import com.github.twitch4j.core.RestCommand;
import com.github.twitch4j.core.TwitchCompanion;
import com.github.twitch4j.core.http.TwitchRouter;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Getter
public final class TwitchAuthentication extends AbstractRestClient {
  private final TwitchAuthentication.Config configuration;

  public TwitchAuthentication(
    final TwitchCompanion companion, final TwitchAuthentication.Config configuration
  ) {
    super(companion, TwitchRouter.OAUTH);
    this.configuration = configuration;
  }

  @SuppressWarnings("deprecation")
  public RestCommand<ApplicationCredential> asApplication(final Set<TwitchScope> scope) {
    if (getCompanion().getClientSecret() == null) {
      throw new NullPointerException("Client Secret is Required!");
    }
    return post("/token", AccessToken.class, spec -> {
      spec.setQueryParameter("client_id", getCompanion().getClientId());
      spec.setQueryParameter("client_secret", getCompanion().getClientSecret());
      spec.setQueryParameter("grant_type", GrantType.CLIENT_CREDENTIALS.name().toLowerCase());
      spec.setQueryParameter("scope", URLEncoder.encode(
        scope.stream().map(TwitchScope::getValue).collect(Collectors.joining(" "))
      ));
    }).map(at -> new ApplicationCredential(
      at.getAccessToken(), at.getExpiredAt(), at.getScope(), getCompanion().getClientId()
    )).doOnSuccess(configuration.tokenCache.getApplication()::set);
  }

  @SuppressWarnings("deprecation")
  public RestCommand<Credential> asUser(final String code, final URI redirectUri) {
    if (getCompanion().getClientSecret() == null) {
      throw new NullPointerException("Client Secret is Required!");
    }
    return post("/token", AccessToken.class, spec -> {
      spec.setQueryParameter("client_id", getCompanion().getClientId());
      spec.setQueryParameter("client_secret", getCompanion().getClientSecret());
      spec.setQueryParameter("grant_type", GrantType.AUTHORIZATION_CODE.name().toLowerCase());
      spec.setQueryParameter("redirect_uri", URLEncoder.encode(redirectUri.toString()));
      spec.setQueryParameter("code", code);
    }).flatMap(at -> validate(at.getAccessToken()).map(v -> compose(at, v)))
      .doOnSuccess(configuration.tokenCache::add);
  }

  public RestCommand<Credential> asUser(final String code) {
    return asUser(code, configuration.defaultRedirectUri);
  }

  public RestCommand<Credential> provideUser(
    final @NotNull String accessToken, final @NotNull String refreshToken
  ) {
    return validate(accessToken).map(v -> compose(new ProvidedToken(accessToken, refreshToken), v))
      .doOnSuccess(configuration.tokenCache::add);
  }

  private Credential compose(final Token token, final Validate validate) {
    return new Credential(
      token.getAccessToken(), token.getRefreshToken(), validate.getExpiredAt(),
      validate.getScopes(), validate.getClientId(), validate.getLogin(),
      validate.getUserIdNumeric().intValue()
    );
  }

  private RestCommand<Validate> validate(final String token) {
    return get("/validate", Validate.class, spec -> {
      spec.setHeader("Authorization", "OAuth: " + token);
    });
  }

  @Value
  public static class Config {
    Set<TwitchScope> defaultScope;
    URI defaultRedirectUri;
    TokenCache tokenCache;
  }
}
