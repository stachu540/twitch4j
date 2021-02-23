package com.github.twitch4j.auth;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

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

    public void addScope(TwitchScope... scopes) {
        addScope(Arrays.asList(scopes));
    }

    public void addScope(Collection<TwitchScope> scopes) {
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
    final URI build() {
        StringBuffer sb = new StringBuffer("https://id.twitch.tv/oauth2/authorize");
        sb.append(createQuery().entrySet().stream().map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue())).collect(Collectors.joining("&", "?", "")));

        return URI.create(sb.toString());
    }
}
