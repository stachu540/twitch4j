package com.github.twitch4j.core.utils;

import com.github.twitch4j.core.IterableRestCommand;
import com.github.twitch4j.core.PaginatedRestCommand;
import com.github.twitch4j.core.RestCommand;
import com.github.twitch4j.core.dao.Data;
import com.github.twitch4j.core.dao.Paginate;

public class TwitchHttpUtils {
    public static <T> IterableRestCommand<T> convertToIterable(RestCommand<Data<T>> command) {
        return null;
    }

    public static <T> PaginatedRestCommand<T> convertToPaginate(RestCommand<Paginate<T>> command) {
        return null;
    }
}
