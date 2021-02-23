package com.github.twitch4j.core.http.engine.okhttp;

import com.github.twitch4j.core.http.engine.HttpEngineConfig;
import lombok.Getter;
import okhttp3.OkHttpClient;

import java.util.function.Consumer;


public class OkHttpEngineConfig extends HttpEngineConfig {

    @Getter
    private Consumer<OkHttpClient.Builder> builder = builder -> {
    };

    OkHttpEngineConfig() {
        super();
    }

    public void configureBuilder(Consumer<OkHttpClient.Builder> builder) {
        this.builder = this.builder.andThen(builder);
    }
}
