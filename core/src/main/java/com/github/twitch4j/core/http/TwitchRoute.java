package com.github.twitch4j.core.http;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MultiValuedMap;

import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public final class TwitchRoute {
    private final TwitchRouter router;
    @Getter
    private final Method method;
    private final String endpoint;

    public String getRawUri() {
        return URI.create(router.baseUrl + endpoint).toString();
    }

    public URI buildUri(Map<String, String> pathParameters, MultiValuedMap<String, String> queryParameters) {
        String uri = getRawUri();

        for (Map.Entry<String, String> entry : pathParameters.entrySet()) {
            if (uri.contains("{" + entry.getKey() + "}")) {
                uri = uri.replaceFirst("\\{" + entry.getKey() + "}", entry.getValue());
            }
        }

        uri = uri + queryParameters.entries().stream().map(e -> e.getKey() + "=" + e.getValue()).collect(Collectors.joining("&", "?", ""));

        return URI.create(uri);
    }

    public enum Method {
        GET, POST, PUT, PATCH, DELETE, OPTIONS
    }
}
