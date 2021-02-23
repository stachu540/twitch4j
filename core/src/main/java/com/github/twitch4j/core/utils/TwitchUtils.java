package com.github.twitch4j.core.utils;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.twitch4j.core.http.TwitchRequestSpec;

import java.net.URI;
import java.util.function.Consumer;

public class TwitchUtils {
    public static JsonMapper defaultMapper() {
        return JsonMapper.builder().propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .findAndAddModules().build();
    }

    public static Consumer<TwitchRequestSpec> defaultRequest() {
        return spec -> {
            spec.addHeader("User-Agent", "Twitch4J v2 (https://twitch4j.github.io)");
            spec.addHeader("Content-Type", "application/json");
            spec.addHeader("Accept", "application/json");
        };
    }

    public static String stripUrl(URI url) {
        StringBuilder sb = new StringBuilder();
        sb.append(url.getScheme()).append("://")
            .append(url.getHost());
        if (url.getPort() > 0) {
            sb.append(url.getPort());
        }
        sb.append(url.getPath());

        return sb.toString();
    }
}
