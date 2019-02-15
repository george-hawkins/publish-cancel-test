package com.example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.ReplayProcessor;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class RecordTest {
    private final static int CAPACITY = 100;

    @Test
    void testConsumeLessThanAvailable() {
        test(CAPACITY - 1);
    }

    @Test
    void testConsumeAllAvailable() {
        Assertions.assertTimeoutPreemptively(Duration.ofSeconds(2), () -> {
            test(CAPACITY);
        });
    }

    @Test
    void testConsumeAllAvailableWithoutExpect() {
        // If you knock out the expect bit then everything finishes.
        testWithoutExpect(CAPACITY);
    }

    private void test(int end) {
        List<Integer> expected = IntStream.range(0, end).boxed().collect(Collectors.toList());

        ReplayProcessor<Integer> flux = ReplayProcessor.create(CAPACITY);

        for (int i = 0; i < CAPACITY; i++) flux.onNext(i);

        StepVerifier.create(flux)
                .recordWith(ArrayList::new)
                .thenConsumeWhile(i -> i < (end - 1))
                .expectRecordedMatches(actual -> {
                    boolean result = expected.equals(actual);
                    System.err.println("result = " + result);
                    return result;
                })
                .thenCancel()
                .verify();
    }

    private void testWithoutExpect(int end) {
        ReplayProcessor<Integer> flux = ReplayProcessor.create(CAPACITY);

        for (int i = 0; i < CAPACITY; i++) flux.onNext(i);

        StepVerifier.create(flux)
                .recordWith(ArrayList::new)
                .thenConsumeWhile(i -> i < (end - 1))
                .thenCancel()
                .verify();
    }
}
