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
                .thenConsumeWhile(i -> i < 10)
                .thenCancel()
                .verify();
    }

    @Test
    void testFinishes() {
        Flux<Integer> flux = Flux.fromStream(Stream.iterate(0, i -> i + 1));

        test(flux);
    }

    @Test
    void testNeverFinishes() {
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            Flux<Integer> flux = Flux.fromStream(Stream.iterate(0, i -> i + 1));

            test(flux.publish().autoConnect());
        });
    }

    private Flux<Integer> createReplay() {
        int count = 100;
        ReplayProcessor<Integer> flux = ReplayProcessor.create(count);

        for (int i = 0; i < count; i++) flux.onNext(i);

        return flux;
    }

    @Test
    void testReplayProcessorFinishes() {
        Flux<Integer> flux = createReplay();

        test(flux);
    }

    @Test
    void testReplayProcessorWithPublishFinishes() {
        Flux<Integer> flux = createReplay();

        test(flux.publish().autoConnect());
    }
}
