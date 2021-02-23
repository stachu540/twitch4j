package com.github.twitch4j.auth;

import com.github.twitch4j.auth.dao.ApplicationCredential;
import com.github.twitch4j.auth.dao.Credential;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public interface TokenCache {
    AtomicReference<ApplicationCredential> getApplication();

    Optional<Credential> get(long id);

    Optional<Credential> get(String login);

    Collection<Credential> find(Predicate<Credential> filter);

    AtomicBoolean remove(long id);

    AtomicBoolean remove(String login);

    AtomicBoolean removeIf(Predicate<Credential> filter);

    void add(Credential credential);
}
