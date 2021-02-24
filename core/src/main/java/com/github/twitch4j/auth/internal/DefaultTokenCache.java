/*
 * MIT License
 *
 * Copyright (c) 2021 Philipp Heuer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.twitch4j.auth.internal;

import com.github.twitch4j.auth.TokenCache;
import com.github.twitch4j.auth.dao.ApplicationCredential;
import com.github.twitch4j.auth.dao.Credential;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import lombok.Getter;

@Getter
public class DefaultTokenCache implements TokenCache {
  private final AtomicReference<ApplicationCredential> application = new AtomicReference<>();
  private final Set<Credential> credentials = new LinkedHashSet<>();

  @Override
  public final Optional<Credential> get(final long id) {
    return credentials.stream().filter(c -> c.getId() == id).findFirst();
  }

  @Override
  public final Optional<Credential> get(final String login) {
    return Optional.empty();
  }

  @Override
  public final Collection<Credential> find(final Predicate<Credential> filter) {
    return null;
  }

  @Override
  public final AtomicBoolean remove(final long id) {
    return null;
  }

  @Override
  public final AtomicBoolean remove(final String login) {
    return null;
  }

  @Override
  public final AtomicBoolean removeIf(final Predicate<Credential> filter) {
    return null;
  }

  @Override
  public final void add(final Credential credential) {

  }
}
