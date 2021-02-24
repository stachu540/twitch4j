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
package com.github.twitch4j.core.utils;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.twitch4j.core.http.TwitchRequestSpec;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TwitchUtils {
  private final Map<String, String> DEFAULT_HEADERS = new LinkedHashMap<>();

  {
    DEFAULT_HEADERS.put("User-Agent", "Twitch4J v2 (https://twitch4j.github.io)");
    DEFAULT_HEADERS.put("Content-Type", "application/json");
    DEFAULT_HEADERS.put("Accept", "application/json");
  }

  public JsonMapper defaultMapper() {
    return JsonMapper.builder().propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
      .findAndAddModules().build();
  }

  public Consumer<TwitchRequestSpec> defaultRequest() {
    return spec -> DEFAULT_HEADERS.forEach(spec::addHeader);
  }

  public String stripUrl(final URI url) {
    StringBuilder sb = new StringBuilder();
    sb.append(url.getScheme()).append("://")
      .append(url.getHost());
    if (url.getPort() > 0) {
      sb.append(url.getPort());
    }
    sb.append(url.getPath());

    return sb.toString();
  }
}
