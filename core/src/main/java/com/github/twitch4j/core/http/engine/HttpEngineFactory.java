package com.github.twitch4j.core.http.engine;

import java.util.function.Consumer;

public interface HttpEngineFactory<C extends HttpEngineConfig> {
    C configure(Consumer<C> configure);

    HttpEngine install(C configure);
}
