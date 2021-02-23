package com.github.twitch4j.core.http;

import java.net.URI;
import java.net.URL;
import java.util.regex.Pattern;

public final class TwitchRouter {
    public static final TwitchRouter OAUTH = new TwitchRouter("https://id.twitch.tv/oauth2/");
    public static final TwitchRouter API = new TwitchRouter("https://api.twitch.tv/helix");
    public static final TwitchRouter TMI = new TwitchRouter("https://tmi.twitch.tv/");

    public static final TwitchRouter EVENTSUB = API.append("/eventsub/subscriptions");
    public static final TwitchRouter WEBSUB = API.append("/webhooks/hub");

    private static final Pattern URL_MATCHES = Pattern.compile("^http[s]?://([a-z0-9]+\\.)+[a-z0-9]+(/.+)?", Pattern.CASE_INSENSITIVE);

    final String baseUrl;

    private TwitchRouter(URL baseUrl) {
        this(baseUrl.toString());
    }

    private TwitchRouter(URI baseUrl) {
        this(baseUrl.toString());
    }

    private TwitchRouter(String baseUrl) {
        if (!URL_MATCHES.matcher(baseUrl).matches()) {
            throw new IllegalArgumentException("Current base url is not allowed.");
        }
        this.baseUrl = baseUrl.toLowerCase();
    }

    public static TwitchRouter custom(String baseUrl) {
        return new TwitchRouter(baseUrl);
    }

    public static TwitchRouter custom(URL baseUrl) {
        return new TwitchRouter(baseUrl);
    }

    public static TwitchRouter custom(URI baseUrl) {
        return new TwitchRouter(baseUrl);
    }

    private TwitchRoute create(TwitchRoute.Method method, String endpoint) {
        return new TwitchRoute(this, method, endpoint);
    }

    public final TwitchRoute get(String endpoint) {
        return create(TwitchRoute.Method.GET, endpoint);
    }

    public final TwitchRoute post(String endpoint) {
        return create(TwitchRoute.Method.POST, endpoint);
    }

    public final TwitchRoute put(String endpoint) {
        return create(TwitchRoute.Method.PUT, endpoint);
    }

    public final TwitchRoute patch(String endpoint) {
        return create(TwitchRoute.Method.PATCH, endpoint);
    }

    public final TwitchRoute delete(String endpoint) {
        return create(TwitchRoute.Method.DELETE, endpoint);
    }

    public final TwitchRoute options(String endpoint) {
        return create(TwitchRoute.Method.OPTIONS, endpoint);
    }

    private TwitchRouter append(String endpoint) {
        return new TwitchRouter(URI.create(baseUrl + endpoint));
    }
}
