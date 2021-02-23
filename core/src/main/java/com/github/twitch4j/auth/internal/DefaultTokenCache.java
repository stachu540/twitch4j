package com.github.twitch4j.auth.internal;

import com.github.twitch4j.auth.TokenCache;
import com.github.twitch4j.auth.dao.ApplicationCredential;
import com.github.twitch4j.auth.dao.Credential;
import lombok.Getter;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

@Getter
public class DefaultTokenCache implements TokenCache {
    private final AtomicReference<ApplicationCredential> application = new AtomicReference<>();
    private final Set<Credential> credentials = new LinkedHashSet<>();

    @Override
    public Optional<Credential> get(long id) {
        return credentials.stream().filter(c -> c.getId() == id).findFirst();
    }

    @Override
    public Optional<Credential> get(String login) {
        return Optional.empty();
    }

    @Override
    public Collection<Credential> find(Predicate<Credential> filter) {
        return null;
    }

    @Override
    public AtomicBoolean remove(long id) {
        return null;
    }

    @Override
    public AtomicBoolean remove(String login) {
        return null;
    }

    @Override
    public AtomicBoolean removeIf(Predicate<Credential> filter) {
        return null;
    }

    @Override
    public void add(Credential credential) {

    }
}
