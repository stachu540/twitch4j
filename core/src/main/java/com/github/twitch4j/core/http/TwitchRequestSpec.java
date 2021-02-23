package com.github.twitch4j.core.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.MultiMapUtils;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public final URI getUrl() {
        return route.buildUri(pathParameters, queryParameters);
    }

    public final TwitchRoute.Method getMethod() {
        return route.getMethod();
    }

    public final void setBody(@Nullable final Object body) {
        if (body == null) {
            this.body = null;
        } else {
            this.body = mapper.valueToTree(body);
        }
    }

    public final void addHeader(final String key, final Iterable<String> values) {
        List<String> currentValues = headers.get(key);
        values.forEach(currentValues::add);
        setHeader(key, currentValues);
    }

    public final void addHeader(final String key, final String... values) {
        addHeader(key, Arrays.asList(values));
    }

    public final void setHeader(final String key, final Iterable<String> values) {
        headers.putAll(key, values);
    }

    public final void setHeader(final String key, final String... values) {
        setHeader(key, Arrays.asList(values));
    }

    public final void addQueryParameter(final String key, final Iterable<String> values) {
        List<String> currentValues = queryParameters.get(key);
        values.forEach(currentValues::add);
        setQueryParameter(key, currentValues);
    }

    public final void addQueryParameter(final String key, final String... values) {
        addQueryParameter(key, Arrays.asList(values));
    }

    public final void setQueryParameter(final String key, final Iterable<String> values) {
        queryParameters.putAll(key, values);
    }

    public final void setQueryParameter(final String key, final String... values) {
        setQueryParameter(key, Arrays.asList(values));
    }

    public final void setPathParameter(final String key, final String value) {
        pathParameters.put(key, value);
    }
}
