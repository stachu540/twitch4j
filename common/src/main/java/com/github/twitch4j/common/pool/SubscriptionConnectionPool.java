package com.github.twitch4j.common.pool;

import lombok.Builder;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A pool of connections for making subscriptions (and potentially unsubscribing from later).
 *
 * @param <C> the connection type
 * @param <S> the subscription request
 * @param <T> transactional subscription response and potential unsubscription request
 * @param <U> the unsubscription response
 */
@SuperBuilder
public abstract class SubscriptionConnectionPool<C, S, T, U> extends AbstractConnectionPool<C> implements TransactionalSubscriber<S, T, U> {

    /**
     * Whether connections without subscriptions should be disposed of. Default: true.
     * <p>
     * As an optimization, this can be set to false to not dispose of connections in an environment where
     * it is known that a large burst of unsubscriptions will be followed by enough subscriptions such that
     * connections will never need to be disposed of automatically. However, be wary of configuring this as
     * a misstep in your calculations may lead to connections sitting idly while consuming resources.
     */
    @Builder.Default
    protected final boolean disposeUnusedConnections = true;

    /**
     * The maximum number of outstanding subscriptions a connection may have. Default: 50.
     * <p>
     * 50 is a reasonable default given that it is the standard limit imposed on PubSub connections by Twitch
     * and it is the previously-documented rate-limit on the number of possible JOINs in chat in a short burst.
     *
     * @see <a href="https://dev.twitch.tv/docs/pubsub#api-limits">PubSub Limits</a>
     * @see <a href="https://web.archive.org/web/20181204131212/https://help.twitch.tv/customer/portal/articles/1302780-twitch-irc">Previous Chat Limits</a>
     */
    @Builder.Default
    protected final int maxSubscriptionsPerConnection = 50; // Defining this default here is not ideal, but it is not easy to redefine it in a subclass due to SuperBuilder

    /**
     * Connections that are already at the maximum subscriptions.
     */
    protected final Set<C> saturatedConnections = ConcurrentHashMap.newKeySet();

    /**
     * A mapping of connections that are not saturated yet to the number of subscriptions they have.
     */
    protected final ConcurrentMap<C, Integer> unsaturatedConnections = new ConcurrentHashMap<>();

    /**
     * A mapping of which connections hold what subscriptions.
     */
    protected final ConcurrentMap<S, C> subscriptions = new ConcurrentHashMap<>();

    @Override
    public T subscribe(S s) {
        final C connection = getOrCreateConnectionWithHeadroomAndIncrement();
        if (connection != null && subscriptions.putIfAbsent(s, connection) != null) {
            decrementSubscriptions(connection);
            return handleDuplicateSubscription(connection, s);
        }
        return handleSubscription(connection, s);
    }

    @Override
    public U unsubscribe(T t) {
        final C connection = subscriptions.remove(getRequestFromSubscription(t));
        if (connection != null)
            decrementSubscriptions(connection);
        return handleUnsubscription(connection, t);
    }

    @Override
    public int numConnections() {
        return saturatedConnections.size() + unsaturatedConnections.size();
    }

    @Override
    protected Iterable<C> getConnections() {
        final Collection<C> connections = new ArrayList<>(numConnections());
        connections.addAll(saturatedConnections);
        connections.addAll(unsaturatedConnections.keySet());
        return Collections.unmodifiableCollection(connections);
    }

    /**
     * @return the total number of subscriptions held by all connections
     */
    public int numSubscriptions() {
        int n = 0;

        n += saturatedConnections.size() * maxSubscriptionsPerConnection;

        for (Integer i : unsaturatedConnections.values()) {
            n += i;
        }

        return n;
    }

    protected abstract T handleSubscription(C c, S s);

    protected abstract T handleDuplicateSubscription(C c, S s);

    protected abstract U handleUnsubscription(C c, T t);

    protected abstract S getRequestFromSubscription(T t);

    private C getOrCreateConnectionWithHeadroomAndIncrement() {
        final int max = this.maxSubscriptionsPerConnection;

        // Attempt to find an existing unsaturated connection
        // Implementation Notes:
        // With this loop, there is no guarantee regarding entry order over time.
        // Without unsubscriptions, this algorithm prefers to keep a minimal amount of unsaturated connections (one, if possible without locking)
        // and fill them completely before moving onto another unsaturated connection.
        // With unsubscriptions, the behavior is not as deterministic as the elements are not constantly reordered by to subscription count.
        // Lastly, if multiple threads attempt to make a subscription at the same time and there are no existing unsaturated connections,
        // this code may create a new connection for each of these threads, due to the lock-free approach. Synchronization would avoid this.
        for (C connection : unsaturatedConnections.keySet()) {
            // Try to increment this connection atomically
            final Integer computed = unsaturatedConnections.compute(connection, (c, n) -> {
                if (n == null || n + 1 > max)
                    return null;

                return n + 1;
            });

            if (computed == null)
                continue; // did not have headroom

            // Check if the connection has further headroom or needs to be marked as saturated
            if (computed >= max) {
                unsaturatedConnections.remove(connection);
                saturatedConnections.add(connection);
            }

            return connection; // found a sufficient existing connection!
        }

        // Fallback to creating a new connection (and incrementing that)
        final C c = createConnection();
        if (c != null) {
            // noinspection ConstantConditions (SuperBuilder related)
            if (1 < max)
                unsaturatedConnections.putIfAbsent(c, 1);
            else
                saturatedConnections.add(c);
        }
        return c;
    }

    private void decrementSubscriptions(C connection) {
        // Can no longer be saturated
        saturatedConnections.remove(connection);

        // Decrement subscriptions atomically
        Integer newSubs = unsaturatedConnections.compute(connection, (c, n) -> {
            final int prev = n != null ? n : maxSubscriptionsPerConnection;
            final int next = prev - 1;
            if (next <= 0 && this.disposeUnusedConnections) {
                return null; // remove
            }
            return next;
        });

        // Dispose if needed
        if (newSubs == null)
            disposeConnection(connection);
    }

}
