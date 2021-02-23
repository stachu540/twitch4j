package com.github.twitch4j.core.event.reactivex;

import com.github.twitch4j.core.event.Event;
import com.github.twitch4j.core.event.EventDisposable;
import com.github.twitch4j.core.event.EventHandler;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;

import java.util.function.Consumer;

public class RxEventHandler implements EventHandler {

    private final PublishSubject<Event> publishSubject = PublishSubject.create();
    private final Scheduler schedulerObserver;
    private final Scheduler schedulerSubscriber;
    private final BackpressureStrategy backpressureStrategy;
    private final int bufferSize;
//    private final int maxThreads = Math.max(Runtime.getRuntime().availableProcessors(), 4);

    public RxEventHandler() {
        schedulerObserver = Schedulers.computation();
        schedulerSubscriber = Schedulers.newThread();
        backpressureStrategy = BackpressureStrategy.BUFFER;
        bufferSize = 8192;
    }

    public RxEventHandler(Scheduler schedulerObserver, Scheduler schedulerSubscriber, BackpressureStrategy backpressureStrategy, int bufferSize) {
        this.schedulerObserver = schedulerObserver;
        this.schedulerSubscriber = schedulerSubscriber;
        this.backpressureStrategy = backpressureStrategy;
        this.bufferSize = bufferSize;
    }

    @Override
    public void handle(Event event) {
        publishSubject.onNext(event);
    }

    @Override
    public <E extends Event> EventDisposable onEvent(Class<E> type, Consumer<E> event) {
        Flowable<E> observable = publishSubject
            .toFlowable(backpressureStrategy)
            .observeOn(schedulerObserver, false, bufferSize)
            .subscribeOn(schedulerSubscriber)
            .ofType(type);

        RxSubscription<E> subscription = new RxSubscription<>(event);

        observable.subscribe(subscription);

        return subscription;
    }

    @Override
    public void close() throws Exception {

    }
}
