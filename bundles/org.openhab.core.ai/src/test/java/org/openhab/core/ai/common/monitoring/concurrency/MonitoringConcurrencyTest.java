package org.openhab.core.ai.common.monitoring.concurrency;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.collector.ExecutionMetricsCollector;
import org.openhab.core.ai.common.monitoring.collector.ProviderHealthCollector;
import org.openhab.core.ai.common.monitoring.registry.DefaultMonitoringRegistry;
import org.openhab.core.ai.common.monitoring.snapshot.ExecutionMetricsSnapshot;
import org.openhab.core.ai.common.monitoring.snapshot.ProviderHealthSnapshot;

/**
 * Concurrency tests for the monitoring system.
 * 
 * This test class validates thread safety and proper behavior under
 * extreme concurrency conditions.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class MonitoringConcurrencyTest {

    private DefaultMonitoringRegistry registry;
    private ExecutorService executorService;

    private static final int HIGH_THREAD_COUNT = 50;
    private static final int STRESS_OPERATIONS = 50_000;

    @BeforeEach
    void setUp() {
        registry = new DefaultMonitoringRegistry();
        executorService = Executors.newFixedThreadPool(HIGH_THREAD_COUNT);
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testExecutionMetricsCollectorThreadSafety() throws InterruptedException {
        ExecutionMetricsCollector collector = new ExecutionMetricsCollector("thread-safety-test");
        final AtomicLong totalOperations = new AtomicLong(0);
        final AtomicLong successfulOperations = new AtomicLong(0);
        final AtomicLong totalDuration = new AtomicLong(0);

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int t = 0; t < HIGH_THREAD_COUNT; t++) {
            final int threadId = t;
            futures.add(CompletableFuture.runAsync(() -> {
                for (int i = 0; i < STRESS_OPERATIONS / HIGH_THREAD_COUNT; i++) {
                    boolean success = (threadId + i) % 3 != 0; // Deterministic success pattern
                    long duration = (threadId * 1000L) + i;

                    collector.recordExecution(success, duration);

                    // Track expected values
                    totalOperations.incrementAndGet();
                    if (success) {
                        successfulOperations.incrementAndGet();
                    }
                    totalDuration.addAndGet(duration);
                }
            }, executorService));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // Verify thread safety - no lost updates
        ExecutionMetricsSnapshot snapshot = collector.snapshot();
        assertEquals(totalOperations.get(), snapshot.total(), "Total operations should match");
        assertEquals(successfulOperations.get(), snapshot.successful(), "Successful operations should match");
        assertEquals(totalOperations.get() - successfulOperations.get(), snapshot.failed(),
                "Failed operations should match");
        assertEquals(totalDuration.get(), snapshot.totalDurationNanos(), "Total duration should match");
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testProviderHealthCollectorConcurrency() throws InterruptedException {
        ProviderHealthCollector healthCollector = new ProviderHealthCollector("health-concurrency-test");
        final AtomicLong expectedSuccesses = new AtomicLong(0);
        final AtomicLong expectedFailures = new AtomicLong(0);

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int t = 0; t < HIGH_THREAD_COUNT; t++) {
            final int threadId = t;
            futures.add(CompletableFuture.runAsync(() -> {
                for (int i = 0; i < STRESS_OPERATIONS / HIGH_THREAD_COUNT; i++) {
                    if ((threadId + i) % 7 == 0) {
                        healthCollector.recordFailure("Thread-" + threadId + "-Error-" + i);
                        expectedFailures.incrementAndGet();
                    } else {
                        healthCollector.recordSuccess();
                        expectedSuccesses.incrementAndGet();
                    }
                }
            }, executorService));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // Verify thread safety
        ProviderHealthSnapshot snapshot = healthCollector.snapshot();
        assertEquals(expectedSuccesses.get() + expectedFailures.get(), snapshot.totalOperations());
        assertEquals(expectedSuccesses.get(), snapshot.successfulOperations());
        assertEquals(expectedFailures.get(), snapshot.failedOperations());
    }

    @Test
    @Timeout(value = 25, unit = TimeUnit.SECONDS)
    void testRegistryConcurrentAccess() throws InterruptedException {
        final int KEY_COUNT = 100;
        final AtomicLong totalRecordings = new AtomicLong(0);

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int t = 0; t < HIGH_THREAD_COUNT; t++) {
            final int threadId = t;
            futures.add(CompletableFuture.runAsync(() -> {
                for (int i = 0; i < STRESS_OPERATIONS / HIGH_THREAD_COUNT; i++) {
                    String keyName = "provider-" + (i % KEY_COUNT);
                    var collector = registry.executionCollector(MetricKeys.provider(keyName));
                    collector.recordExecution(i % 4 != 0, i * 1000L);
                    totalRecordings.incrementAndGet();
                }
            }, executorService));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // Verify registry consistency
        var keys = registry.keys();
        assertEquals(KEY_COUNT, keys.size(), "Should have correct number of keys");

        var snapshots = registry.getMetricsSnapshots();
        assertEquals(KEY_COUNT, snapshots.size(), "Should have snapshots for all keys");

        // Verify total operations across all collectors
        long totalOperations = snapshots.stream().mapToLong(s -> s.total()).sum();
        assertEquals(totalRecordings.get(), totalOperations, "No operations should be lost");
    }

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void testSnapshotConsistencyUnderLoad() throws InterruptedException {
        ExecutionMetricsCollector collector = new ExecutionMetricsCollector("consistency-test");
        final AtomicBoolean keepRunning = new AtomicBoolean(true);
        final List<ExecutionMetricsSnapshot> snapshots = new ArrayList<>();

        // Background thread continuously recording metrics
        CompletableFuture<Void> recordingTask = CompletableFuture.runAsync(() -> {
            long counter = 0;
            while (keepRunning.get()) {
                collector.recordExecution(counter % 5 != 0, counter * 1000L);
                counter++;

                // Small delay to prevent overwhelming the system
                if (counter % 1000 == 0) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }, executorService);

        // Multiple threads taking snapshots simultaneously
        List<CompletableFuture<Void>> snapshotTasks = new ArrayList<>();
        for (int t = 0; t < 10; t++) {
            snapshotTasks.add(CompletableFuture.runAsync(() -> {
                for (int i = 0; i < 100; i++) {
                    ExecutionMetricsSnapshot snapshot = collector.snapshot();
                    synchronized (snapshots) {
                        snapshots.add(snapshot);
                    }

                    // Verify snapshot internal consistency
                    assertEquals(snapshot.successful() + snapshot.failed(), snapshot.total(),
                            "Snapshot should be internally consistent");

                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }, executorService));
        }

        CompletableFuture.allOf(snapshotTasks.toArray(new CompletableFuture[0])).join();
        keepRunning.set(false);
        recordingTask.join();

        // Verify all snapshots are valid and monotonic
        assertFalse(snapshots.isEmpty(), "Should have captured snapshots");

        ExecutionMetricsSnapshot previousSnapshot = null;
        for (ExecutionMetricsSnapshot snapshot : snapshots) {
            // Each snapshot should be internally consistent
            assertEquals(snapshot.successful() + snapshot.failed(), snapshot.total());
            assertTrue(snapshot.total() >= 0);
            assertTrue(snapshot.successful() >= 0);
            assertTrue(snapshot.failed() >= 0);
            assertTrue(snapshot.totalDurationNanos() >= 0);

            // Snapshots should generally be monotonically increasing
            // (allowing for some variance due to timing)
            if (previousSnapshot != null) {
                assertTrue(snapshot.total() >= previousSnapshot.total() - 10,
                        "Snapshot totals should generally increase");
            }
            previousSnapshot = snapshot;
        }
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testRegistryResetUnderConcurrency() throws InterruptedException {
        final int RESET_INTERVAL_MS = 100;
        final AtomicBoolean keepRunning = new AtomicBoolean(true);

        // Continuous metric recording
        List<CompletableFuture<Void>> recordingTasks = new ArrayList<>();
        for (int t = 0; t < 10; t++) {
            final int threadId = t;
            recordingTasks.add(CompletableFuture.runAsync(() -> {
                long counter = 0;
                while (keepRunning.get()) {
                    String keyName = "provider-" + (threadId % 5);
                    var collector = registry.executionCollector(MetricKeys.provider(keyName));
                    collector.recordExecution(counter % 3 == 0, counter * 1000L);
                    counter++;

                    if (counter % 100 == 0) {
                        try {
                            Thread.sleep(1);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }
            }, executorService));
        }

        // Periodic registry resets
        CompletableFuture<Void> resetTask = CompletableFuture.runAsync(() -> {
            while (keepRunning.get()) {
                try {
                    Thread.sleep(RESET_INTERVAL_MS);
                    registry.resetAll();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, executorService);

        // Let it run for a while
        Thread.sleep(2000);
        keepRunning.set(false);

        CompletableFuture.allOf(recordingTasks.toArray(new CompletableFuture[0])).join();
        resetTask.join();

        // Verify registry is in a valid state after all the concurrent operations
        var snapshots = registry.getMetricsSnapshots();
        assertTrue(snapshots.size() <= 5, "Should have at most 5 providers");

        // All snapshots should be valid (though possibly empty due to resets)
        for (var snapshot : snapshots) {
            assertTrue(snapshot.total() >= 0);
            assertEquals(snapshot.successful() + snapshot.failed(), snapshot.total());
        }
    }

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void testExtremeContention() throws InterruptedException {
        // Test with extremely high contention on a single collector
        ExecutionMetricsCollector collector = new ExecutionMetricsCollector("extreme-contention");
        final int EXTREME_THREAD_COUNT = 100;
        final int OPERATIONS_PER_THREAD = 1000;

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        Instant startTime = Instant.now();

        for (int t = 0; t < EXTREME_THREAD_COUNT; t++) {
            futures.add(CompletableFuture.runAsync(() -> {
                for (int i = 0; i < OPERATIONS_PER_THREAD; i++) {
                    collector.recordExecution(i % 2 == 0, i * 1000L);
                }
            }, executorService));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        Duration duration = Duration.between(startTime, Instant.now());

        // Verify correctness under extreme contention
        ExecutionMetricsSnapshot snapshot = collector.snapshot();
        assertEquals(EXTREME_THREAD_COUNT * OPERATIONS_PER_THREAD, snapshot.total());
        assertEquals(EXTREME_THREAD_COUNT * OPERATIONS_PER_THREAD / 2, snapshot.successful());
        assertEquals(EXTREME_THREAD_COUNT * OPERATIONS_PER_THREAD / 2, snapshot.failed());

        System.out.printf("Extreme contention test completed in %dms%n", duration.toMillis());

        // Should complete within reasonable time even under extreme contention
        assertTrue(duration.toMillis() < 10000, "Should handle extreme contention efficiently");
    }
}
