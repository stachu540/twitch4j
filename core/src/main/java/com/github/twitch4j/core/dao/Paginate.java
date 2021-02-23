package com.github.twitch4j.core.dao;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

@Value
public class Paginate<T> implements Iterable<T> {
    @JsonAlias({"pagination.cursor"})
    String cursor;
    List<T> data;

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return data.iterator();
    }
}
