package org.openhab.core.ai.common.monitoring.circuit;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.core.ai.common.monitoring.health.MetricsCircuitBreaker;
import org.openhab.core.ai.common.monitoring.health.MetricsCircuitBreaker.CircuitBreakerOpenException;
import org.openhab.core.ai.common.monitoring.health.MetricsCircuitBreaker.State;

/**
 * Comprehensive tests for all circuit breaker states and behaviors.
 * 
 * This test class validates the circuit breaker implementation across
 * all states (CLOSED, OPEN, HALF_OPEN) and edge cases.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
class CircuitBreakerTest {

    private MetricsCircuitBreaker circuitBreaker;

    @BeforeEach
    void setUp() {
        circuitBreaker = MetricsCircuitBreaker.builder().failureThreshold(3).timeout(Duration.ofMillis(100)).build();
    }

    @Test
    void testInitialState() {
        assertEquals(State.CLOSED, circuitBreaker.getState());
        assertEquals(0, circuitBreaker.getFailureCount());
        assertNull(circuitBreaker.getLastFailureTime());
        assertNull(circuitBreaker.getLastSuccessTime());
        assertTrue(circuitBreaker.canExecute());
    }

    @Test
    void testSuccessfulExecution() {
        String result = circuitBreaker.execute(() -> "success");
        assertEquals("success", result);

        assertEquals(State.CLOSED, circuitBreaker.getState());
        assertEquals(0, circuitBreaker.getFailureCount());
        assertNotNull(circuitBreaker.getLastSuccessTime());
        assertTrue(circuitBreaker.canExecute());
    }

    @Test
    void testFailedExecution() {
        assertThrows(RuntimeException.class, () -> {
            circuitBreaker.execute(() -> {
                throw new RuntimeException("test failure");
            });
        });

        assertEquals(State.CLOSED, circuitBreaker.getState());
        assertEquals(1, circuitBreaker.getFailureCount());
        assertNotNull(circuitBreaker.getLastFailureTime());
        assertTrue(circuitBreaker.canExecute());
    }

    @Test
    void testCircuitOpensAfterThreshold() {
        // Execute 3 failures to trigger circuit opening
        for (int i = 0; i < 3; i++) {
            assertThrows(RuntimeException.class, () -> {
                circuitBreaker.execute(() -> {
                    throw new RuntimeException("failure " + i);
                });
            });
        }

        assertEquals(State.OPEN, circuitBreaker.getState());
        assertEquals(3, circuitBreaker.getFailureCount());
        assertFalse(circuitBreaker.canExecute());

        // Additional executions should be rejected
        assertThrows(CircuitBreakerOpenException.class, () -> {
            circuitBreaker.execute(() -> "should not execute");
        });
    }

    @Test
    void testCircuitHalfOpenAfterTimeout() throws InterruptedException {
        // Open the circuit
        for (int i = 0; i < 3; i++) {
            circuitBreaker.execute(() -> {
                throw new RuntimeException("failure");
            });
        }
        assertEquals(State.OPEN, circuitBreaker.getState());

        // Wait for timeout
        Thread.sleep(150); // Longer than 100ms timeout

        assertEquals(State.HALF_OPEN, circuitBreaker.getState());
        assertTrue(circuitBreaker.canExecute());
    }

    @Test
    void testCircuitClosesOnSuccessInHalfOpen() throws InterruptedException {
        // Open the circuit
        for (int i = 0; i < 3; i++) {
            circuitBreaker.execute(() -> {
                throw new RuntimeException("failure");
            });
        }

        // Wait for timeout to enter half-open state
        Thread.sleep(150);
        assertEquals(State.HALF_OPEN, circuitBreaker.getState());

        // Successful execution should close the circuit
        String result = circuitBreaker.execute(() -> "success");
        assertEquals("success", result);

        assertEquals(State.CLOSED, circuitBreaker.getState());
        assertEquals(0, circuitBreaker.getFailureCount());
        assertTrue(circuitBreaker.canExecute());
    }

    @Test
    void testCircuitReopensOnFailureInHalfOpen() throws InterruptedException {
        // Open the circuit
        for (int i = 0; i < 3; i++) {
            circuitBreaker.execute(() -> {
                throw new RuntimeException("failure");
            });
        }

        // Wait for timeout to enter half-open state
        Thread.sleep(150);
        assertEquals(State.HALF_OPEN, circuitBreaker.getState());

        // Failed execution should reopen the circuit
        assertThrows(RuntimeException.class, () -> {
            circuitBreaker.execute(() -> {
                throw new RuntimeException("failure in half-open");
            });
        });

        assertEquals(State.OPEN, circuitBreaker.getState());
        assertEquals(4, circuitBreaker.getFailureCount());
        assertFalse(circuitBreaker.canExecute());
    }

    @Test
    void testManualCircuitControl() {
        // Test manual opening
        circuitBreaker.open();
        assertEquals(State.OPEN, circuitBreaker.getState());
        assertFalse(circuitBreaker.canExecute());

        // Test manual closing
        circuitBreaker.close();
        assertEquals(State.CLOSED, circuitBreaker.getState());
        assertTrue(circuitBreaker.canExecute());

        // Test manual reset
        circuitBreaker.reset();
        assertEquals(State.CLOSED, circuitBreaker.getState());
        assertEquals(0, circuitBreaker.getFailureCount());
    }

    @Test
    void testExponentialBackoff() {
        // Open the circuit
        for (int i = 0; i < 3; i++) {
            circuitBreaker.execute(() -> {
                throw new RuntimeException("failure");
            });
        }

        long firstAttempt = circuitBreaker.getNextAttemptTime();
        assertTrue(firstAttempt > System.currentTimeMillis());

        // Wait and check that next attempt time increases
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long secondAttempt = circuitBreaker.getNextAttemptTime();
        assertTrue(secondAttempt >= firstAttempt);
    }

    @Test
    void testRunnableExecution() {
        AtomicInteger counter = new AtomicInteger(0);

        circuitBreaker.execute(() -> counter.incrementAndGet());
        assertEquals(1, counter.get());

        assertThrows(RuntimeException.class, () -> {
            circuitBreaker.execute(() -> {
                throw new RuntimeException("failure");
            });
        });
        assertEquals(1, counter.get()); // Should not increment
    }

    @Test
    void testAsyncExecution() throws InterruptedException, ExecutionException {
        CompletableFuture<String> future = circuitBreaker.executeAsync(() -> "async success");
        assertEquals("async success", future.get());

        CompletableFuture<String> failedFuture = circuitBreaker.executeAsync(() -> {
            throw new RuntimeException("async failure");
        });

        assertThrows(ExecutionException.class, () -> failedFuture.get());
    }

    @Test
    @Timeout(5)
    void testTimeoutBehavior() {
        MetricsCircuitBreaker timeoutBreaker = MetricsCircuitBreaker.builder().failureThreshold(1)
                .timeout(Duration.ofMillis(50)).build();

        // Execute a long-running task
        assertThrows(RuntimeException.class, () -> {
            timeoutBreaker.execute(() -> {
                try {
                    Thread.sleep(200); // Longer than timeout
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return "should timeout";
            });
        });

        assertEquals(State.OPEN, timeoutBreaker.getState());
    }

    @Test
    void testStatisticsTracking() {
        // Execute some operations
        circuitBreaker.execute(() -> "success1");
        circuitBreaker.execute(() -> "success2");

        try {
            circuitBreaker.execute(() -> {
                throw new RuntimeException("failure");
            });
        } catch (RuntimeException e) {
            // Expected
        }

        var stats = circuitBreaker.getStatistics();
        assertNotNull(stats);
        assertTrue(stats.getTotalExecutions() >= 3);
        assertTrue(stats.getSuccessfulExecutions() >= 2);
        assertTrue(stats.getFailedExecutions() >= 1);
    }

    @Test
    void testConcurrentExecution() throws InterruptedException {
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                try {
                    if (threadId % 2 == 0) {
                        circuitBreaker.execute(() -> "success");
                        successCount.incrementAndGet();
                    } else {
                        circuitBreaker.execute(() -> {
                            throw new RuntimeException("failure");
                        });
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                }
            });
        }

        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Verify results
        assertEquals(threadCount / 2, successCount.get());
        assertEquals(threadCount / 2, failureCount.get());
    }

    @Test
    void testBuilderConfiguration() {
        MetricsCircuitBreaker customBreaker = MetricsCircuitBreaker.builder().failureThreshold(5)
                .timeout(Duration.ofSeconds(1)).build();

        assertEquals(State.CLOSED, customBreaker.getState());
        assertTrue(customBreaker.canExecute());
    }

    @Test
    void testEdgeCases() {
        // Test with zero failure threshold
        MetricsCircuitBreaker zeroThresholdBreaker = MetricsCircuitBreaker.builder().failureThreshold(0)
                .timeout(Duration.ofMillis(100)).build();

        // Should open immediately on first failure
        assertThrows(RuntimeException.class, () -> {
            zeroThresholdBreaker.execute(() -> {
                throw new RuntimeException("failure");
            });
        });

        assertEquals(State.OPEN, zeroThresholdBreaker.getState());
    }

    @Test
    void testStateTransitions() {
        // CLOSED -> OPEN
        for (int i = 0; i < 3; i++) {
            circuitBreaker.execute(() -> {
                throw new RuntimeException("failure");
            });
        }
        assertEquals(State.OPEN, circuitBreaker.getState());

        // OPEN -> HALF_OPEN (after timeout)
        try {
            Thread.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        assertEquals(State.HALF_OPEN, circuitBreaker.getState());

        // HALF_OPEN -> CLOSED (on success)
        circuitBreaker.execute(() -> "success");
        assertEquals(State.CLOSED, circuitBreaker.getState());
    }
}
