package com.example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.stream.Stream;

class PublishCancelTest {
    private Flux<Integer> flux = Flux.fromStream(Stream.iterate(0, i -> i + 1));

    @Test
    void testFinishes() {
        StepVerifier.create(flux)
                .thenCancel()
                .verify();
    }

    @Test
    void testNeverFinishes() {
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            StepVerifier.create(flux.publish().autoConnect())
                    .thenCancel()
                    .verify();
        });
    }
}
