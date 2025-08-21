package org.openhab.core.ai.integration;

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
 * Performance optimization tests for the monitoring system.
 * 
 * This test validates performance characteristics and identifies
 * optimization opportunities in the monitoring system.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class PerformanceOptimizationTest {

    private DefaultMonitoringRegistry registry;
    private static final int WARMUP_ITERATIONS = 1000;
    private static final int BENCHMARK_ITERATIONS = 10000;
    private static final int CONCURRENT_THREADS = 10;

    @BeforeEach
    void setUp() {
        registry = new DefaultMonitoringRegistry();
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testCollectorPerformance() {
        ExecutionMetricsCollector collector = registry.executionCollector(MetricKeys.action("performance-test"));

        // Warmup
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            collector.recordExecution(i % 2 == 0, 100_000_000L);
        }

        // Benchmark
        Instant start = Instant.now();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            collector.recordExecution(i % 2 == 0, 100_000_000L);
        }
        Duration duration = Duration.between(start, Instant.now());

        // Performance assertions
        long operationsPerSecond = BENCHMARK_ITERATIONS * 1_000_000_000L / duration.toNanos();
        assertTrue(operationsPerSecond > 100_000,
                "Should achieve at least 100k operations/second, got: " + operationsPerSecond);

        // Verify data integrity
        ExecutionMetricsSnapshot snapshot = collector.snapshot();
        assertEquals(BENCHMARK_ITERATIONS + WARMUP_ITERATIONS, snapshot.total(), "Should have recorded all operations");
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testConcurrentCollectorPerformance() throws Exception {
        ExecutionMetricsCollector collector = registry.executionCollector(MetricKeys.action("concurrent-performance"));
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);
        AtomicLong totalOperations = new AtomicLong(0);

        // Warmup
        List<CompletableFuture<Void>> warmupFutures = new ArrayList<>();
        for (int t = 0; t < CONCURRENT_THREADS; t++) {
            final int threadId = t;
            warmupFutures.add(CompletableFuture.runAsync(() -> {
                for (int i = 0; i < WARMUP_ITERATIONS / CONCURRENT_THREADS; i++) {
                    collector.recordExecution(threadId % 2 == 0, 100_000_000L);
                    totalOperations.incrementAndGet();
                }
            }, executor));
        }
        CompletableFuture.allOf(warmupFutures.toArray(new CompletableFuture[0])).get();

        // Benchmark
        Instant start = Instant.now();
        List<CompletableFuture<Void>> benchmarkFutures = new ArrayList<>();
        for (int t = 0; t < CONCURRENT_THREADS; t++) {
            final int threadId = t;
            benchmarkFutures.add(CompletableFuture.runAsync(() -> {
                for (int i = 0; i < BENCHMARK_ITERATIONS / CONCURRENT_THREADS; i++) {
                    collector.recordExecution(threadId % 2 == 0, 100_000_000L);
                    totalOperations.incrementAndGet();
                }
            }, executor));
        }
        CompletableFuture.allOf(benchmarkFutures.toArray(new CompletableFuture[0])).get();
        Duration duration = Duration.between(start, Instant.now());

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // Performance assertions
        long operationsPerSecond = BENCHMARK_ITERATIONS * 1_000_000_000L / duration.toNanos();
        assertTrue(operationsPerSecond > 50_000,
                "Should achieve at least 50k operations/second under concurrent load, got: " + operationsPerSecond);

        // Verify data integrity
        ExecutionMetricsSnapshot snapshot = collector.snapshot();
        assertEquals(totalOperations.get(), snapshot.total(), "Should have recorded all operations without loss");
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testSnapshotGenerationPerformance() {
        ExecutionMetricsCollector collector = registry.executionCollector(MetricKeys.action("snapshot-performance"));

        // Record some data
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            collector.recordExecution(i % 2 == 0, 100_000_000L);
        }

        // Benchmark snapshot generation
        Instant start = Instant.now();
        for (int i = 0; i < 1000; i++) {
            ExecutionMetricsSnapshot snapshot = collector.snapshot();
            assertNotNull(snapshot, "Snapshot should not be null");
        }
        Duration duration = Duration.between(start, Instant.now());

        // Performance assertions
        long snapshotsPerSecond = 1000 * 1_000_000_000L / duration.toNanos();
        assertTrue(snapshotsPerSecond > 10_000,
                "Should generate at least 10k snapshots/second, got: " + snapshotsPerSecond);
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testRegistryPerformance() {
        // Benchmark registry operations
        Instant start = Instant.now();
        for (int i = 0; i < 1000; i++) {
            registry.executionCollector(MetricKeys.action("registry-test-" + i));
            registry.healthCollector(MetricKeys.provider("registry-provider-" + i));
        }
        Duration duration = Duration.between(start, Instant.now());

        // Performance assertions
        long operationsPerSecond = 2000 * 1_000_000_000L / duration.toNanos();
        assertTrue(operationsPerSecond > 1_000,
                "Should create at least 1k collectors/second, got: " + operationsPerSecond);

        // Verify registry state
        assertEquals(2000, registry.keys().size(), "Should have 2000 keys");
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testMemoryUsageOptimization() {
        // Test memory efficiency
        List<ExecutionMetricsCollector> collectors = new ArrayList<>();
        List<ProviderHealthCollector> healthCollectors = new ArrayList<>();

        // Create many collectors
        for (int i = 0; i < 1000; i++) {
            collectors.add(registry.executionCollector(MetricKeys.action("memory-test-" + i)));
            healthCollectors.add(registry.healthCollector(MetricKeys.provider("memory-provider-" + i)));
        }

        // Record data in all collectors
        for (int i = 0; i < 100; i++) {
            for (ExecutionMetricsCollector collector : collectors) {
                collector.recordExecution(i % 2 == 0, 100_000_000L);
            }
            for (ProviderHealthCollector collector : healthCollectors) {
                collector.recordHealthCheck(i % 2 == 0, 10_000_000L);
            }
        }

        // Generate snapshots for all collectors
        List<ExecutionMetricsSnapshot> executionSnapshots = registry.getExecutionSnapshots();
        List<ProviderHealthSnapshot> healthSnapshots = registry.getHealthSnapshots();

        // Verify all data is accessible
        assertEquals(1000, executionSnapshots.size(), "Should have 1000 execution snapshots");
        assertEquals(1000, healthSnapshots.size(), "Should have 1000 health snapshots");

        // Verify data integrity
        for (ExecutionMetricsSnapshot snapshot : executionSnapshots) {
            assertEquals(100, snapshot.total(), "Each snapshot should have 100 total executions");
        }
        for (ProviderHealthSnapshot snapshot : healthSnapshots) {
            assertEquals(100, snapshot.totalChecks(), "Each snapshot should have 100 total checks");
        }
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void testHotPathOptimization() {
        // Test optimization of frequently accessed paths
        ExecutionMetricsCollector collector = registry.executionCollector(MetricKeys.action("hot-path-test"));

        // Simulate hot path usage
        Instant start = Instant.now();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            // Record execution
            collector.recordExecution(i % 2 == 0, 100_000_000L);

            // Generate snapshot (hot path)
            ExecutionMetricsSnapshot snapshot = collector.snapshot();

            // Access computed values (hot path)
            double successRate = snapshot.successRate();
            double averageMs = snapshot.averageMs();

            // Verify computed values are reasonable
            assertTrue(successRate >= 0.0 && successRate <= 1.0, "Success rate should be between 0 and 1");
            assertTrue(averageMs >= 0.0, "Average duration should be non-negative");
        }
        Duration duration = Duration.between(start, Instant.now());

        // Performance assertions for hot path
        long operationsPerSecond = BENCHMARK_ITERATIONS * 1_000_000_000L / duration.toNanos();
        assertTrue(operationsPerSecond > 10_000,
                "Should handle hot path operations efficiently, got: " + operationsPerSecond);
    }
}
