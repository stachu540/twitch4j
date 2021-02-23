package com.github.twitch4j.core.http.engine.okhttp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.twitch4j.core.http.TwitchRequest;
import com.github.twitch4j.core.http.TwitchResponse;
import com.github.twitch4j.core.http.TwitchRoute;
import com.github.twitch4j.core.http.engine.HttpEngine;
import com.github.twitch4j.core.socket.SocketDispatcher;
import com.github.twitch4j.core.socket.TwitchSocket;
import com.github.twitch4j.core.socket.OkHttpWebSocket;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.apache.commons.collections4.ListValuedMap;
import org.apache.commons.collections4.MultiMapUtils;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@RequiredArgsConstructor
public class OkHttpEngine implements HttpEngine {
    private final OkHttpClient realClient;

    @Override
    public final TwitchResponse execute(TwitchRequest request) throws IOException {
        return convertToResponse(request, getCall(request).execute());
    }

    private Call getCall(TwitchRequest request) {
        return realClient.newCall(convertToRequest(request));
    }

    private Request convertToRequest(TwitchRequest synthetic) {
        Request.Builder request = new Request.Builder();
        request.url(Objects.requireNonNull(HttpUrl.get(synthetic.getUrl())));
        RequestBody body = RequestBody.create((synthetic.getBody() == null || synthetic.getBody().isNull()) ? new byte[0] : synthetic.getBody().asText().getBytes(StandardCharsets.UTF_8), MediaType.get("application/json"));
        request.method(synthetic.getMethod().name(), (synthetic.getMethod() == TwitchRoute.Method.GET || (synthetic.getMethod() == TwitchRoute.Method.DELETE && synthetic.getBody() == null)) ? null : body);
        synthetic.getHeaders().entries().forEach(e -> request.addHeader(e.getKey(), e.getValue()));

        return request.build();
    }

    private TwitchResponse convertToResponse(TwitchRequest request, Response response) throws IOException {
        int code = response.code();
        ListValuedMap<String, String> headers = new ArrayListValuedHashMap<>();
        response.headers().toMultimap().forEach(headers::putAll);
        ResponseBody rb = response.body();
        JsonNode body = (rb != null) ? new JsonMapper().readTree(rb.charStream()) : null;

        return new TwitchResponse(code, body, MultiMapUtils.unmodifiableMultiValuedMap(headers), request);
    }


}
