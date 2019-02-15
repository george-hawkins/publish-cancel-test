package com.example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.stream.Stream;

class PublishCancelTest {

    private void test(Flux<Integer> flux) {
        StepVerifier.create(flux)
                .thenConsumeWhile(i -> {
                    boolean r = i < 10;
                    System.err.println(r);
                    return r;
                })
                .thenCancel()
                .verify();
    }

    private Flux<Integer> createStreamFlux() {
        return  Flux.fromStream(Stream.iterate(0, i -> i + 1));
    }

    @Test
    void testFinishes() {
        Flux<Integer> flux = createStreamFlux();

        test(flux);
    }

    @Test
    void testNeverFinishes() {
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Flux<Integer> flux = createStreamFlux().publish().autoConnect();

            test(flux.publish().autoConnect());
        });
    }

    private Flux<Integer> createReplayFlux() {
        int count = 100;
        ReplayProcessor<Integer> flux = ReplayProcessor.create(count);

        for (int i = 0; i < count; i++) flux.onNext(i);

        return flux;
    }

    @Test
    void testReplayProcessorFinishes() {
        Flux<Integer> flux = createReplayFlux();

        test(flux);
    }

    @Test
    void testReplayProcessorWithPublishFinishes() {
        Flux<Integer> flux = createReplayFlux().publish().autoConnect();

        test(flux);
    }
}
