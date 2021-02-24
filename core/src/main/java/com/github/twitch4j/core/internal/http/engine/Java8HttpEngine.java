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
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.time.Duration;
import java.util.function.Consumer;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.MultiMapUtils;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

public final class Java8HttpEngine implements HttpEngine {
  private final Proxy proxy;
  private final Duration timeout;

  Java8HttpEngine(final Proxy proxy, final Duration timeout) {
    if (Integer.parseInt(System.getProperty("java.specification.version").split("\\.")[0]) >= 9) {
      throw new RuntimeException("Current engine instance doesn't allow to be used in this java version: "
        + System.getProperty("java.specification.version")
        + ". Please use Java11HttpEngine instead this one."
      );
    }
    this.proxy = proxy;
    this.timeout = timeout;
  }

  @Override
  public TwitchResponse execute(final TwitchRequest request) throws IOException {
    URL url = request.getUrl().toURL();
    HttpURLConnection connection = (HttpURLConnection) ((proxy != Proxy.NO_PROXY)
      ? url.openConnection(proxy) : url.openConnection()
    );
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

  private void putRequest(final TwitchRequest request, final HttpURLConnection connection) throws IOException {
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

  private TwitchResponse getResponse(
    final TwitchRequest request, final HttpURLConnection connection
  ) throws IOException {
    JsonMapper mapper = new JsonMapper();
    ListValuedMap<String, String> headers = new ArrayListValuedHashMap<>();

    int code = connection.getResponseCode();
    connection.getHeaderFields().entrySet().stream()
      .filter(e -> e.getKey() != null)
      .forEach(e -> headers.putAll(e.getKey(), e.getValue()));

    Reader streamReader = new InputStreamReader(
      (code > 299) ? connection.getErrorStream() : connection.getInputStream()
    );
    JsonNode body = mapper.readTree(streamReader);

    return new TwitchResponse(code, body, MultiMapUtils.unmodifiableMultiValuedMap(headers), request);
  }


  public static final class Config extends HttpEngineConfig {
    Config() {
      super();
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
    public Java8HttpEngine install(final Config configure) {
      return new Java8HttpEngine(configure.getProxy(), configure.getTimeout());
    }
  }
}
