package com.github.twitch4j.core.http;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Value;
import org.apache.commons.collections4.MultiValuedMap;
import org.jetbrains.annotations.Nullable;

import java.net.URI;

@Value
public class TwitchRequest {
    URI url;
    TwitchRoute.Method method;
    MultiValuedMap<String, String> headers;
    @Nullable JsonNode body;
}
