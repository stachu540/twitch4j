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
import com.github.twitch4j.core.http.TwitchRoute;
import com.github.twitch4j.core.http.engine.HttpEngine;
import com.github.twitch4j.core.http.engine.HttpEngineConfig;
import com.github.twitch4j.core.http.engine.HttpEngineFactory;
import java.io.IOException;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.MultiMapUtils;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

@RequiredArgsConstructor
public final class OkHttpEngine implements HttpEngine {
  private final OkHttpClient realClient;

  @Override
  public TwitchResponse execute(final TwitchRequest request) throws IOException {
    return convertToResponse(request, getCall(request).execute());
  }

  private Call getCall(final TwitchRequest request) {
    return realClient.newCall(convertToRequest(request));
  }

  private Request convertToRequest(final TwitchRequest synthetic) {
    Request.Builder request = new Request.Builder();
    request.url(Objects.requireNonNull(HttpUrl.get(synthetic.getUrl())));
    RequestBody body = RequestBody.create(
      (synthetic.getBody() == null || synthetic.getBody().isNull()) ? new byte[0]
        : synthetic.getBody().asText().getBytes(StandardCharsets.UTF_8), MediaType.get("application/json")
    );
    request.method(synthetic.getMethod().name(),
      (
        synthetic.getMethod() == TwitchRoute.Method.GET
          || (synthetic.getMethod() == TwitchRoute.Method.DELETE && synthetic.getBody() == null)
      ) ? null : body
    );
    synthetic.getHeaders().entries().forEach(e -> request.addHeader(e.getKey(), e.getValue()));

    return request.build();
  }

  private TwitchResponse convertToResponse(
    final TwitchRequest request, final Response response
  ) throws IOException {
    int code = response.code();
    ListValuedMap<String, String> headers = new ArrayListValuedHashMap<>();
    response.headers().toMultimap().forEach(headers::putAll);
    ResponseBody rb = response.body();
    JsonNode body = (rb != null) ? new JsonMapper().readTree(rb.charStream()) : null;

    return new TwitchResponse(code, body, MultiMapUtils.unmodifiableMultiValuedMap(headers), request);
  }


  public static final class Config extends HttpEngineConfig {

    @Getter
    private Consumer<OkHttpClient.Builder> builder = ignore -> {
    };

    private Config() {
      super();
    }

    public void configureBuilder(final Consumer<OkHttpClient.Builder> builder) {
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
    public OkHttpEngine install(final Config config) {
      OkHttpClient.Builder builder = new OkHttpClient.Builder();

      if (config.getProxy() != Proxy.NO_PROXY) {
        builder.proxy(config.getProxy());
      }

      builder.connectTimeout(config.getTimeout())
        .callTimeout(config.getTimeout())
        .writeTimeout(config.getTimeout())
        .readTimeout(config.getTimeout());

      config.getBuilder().accept(builder);

      return new OkHttpEngine(builder.build());
    }
  }
}
