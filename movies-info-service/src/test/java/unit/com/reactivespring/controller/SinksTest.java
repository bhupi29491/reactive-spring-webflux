package com.reactivespring.controller;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

public class SinksTest {

    @Test
    public void sink() {

        // given
        Sinks.Many<Integer> replaySink = Sinks.many()
                                              .replay()
                                              .all();

        // when
        replaySink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        replaySink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        // then
        Flux<Integer> integerFlux = replaySink.asFlux();
        integerFlux.subscribe(i -> {
            System.out.println("Subscriber 1 : " + i);
        });

        Flux<Integer> integerFlux1 = replaySink.asFlux();
        integerFlux1.subscribe(i -> {
            System.out.println("Subscriber 2 : " + i);
        });

        replaySink.tryEmitNext(3);

        Flux<Integer> integerFlux2 = replaySink.asFlux();
        integerFlux2.subscribe(i -> {
            System.out.println("Subscriber 3 : " + i);
        });

    }

    @Test
    public void sinks_Multicast() {

        // given
        Sinks.Many<Integer> multiCast = Sinks.many()
                                             .multicast()
                                             .onBackpressureBuffer();

        // when
        multiCast.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        multiCast.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        // then
        Flux<Integer> integerFlux = multiCast.asFlux();
        integerFlux.subscribe(i -> {
            System.out.println("Subscriber 1 : " + i);
        });

        Flux<Integer> integerFlux1 = multiCast.asFlux();
        integerFlux1.subscribe(i -> {
            System.out.println("Subscriber 2 : " + i);
        });

        multiCast.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST);

    }


    @Test
    public void sinks_UniCast() {

        // given
        Sinks.Many<Integer> uniCast = Sinks.many()
                                           .unicast()
                                           .onBackpressureBuffer();

        // when
        uniCast.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        uniCast.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        // then
        Flux<Integer> integerFlux = uniCast.asFlux();
        integerFlux.subscribe(i -> {
            System.out.println("Subscriber 1 : " + i);
        });

        Flux<Integer> integerFlux1 = uniCast.asFlux();
        integerFlux1.subscribe(i -> {
            System.out.println("Subscriber 2 : " + i);
        });

        uniCast.emitNext(3, Sinks.EmitFailureHandler.FAIL_FAST);

    }


}
