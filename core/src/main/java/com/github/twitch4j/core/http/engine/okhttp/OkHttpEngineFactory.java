package com.github.twitch4j.core.http.engine.okhttp;

import com.github.twitch4j.core.http.engine.HttpEngineFactory;
import okhttp3.OkHttpClient;

import java.net.Proxy;
import java.util.function.Consumer;

public class OkHttpEngineFactory implements HttpEngineFactory<OkHttpEngineConfig, OkHttpEngine> {

    @Override
    public OkHttpEngineConfig configure(Consumer<OkHttpEngineConfig> configure) {
        OkHttpEngineConfig config = new OkHttpEngineConfig();
        configure.accept(config);

        return config;
    }

    @Override
    public OkHttpEngine install(OkHttpEngineConfig config) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        if (config.getProxyAddress() != Proxy.NO_PROXY) {
            builder.proxy(config.getProxyAddress());
        }

        builder.connectTimeout(config.getTimeout())
            .callTimeout(config.getTimeout())
            .writeTimeout(config.getTimeout())
            .readTimeout(config.getTimeout());

        config.getBuilder().accept(builder);

        return new OkHttpEngine(builder.build());
    }
}
