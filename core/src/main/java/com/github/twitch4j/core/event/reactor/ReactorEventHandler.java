package com.github.twitch4j.core.event.reactor;

import com.github.twitch4j.core.event.Event;
import com.github.twitch4j.core.event.EventDisposable;
import com.github.twitch4j.core.event.EventHandler;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Scheduler;
import reactor.scheduler.forkjoin.ForkJoinPoolScheduler;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class ReactorEventHandler implements EventHandler {

    private final Sinks.Many<Event> sinks;
    private final Scheduler scheduler;

    public ReactorEventHandler() {
        sinks = Sinks.many().multicast().onBackpressureBuffer(8192, true);
        scheduler = ForkJoinPoolScheduler.create("twitch4j-events", Math.max(Runtime.getRuntime().availableProcessors(), 4));
    }

    @Override
    public void handle(Event event) {
        sinks.emitNext(event, Sinks.EmitFailureHandler.FAIL_FAST);
    }

    @Override
    public <E extends Event> EventDisposable onEvent(Class<E> type, Consumer<E> event) {
        Flux<E> flux = sinks.asFlux()
            .publishOn(scheduler)
            .ofType(type)
            .limitRequest(15);

        ReactorSubscription<E> subscription = new ReactorSubscription<>(event);
        flux.subscribe(subscription);

        return subscription;
    }

    @Override
    public void close() throws Exception {
        // complete sink
        sinks.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);

        // delayed
        Thread.sleep(1000);

        // dispose scheduler
        if (!scheduler.isDisposed()) {
            scheduler.dispose();
        }
    }
}
