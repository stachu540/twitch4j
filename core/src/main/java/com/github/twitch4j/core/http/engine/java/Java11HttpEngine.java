package com.github.twitch4j.core.http.engine.java;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.twitch4j.core.http.TwitchRequest;
import com.github.twitch4j.core.http.TwitchResponse;
import com.github.twitch4j.core.http.engine.HttpEngine;
import com.github.twitch4j.core.http.engine.HttpEngineConfig;
import com.github.twitch4j.core.http.engine.HttpEngineFactory;
import lombok.Getter;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.io.IOException;
import java.io.InputStream;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Consumer;

public class Java11HttpEngine implements HttpEngine {

    private final HttpClient realClient;

    Java11HttpEngine(HttpClient client) {
        if (Integer.parseInt(System.getProperty("java.specification.version").split("\\.")[0]) < 9) {
            throw new RuntimeException("Current engine instance doesn't allow to be used in this java version: " + System.getProperty("java.specification.version") + ". Make sure if you are using JVM 9+");
        }
        this.realClient = client;
    }

    @Override
    public TwitchResponse execute(TwitchRequest request) throws Exception {
        return convertToResponse(request, realClient.send(convertToRequest(request), HttpResponse.BodyHandlers.ofInputStream()));
    }

    private HttpRequest convertToRequest(TwitchRequest synthetic) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(synthetic.getUrl());
        builder.method(synthetic.getMethod().toString(), (synthetic.getBody() != null) ? HttpRequest.BodyPublishers.ofString(synthetic.getBody().asText()) : HttpRequest.BodyPublishers.noBody());
        synthetic.getHeaders().entries().forEach(e -> builder.header(e.getKey(), e.getValue()));

        return builder.build();
    }

    private TwitchResponse convertToResponse(TwitchRequest request, HttpResponse<InputStream> response) throws IOException {
        int code = response.statusCode();
        ListValuedMap<String, String> headers = new ArrayListValuedHashMap<>();
        response.headers().map().forEach(headers::putAll);
        JsonNode body = (response.body().available() > 0) ? new JsonMapper().readTree(response.body()) : null;

        return new TwitchResponse(code, body, headers, request);
    }

    public static class Config extends HttpEngineConfig {
        @Getter
        private Consumer<HttpClient.Builder> builder = builder -> {
        };

        Config() {
            super();
        }

        public void configureBuilder(Consumer<HttpClient.Builder> builder) {
            this.builder = this.builder.andThen(builder);
        }
    }

    public static class Factory implements HttpEngineFactory<Config> {
        @Override
        public Config configure(Consumer<Config> configure) {
            Config config = new Config();
            configure.accept(config);
            return config;
        }

        @Override
        public Java11HttpEngine install(Config configure) {
            HttpClient.Builder builder = HttpClient.newBuilder()
                .connectTimeout(configure.getTimeout());
            if (configure.getProxyAddress() != null) {
                builder.proxy(ProxySelector.of(configure.getProxyAddress()));
            }

            configure.getBuilder().accept(builder);

            return new Java11HttpEngine(builder.build());
        }
    }
}
