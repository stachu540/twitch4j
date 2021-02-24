/*
 * MIT License
 *
 * Copyright (c) 2021 Philipp Heuer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.twitch4j.core.internal.http.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.twitch4j.core.http.TwitchRequest;
import com.github.twitch4j.core.http.TwitchResponse;
import com.github.twitch4j.core.http.engine.HttpEngine;
import com.github.twitch4j.core.http.engine.HttpEngineConfig;
import com.github.twitch4j.core.http.engine.HttpEngineFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Consumer;
import lombok.Getter;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

public final class Java11HttpEngine implements HttpEngine {

  private final HttpClient realClient;

  Java11HttpEngine(final HttpClient client) {
    if (Integer.parseInt(System.getProperty("java.specification.version").split("\\.")[0]) < 9) {
      throw new RuntimeException(
        "Current engine instance doesn't allow to be used in this java version: "
          + System.getProperty("java.specification.version")
          + ". Make sure if you are using JVM 9+"
      );
    }
    this.realClient = client;
  }

  @Override
  public TwitchResponse execute(final TwitchRequest request) throws Exception {
    return convertToResponse(
      request, realClient.send(convertToRequest(request), HttpResponse.BodyHandlers.ofInputStream())
    );
  }

  private HttpRequest convertToRequest(final TwitchRequest synthetic) {
    HttpRequest.Builder builder = HttpRequest.newBuilder(synthetic.getUrl());
    builder.method(synthetic.getMethod().toString(),
      (synthetic.getBody() != null)
        ? HttpRequest.BodyPublishers.ofString(synthetic.getBody().asText())
        : HttpRequest.BodyPublishers.noBody()
    );
    synthetic.getHeaders().entries().forEach(e -> builder.header(e.getKey(), e.getValue()));

    return builder.build();
  }

  private TwitchResponse convertToResponse(
    final TwitchRequest request, final HttpResponse<InputStream> response
  ) throws IOException {
    int code = response.statusCode();
    ListValuedMap<String, String> headers = new ArrayListValuedHashMap<>();
    response.headers().map().forEach(headers::putAll);
    JsonNode body = (response.body().available() > 0) ? new JsonMapper().readTree(response.body()) : null;

    return new TwitchResponse(code, body, headers, request);
  }

  public static final class Config extends HttpEngineConfig {
    @Getter
    private Consumer<HttpClient.Builder> builder = ignore -> {
    };

    Config() {
      super();
    }

    public void configureBuilder(Consumer<HttpClient.Builder> builder) {
      this.builder = this.builder.andThen(builder);
    }
  }

  public static final class Factory implements HttpEngineFactory<Config> {
    @Override
    public Config configure(final Consumer<Config> configure) {
      Config config = new Config();
      configure.accept(config);
      return config;
    }

    @Override
    public Java11HttpEngine install(final Config configure) {
      HttpClient.Builder builder = HttpClient.newBuilder()
        .connectTimeout(configure.getTimeout());
      if (configure.getProxy() != Proxy.NO_PROXY) {
        builder.proxy(ProxySelector.of((InetSocketAddress) configure.getProxy().address()));
      }

      configure.getBuilder().accept(builder);

      return new Java11HttpEngine(builder.build());
    }
  }
}
