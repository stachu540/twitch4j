package com.github.twitch4j.core.dao;

import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

@Value
public class Data<T> implements Iterable<T> {
    List<T> data;

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return data.iterator();
    }
}
