package org.openhab.core.ai.common.monitoring.performance;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
 * Performance tests for the monitoring system.
 * 
 * This test class validates performance characteristics of the new monitoring
 * infrastructure compared to legacy implementations.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class MonitoringPerformanceTest {

    private DefaultMonitoringRegistry registry;
    private ExecutorService executorService;

    private static final int THREAD_COUNT = 10;
    private static final int OPERATIONS_PER_THREAD = 10_000;
    private static final int TOTAL_OPERATIONS = THREAD_COUNT * OPERATIONS_PER_THREAD;

    @BeforeEach
    void setUp() {
        registry = new DefaultMonitoringRegistry();
        executorService = Executors.newFixedThreadPool(THREAD_COUNT);
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testCollectorPerformanceVsAtomicLong() throws InterruptedException {
        // Test LongAdder-based collector performance
        ExecutionMetricsCollector collector = new ExecutionMetricsCollector("test-collector");

        Instant startTime = Instant.now();

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int t = 0; t < THREAD_COUNT; t++) {
            futures.add(CompletableFuture.runAsync(() -> {
                for (int i = 0; i < OPERATIONS_PER_THREAD; i++) {
                    collector.recordExecution(i % 2 == 0, 1000L); // Alternate success/failure
                }
            }, executorService));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        Duration collectorDuration = Duration.between(startTime, Instant.now());

        // Test AtomicLong-based performance for comparison
        AtomicLong atomicTotal = new AtomicLong(0);
        AtomicLong atomicSuccess = new AtomicLong(0);
        AtomicLong atomicTime = new AtomicLong(0);

        startTime = Instant.now();

        List<CompletableFuture<Void>> atomicFutures = new ArrayList<>();
        for (int t = 0; t < THREAD_COUNT; t++) {
            atomicFutures.add(CompletableFuture.runAsync(() -> {
                for (int i = 0; i < OPERATIONS_PER_THREAD; i++) {
                    atomicTotal.incrementAndGet();
                    atomicTime.addAndGet(1000L);
                    if (i % 2 == 0) {
                        atomicSuccess.incrementAndGet();
                    }
                }
            }, executorService));
        }

        CompletableFuture.allOf(atomicFutures.toArray(new CompletableFuture[0])).join();

        Duration atomicDuration = Duration.between(startTime, Instant.now());

        // Verify correctness
        ExecutionMetricsSnapshot snapshot = collector.snapshot();
        assertEquals(TOTAL_OPERATIONS, snapshot.total());
        assertEquals(TOTAL_OPERATIONS / 2, snapshot.successful());
        assertEquals(TOTAL_OPERATIONS / 2, snapshot.failed());

        assertEquals(TOTAL_OPERATIONS, atomicTotal.get());
        assertEquals(TOTAL_OPERATIONS / 2, atomicSuccess.get());

        System.out.printf("LongAdder Collector: %dms, AtomicLong: %dms%n", collectorDuration.toMillis(),
                atomicDuration.toMillis());

        // LongAdder should be at least as fast or faster under high contention
        // Allow some tolerance for measurement variance
        assertTrue(collectorDuration.toMillis() <= atomicDuration.toMillis() * 1.5,
                "LongAdder collector should not be significantly slower than AtomicLong");
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testMemoryUsageUnderLoad() throws InterruptedException {
        // Measure memory usage before test
        System.gc();
        long memoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        // Create many collectors and generate snapshots
        List<ExecutionMetricsCollector> collectors = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            ExecutionMetricsCollector collector = new ExecutionMetricsCollector("collector-" + i);
            collectors.add(collector);

            // Record some metrics
            for (int j = 0; j < 100; j++) {
                collector.recordExecution(j % 3 == 0, j * 1000L);
            }
        }

        // Generate snapshots
        List<ExecutionMetricsSnapshot> snapshots = new ArrayList<>();
        for (ExecutionMetricsCollector collector : collectors) {
            snapshots.add(collector.snapshot());
        }

        // Force garbage collection and measure memory
        System.gc();
        Thread.sleep(100); // Allow GC to complete
        long memoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        long memoryUsed = memoryAfter - memoryBefore;
        System.out.printf("Memory used: %d bytes (%.2f MB)%n", memoryUsed, memoryUsed / (1024.0 * 1024.0));

        // Verify memory usage is reasonable (less than 50MB for 1000 collectors)
        assertTrue(memoryUsed < 50 * 1024 * 1024, "Memory usage should be reasonable");

        // Verify all snapshots are valid
        assertEquals(1000, snapshots.size());
        for (ExecutionMetricsSnapshot snapshot : snapshots) {
            assertEquals(100, snapshot.total());
            assertTrue(snapshot.successful() > 0);
            assertTrue(snapshot.failed() > 0);
        }
    }

    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    void testSnapshotGenerationPerformance() {
        // Create collector with significant data
        ExecutionMetricsCollector collector = new ExecutionMetricsCollector("perf-test");

        // Load collector with data
        for (int i = 0; i < 100_000; i++) {
            collector.recordExecution(i % 4 != 0, i * 1000L);
        }

        // Benchmark snapshot generation
        Instant startTime = Instant.now();

        List<ExecutionMetricsSnapshot> snapshots = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            snapshots.add(collector.snapshot());
        }

        Duration snapshotDuration = Duration.between(startTime, Instant.now());

        System.out.printf("Generated 1000 snapshots in %dms (%.3fms per snapshot)%n", snapshotDuration.toMillis(),
                snapshotDuration.toMillis() / 1000.0);

        // Snapshot generation should be very fast (< 1ms per snapshot on average)
        assertTrue(snapshotDuration.toMillis() < 1000, "Snapshot generation should be fast");

        // Verify all snapshots are identical and correct
        ExecutionMetricsSnapshot firstSnapshot = snapshots.get(0);
        for (ExecutionMetricsSnapshot snapshot : snapshots) {
            assertEquals(firstSnapshot.total(), snapshot.total());
            assertEquals(firstSnapshot.successful(), snapshot.successful());
            assertEquals(firstSnapshot.failed(), snapshot.failed());
        }
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void testRegistryPerformanceWithHighCardinality() throws InterruptedException {
        // Test registry performance with many different metric keys
        final int KEY_COUNT = 1000;
        final int OPERATIONS_PER_KEY = 100;

        Instant startTime = Instant.now();

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int t = 0; t < THREAD_COUNT; t++) {
            final int threadId = t;
            futures.add(CompletableFuture.runAsync(() -> {
                for (int k = 0; k < KEY_COUNT / THREAD_COUNT; k++) {
                    String keyName = "provider-" + (threadId * KEY_COUNT / THREAD_COUNT + k);
                    var collector = registry.executionCollector(MetricKeys.provider(keyName));

                    for (int op = 0; op < OPERATIONS_PER_KEY; op++) {
                        collector.recordExecution(op % 3 == 0, op * 1000L);
                    }
                }
            }, executorService));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        Duration registryDuration = Duration.between(startTime, Instant.now());

        // Verify registry state
        var keys = registry.keys();
        assertEquals(KEY_COUNT, keys.size());

        var snapshots = registry.getMetricsSnapshots();
        assertEquals(KEY_COUNT, snapshots.size());

        System.out.printf("Registry with %d keys: %dms%n", KEY_COUNT, registryDuration.toMillis());

        // Registry operations should scale reasonably with key count
        assertTrue(registryDuration.toMillis() < 5000, "Registry should handle high cardinality efficiently");
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void testProviderHealthCollectorPerformance() throws InterruptedException {
        ProviderHealthCollector healthCollector = new ProviderHealthCollector("provider-test");

        Instant startTime = Instant.now();

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (int t = 0; t < THREAD_COUNT; t++) {
            futures.add(CompletableFuture.runAsync(() -> {
                for (int i = 0; i < OPERATIONS_PER_THREAD; i++) {
                    if (i % 10 == 0) {
                        healthCollector.recordFailure("Simulated failure");
                    } else {
                        healthCollector.recordSuccess();
                    }
                }
            }, executorService));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        Duration healthDuration = Duration.between(startTime, Instant.now());

        // Verify health collector state
        ProviderHealthSnapshot snapshot = healthCollector.snapshot();
        assertEquals(TOTAL_OPERATIONS, snapshot.totalOperations());
        assertEquals(TOTAL_OPERATIONS * 9 / 10, snapshot.successfulOperations());
        assertEquals(TOTAL_OPERATIONS / 10, snapshot.failedOperations());

        System.out.printf("Health collector: %dms%n", healthDuration.toMillis());

        // Health collector should be performant
        assertTrue(healthDuration.toMillis() < 2000, "Health collector should be performant");
    }

    @Test
    void testCpuOverheadMeasurement() throws InterruptedException {
        // This test measures the CPU overhead of metrics collection
        long operationsWithoutMetrics = measureOperationsWithoutMetrics();
        long operationsWithMetrics = measureOperationsWithMetrics();

        double overhead = ((double) (operationsWithoutMetrics - operationsWithMetrics)) / operationsWithoutMetrics
                * 100;

        System.out.printf("Operations without metrics: %d/sec%n", operationsWithoutMetrics);
        System.out.printf("Operations with metrics: %d/sec%n", operationsWithMetrics);
        System.out.printf("Metrics overhead: %.2f%%%n", overhead);

        // Metrics overhead should be minimal (< 5%)
        assertTrue(overhead < 5.0, "Metrics overhead should be less than 5%");
    }

    private long measureOperationsWithoutMetrics() throws InterruptedException {
        final AtomicLong counter = new AtomicLong(0);
        final long testDurationMs = 1000; // 1 second

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        Instant startTime = Instant.now();

        for (int t = 0; t < THREAD_COUNT; t++) {
            futures.add(CompletableFuture.runAsync(() -> {
                while (Duration.between(startTime, Instant.now()).toMillis() < testDurationMs) {
                    // Simulate some work
                    counter.incrementAndGet();
                    Math.sqrt(counter.get()); // Some CPU work
                }
            }, executorService));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return counter.get();
    }

    private long measureOperationsWithMetrics() throws InterruptedException {
        final AtomicLong counter = new AtomicLong(0);
        final long testDurationMs = 1000; // 1 second
        ExecutionMetricsCollector collector = new ExecutionMetricsCollector("overhead-test");

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        Instant startTime = Instant.now();

        for (int t = 0; t < THREAD_COUNT; t++) {
            futures.add(CompletableFuture.runAsync(() -> {
                while (Duration.between(startTime, Instant.now()).toMillis() < testDurationMs) {
                    // Simulate some work with metrics
                    long count = counter.incrementAndGet();
                    Math.sqrt(count); // Some CPU work
                    collector.recordExecution(true, 1000L); // Record metrics
                }
            }, executorService));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        return counter.get();
    }
}
