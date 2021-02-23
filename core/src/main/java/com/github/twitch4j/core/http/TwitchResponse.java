package com.github.twitch4j.core.http;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Value;
import org.apache.commons.collections4.MultiValuedMap;
import org.jetbrains.annotations.Nullable;

@Value
public class TwitchResponse {
    int statusCode;
    @Nullable JsonNode body;
    MultiValuedMap<String, String> headers;
    TwitchRequest request;
}
