package com.github.twitch4j.auth;

import com.github.twitch4j.auth.dao.*;
import com.github.twitch4j.core.AbstractRestClient;
import com.github.twitch4j.core.RestCommand;
import com.github.twitch4j.core.TwitchCompanion;
import com.github.twitch4j.core.http.TwitchRouter;
import lombok.Getter;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URLEncoder;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public final class TwitchAuthentication extends AbstractRestClient {
    private final TwitchAuthentication.Config configuration;

    public TwitchAuthentication(TwitchCompanion companion, TwitchAuthentication.Config configuration) {
        super(companion, TwitchRouter.OAUTH);
        this.configuration = configuration;
    }

    @SuppressWarnings("deprecation")
    public final RestCommand<ApplicationCredential> asApplication(Set<TwitchScope> scope) {
        if (getCompanion().getClientSecret() == null) throw new NullPointerException("Client Secret is Required!");
        return post("/token", AccessToken.class, spec -> {
            spec.setQueryParameter("client_id", getCompanion().getClientId());
            spec.setQueryParameter("client_secret", getCompanion().getClientSecret());
            spec.setQueryParameter("grant_type", GrantType.CLIENT_CREDENTIALS.name().toLowerCase());
            spec.setQueryParameter("scope", URLEncoder.encode(scope.stream().map(TwitchScope::getValue).collect(Collectors.joining(" "))));
        }).map(at -> new ApplicationCredential(at.getAccessToken(), at.getExpiredAt(), at.getScope(), getCompanion().getClientId()))
            .doOnSuccess(configuration.tokenCache.getApplication()::set);
    }

    @SuppressWarnings("deprecation")
    public final RestCommand<Credential> asUser(String code, URI redirectUri) {
        if (getCompanion().getClientSecret() == null) throw new NullPointerException("Client Secret is Required!");
        return post("/token", AccessToken.class, spec -> {
            spec.setQueryParameter("client_id", getCompanion().getClientId());
            spec.setQueryParameter("client_secret", getCompanion().getClientSecret());
            spec.setQueryParameter("grant_type", GrantType.AUTHORIZATION_CODE.name().toLowerCase());
            spec.setQueryParameter("recirect_uri", URLEncoder.encode(redirectUri.toString()));
            spec.setQueryParameter("code", code);
        }).flatMap(at -> validate(at.getAccessToken()).map(v -> compose(at, v)))
            .doOnSuccess(configuration.tokenCache::add);
    }

    public final RestCommand<Credential> provideUser(@NotNull String accessToken, @NotNull String refreshToken) {
        return validate(accessToken).map(v -> compose(new ProvidedToken(accessToken, refreshToken), v))
            .doOnSuccess(configuration.tokenCache::add);
    }

    private Credential compose(Token token, Validate validate) {
        return new Credential(token.getAccessToken(), token.getRefreshToken(), validate.getExpiredAt(), validate.getScopes(), validate.getClientId(), validate.getLogin(), validate.getUserIdNumeric().intValue());
    }

    private RestCommand<Validate> validate(final String token) {
        return get("/validate", Validate.class, spec -> spec.setHeader("Authorization", "OAuth: " + token));
    }

    @Value
    public static class Config {
        Set<TwitchScope> defaultScope;
        URI defaultRedirectUri;
        TokenCache tokenCache;
    }
}
