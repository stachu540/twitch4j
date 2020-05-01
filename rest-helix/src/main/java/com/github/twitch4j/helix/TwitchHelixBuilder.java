package com.github.twitch4j.helix;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.twitch4j.common.builder.TwitchAPIBuilder;
import com.github.twitch4j.helix.interceptor.TwitchHelixClientIdInterceptor;
import com.netflix.config.ConfigurationManager;
import feign.Logger;
import feign.Request;
import feign.Retryer;
import feign.hystrix.HystrixFeign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

/**
 * Twitch API - Helix
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class TwitchHelixBuilder extends TwitchAPIBuilder<TwitchHelixBuilder> {

    /**
     * BaseUrl
     */
    private String baseUrl = "https://api.twitch.tv/helix";

    /**
     * Default Timeout
     */
    @With
    private Integer timeout = 5000;

    /**
     * Initialize the builder
     *
     * @return Twitch Helix Builder
     */
    public static TwitchHelixBuilder builder() {
        return new TwitchHelixBuilder();
    }

    /**
     * Twitch API Client (Helix)
     *
     * @return TwitchHelix
     */
    public TwitchHelix build() {
        log.debug("Helix: Initializing Module ...");

        // Hystrix
        ConfigurationManager.getConfigInstance().setProperty("hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds", timeout);
        ConfigurationManager.getConfigInstance().setProperty("hystrix.command.default.requestCache.enabled", false);
        ConfigurationManager.getConfigInstance().setProperty("hystrix.threadpool.default.maxQueueSize", getRequestQueueSize());
        ConfigurationManager.getConfigInstance().setProperty("hystrix.threadpool.default.queueSizeRejectionThreshold", getRequestQueueSize());

        // Jackson ObjectMapper
        ObjectMapper mapper = new ObjectMapper();
        // - Modules
        mapper.findAndRegisterModules();

        // Feign
        TwitchHelix client = HystrixFeign.builder()
            .client(new OkHttpClient())
            .encoder(new JacksonEncoder(mapper))
            .decoder(new JacksonDecoder(mapper))
            .logger(new Logger.ErrorLogger())
            .errorDecoder(new TwitchHelixErrorDecoder(new JacksonDecoder()))
            .requestInterceptor(new TwitchHelixClientIdInterceptor(this))
            .options(new Request.Options(timeout / 3, timeout))
            .retryer(new Retryer.Default(500, timeout, 2))
            .target(TwitchHelix.class, baseUrl);

        return client;
    }
}
