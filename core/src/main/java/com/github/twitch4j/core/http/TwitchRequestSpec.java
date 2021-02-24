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
package com.github.twitch4j.core.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.MultiMapUtils;
import org.jetbrains.annotations.Nullable;

@Getter
@RequiredArgsConstructor
public final class TwitchRequestSpec {
  private final TwitchRoute route;
  private final JsonMapper mapper;
  private final ListValuedMap<String, String> headers = MultiMapUtils.newListValuedHashMap();
  private final ListValuedMap<String, String> queryParameters = MultiMapUtils.newListValuedHashMap();
  private final Map<String, String> pathParameters = new LinkedHashMap<>();
  @Nullable
  private JsonNode body = null;

  public URI getUrl() {
    return route.buildUri(pathParameters, queryParameters);
  }

  public TwitchRoute.Method getMethod() {
    return route.getMethod();
  }

  public void setBody(@Nullable final Object body) {
    if (body == null) {
      this.body = null;
    } else {
      this.body = mapper.valueToTree(body);
    }
  }

  public void addHeader(final String key, final Iterable<String> values) {
    List<String> currentValues = headers.get(key);
    values.forEach(currentValues::add);
    setHeader(key, currentValues);
  }

  public void addHeader(final String key, final String... values) {
    addHeader(key, Arrays.asList(values));
  }

  public void setHeader(final String key, final Iterable<String> values) {
    headers.putAll(key, values);
  }

  public void setHeader(final String key, final String... values) {
    setHeader(key, Arrays.asList(values));
  }

  public void addQueryParameter(final String key, final Iterable<String> values) {
    List<String> currentValues = queryParameters.get(key);
    values.forEach(currentValues::add);
    setQueryParameter(key, currentValues);
  }

  public void addQueryParameter(final String key, final String... values) {
    addQueryParameter(key, Arrays.asList(values));
  }

  public void setQueryParameter(final String key, final Iterable<String> values) {
    queryParameters.putAll(key, values);
  }

  public void setQueryParameter(final String key, final String... values) {
    setQueryParameter(key, Arrays.asList(values));
  }

  public void setPathParameter(final String key, final String value) {
    pathParameters.put(key, value);
  }
}
