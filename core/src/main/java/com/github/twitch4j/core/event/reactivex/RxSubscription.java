package com.github.twitch4j.core.event.reactivex;

import com.github.twitch4j.core.event.Event;
import com.github.twitch4j.core.event.EventDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class RxSubscription<E extends Event> implements Subscriber<E>, EventDisposable, Disposable {
    private final Consumer<E> consumer;
    private final AtomicBoolean disposed = new AtomicBoolean(false);
    private Subscription subscription;

    @Override
    public void dispose() {
        if (!disposed.get()) {
            subscription.cancel();
            disposed.set(true);
            subscription = null;
        }
    }

    @Override
    public boolean isDisposed() {
        return disposed.get();
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        if (!isDisposed()) {
            this.subscription = subscription;
            subscription.request(1);
        }
    }

    @Override
    public void onNext(E e) {
        if (!isDisposed()) {
            consumer.accept(e);
            subscription.request(1);
        }
    }

    @Override
    public void onError(Throwable t) {

    }

    @Override
    public void onComplete() {

    }
}
