package com.github.twitch4j.core.http.engine.java;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.twitch4j.core.http.TwitchRequest;
import com.github.twitch4j.core.http.TwitchResponse;
import com.github.twitch4j.core.http.TwitchRoute;
import com.github.twitch4j.core.http.engine.HttpEngine;
import com.github.twitch4j.core.http.engine.HttpEngineConfig;
import com.github.twitch4j.core.http.engine.HttpEngineFactory;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.MultiMapUtils;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.time.Duration;
import java.util.function.Consumer;

public class Java8HttpEngine implements HttpEngine {
    private final Proxy proxy;
    private final Duration timeout;

    Java8HttpEngine(Proxy proxy, Duration timeout) {
        if (Integer.parseInt(System.getProperty("java.specification.version").split("\\.")[0]) >= 9) {
            throw new RuntimeException("Current engine instance doesn't allow to be used in this java version: " + System.getProperty("java.specification.version") + ". Please use Java11HttpEngine instead this one.");
        }
        this.proxy = proxy;
        this.timeout = timeout;
    }

    @Override
    public TwitchResponse execute(TwitchRequest request) throws IOException {
        URL url = request.getUrl().toURL();
        HttpURLConnection connection = (HttpURLConnection) ((proxy != Proxy.NO_PROXY) ? url.openConnection(proxy) : url.openConnection());
        try {
            connection.setConnectTimeout((int) timeout.toMillis());
            connection.setReadTimeout((int) timeout.toMillis());

            putRequest(request, connection);
            connection.connect();
            return getResponse(request, connection);
        } finally {
            connection.disconnect();
        }
    }

    private void putRequest(TwitchRequest request, HttpURLConnection connection) throws IOException {
        if (request.getMethod() == TwitchRoute.Method.PATCH) {
            // Workaround this issue: https://stackoverflow.com/a/32503192
            connection.setRequestProperty("X-HTTP-Method-Override", "PATCH");
            connection.setRequestMethod("POST");
        } else {
            connection.setRequestMethod(request.getMethod().name());
        }
        request.getHeaders().entries().forEach(e -> connection.addRequestProperty(e.getKey(), e.getValue()));
        if (request.getMethod() != TwitchRoute.Method.GET && request.getBody() != null) {
            connection.setDoOutput(true);
            DataOutputStream output = new DataOutputStream(connection.getOutputStream());
            try {
                output.writeBytes(request.getBody().asText());
                connection.setRequestProperty("Content-Length", Integer.toString(output.size()));
            } finally {
                output.flush();
                output.close();
            }
        }
    }

    private TwitchResponse getResponse(TwitchRequest request, HttpURLConnection connection) throws IOException {
        JsonMapper mapper = new JsonMapper();
        StringBuilder responseBody = new StringBuilder();
        ListValuedMap<String, String> headers = new ArrayListValuedHashMap<>();

        int code = connection.getResponseCode();
        connection.getHeaderFields().entrySet().stream()
            .filter(e -> e.getKey() != null)
            .forEach(e -> headers.putAll(e.getKey(), e.getValue()));

        Reader streamReader = new InputStreamReader((code > 299) ? connection.getErrorStream() : connection.getInputStream());
        JsonNode body = mapper.readTree(streamReader);

        return new TwitchResponse(code, body, MultiMapUtils.unmodifiableMultiValuedMap(headers), request);
    }


    public static class Config extends HttpEngineConfig {
        Config() {
            super();
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
        public Java8HttpEngine install(Config configure) {
            Proxy proxy = (configure.getProxyAddress() != null) ? new Proxy(Proxy.Type.DIRECT, configure.getProxyAddress()) : null;
            return new Java8HttpEngine(proxy, configure.getTimeout());
        }
    }
}
