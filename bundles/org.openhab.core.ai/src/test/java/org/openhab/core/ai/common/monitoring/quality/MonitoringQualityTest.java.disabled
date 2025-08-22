package org.openhab.core.ai.common.monitoring.quality;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.common.monitoring.api.MetricKeys;
import org.openhab.core.ai.common.monitoring.collector.ExecutionMetricsCollector;
import org.openhab.core.ai.common.monitoring.collector.ProviderHealthCollector;
import org.openhab.core.ai.common.monitoring.registry.DefaultMonitoringRegistry;
import org.openhab.core.ai.common.monitoring.snapshot.ExecutionMetricsSnapshot;
import org.openhab.core.ai.common.monitoring.snapshot.ProviderHealthSnapshot;

/**
 * Quality assurance tests for the monitoring system.
 * 
 * This test class validates edge cases, mathematical correctness,
 * and proper behavior under boundary conditions.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public class MonitoringQualityTest {

    @Test
    void testExecutionMetricsWithZeroOperations() {
        ExecutionMetricsCollector collector = new ExecutionMetricsCollector("zero-ops-test");
        ExecutionMetricsSnapshot snapshot = collector.snapshot();

        // Verify zero state
        assertEquals(0, snapshot.total());
        assertEquals(0, snapshot.successful());
        assertEquals(0, snapshot.failed());
        assertEquals(0, snapshot.totalDurationNanos());

        // Verify computed values handle zero gracefully
        assertEquals(0.0, snapshot.operationsPerSecond());
        assertEquals(0.0, snapshot.failureRatePercent());
        assertEquals(100.0, snapshot.successRatePercent()); // Should be 100% when no operations
        assertEquals(0.0, snapshot.averageLatencyMs());
        assertEquals(1.0, snapshot.efficiencyScore()); // Perfect efficiency with no operations
        assertTrue(snapshot.isHealthy()); // Should be healthy with no failures
    }

    @Test
    void testExecutionMetricsWithAllFailures() {
        ExecutionMetricsCollector collector = new ExecutionMetricsCollector("all-failures-test");

        // Record only failures
        for (int i = 0; i < 100; i++) {
            collector.recordExecution(false, 1000_000_000L); // 1 second in nanoseconds
        }

        ExecutionMetricsSnapshot snapshot = collector.snapshot();

        assertEquals(100, snapshot.total());
        assertEquals(0, snapshot.successful());
        assertEquals(100, snapshot.failed());

        // Verify computed values
        assertEquals(0.0, snapshot.successRatePercent());
        assertEquals(100.0, snapshot.failureRatePercent());
        assertFalse(snapshot.isHealthy()); // Should be unhealthy with 100% failures

        // Operations per second should still be calculated correctly
        assertTrue(snapshot.operationsPerSecond() > 0);
        assertEquals(1000.0, snapshot.averageLatencyMs(), 0.001); // 1 second average
    }

    @Test
    void testExecutionMetricsWithAllSuccesses() {
        ExecutionMetricsCollector collector = new ExecutionMetricsCollector("all-success-test");

        // Record only successes
        for (int i = 0; i < 50; i++) {
            collector.recordExecution(true, 500_000_000L); // 0.5 seconds in nanoseconds
        }

        ExecutionMetricsSnapshot snapshot = collector.snapshot();

        assertEquals(50, snapshot.total());
        assertEquals(50, snapshot.successful());
        assertEquals(0, snapshot.failed());

        // Verify computed values
        assertEquals(100.0, snapshot.successRatePercent());
        assertEquals(0.0, snapshot.failureRatePercent());
        assertTrue(snapshot.isHealthy()); // Should be healthy with 100% success
        assertEquals(500.0, snapshot.averageLatencyMs(), 0.001); // 0.5 second average
    }

    @Test
    void testExecutionMetricsWithExtremeValues() {
        ExecutionMetricsCollector collector = new ExecutionMetricsCollector("extreme-values-test");

        // Test with very large duration values
        collector.recordExecution(true, Long.MAX_VALUE / 2);
        collector.recordExecution(false, Long.MAX_VALUE / 4);

        ExecutionMetricsSnapshot snapshot = collector.snapshot();

        assertEquals(2, snapshot.total());
        assertEquals(1, snapshot.successful());
        assertEquals(1, snapshot.failed());

        // Should handle large values without overflow
        assertTrue(snapshot.totalDurationNanos() > 0);
        assertTrue(snapshot.averageLatencyMs() > 0);
        assertTrue(snapshot.operationsPerSecond() >= 0);
    }

    @Test
    void testProviderHealthWithZeroOperations() {
        ProviderHealthCollector collector = new ProviderHealthCollector("zero-health-test");
        ProviderHealthSnapshot snapshot = collector.snapshot();

        assertEquals(0, snapshot.totalOperations());
        assertEquals(0, snapshot.successfulOperations());
        assertEquals(0, snapshot.failedOperations());

        // Verify computed health metrics
        assertEquals(100.0, snapshot.uptimePercent()); // 100% uptime with no operations
        assertEquals(0.0, snapshot.errorRatePercent());
        assertEquals(0.0, snapshot.averageResponseTimeMs());
        assertEquals(100.0, snapshot.getHealthScore(), 0.001);
    }

    @Test
    void testProviderHealthEdgeCases() {
        ProviderHealthCollector collector = new ProviderHealthCollector("health-edge-test");

        // Test with mixed success/failure patterns
        for (int i = 0; i < 1000; i++) {
            if (i % 10 == 0) {
                collector.recordFailure("Error " + i);
            } else {
                collector.recordSuccess();
            }
        }

        ProviderHealthSnapshot snapshot = collector.snapshot();

        assertEquals(1000, snapshot.totalOperations());
        assertEquals(900, snapshot.successfulOperations());
        assertEquals(100, snapshot.failedOperations());

        // Verify computed metrics
        assertEquals(90.0, snapshot.uptimePercent());
        assertEquals(10.0, snapshot.errorRatePercent());

        // Health score should reflect the 90% success rate
        double expectedScore = 90.0; // Simplified calculation
        assertTrue(Math.abs(snapshot.getHealthScore() - expectedScore) < 10.0);
    }

    @Test
    void testSnapshotEqualsAndHashCode() {
        ExecutionMetricsCollector collector1 = new ExecutionMetricsCollector("equals-test-1");
        ExecutionMetricsCollector collector2 = new ExecutionMetricsCollector("equals-test-1"); // Same ID

        // Record identical data
        for (int i = 0; i < 10; i++) {
            collector1.recordExecution(i % 2 == 0, i * 1000L);
            collector2.recordExecution(i % 2 == 0, i * 1000L);
        }

        ExecutionMetricsSnapshot snapshot1 = collector1.snapshot();
        ExecutionMetricsSnapshot snapshot2 = collector2.snapshot();

        // Should be equal if they have the same data
        assertEquals(snapshot1, snapshot2);
        assertEquals(snapshot1.hashCode(), snapshot2.hashCode());

        // Test with different data
        collector2.recordExecution(true, 1000L);
        ExecutionMetricsSnapshot snapshot3 = collector2.snapshot();

        assertNotEquals(snapshot1, snapshot3);
    }

    @Test
    void testSnapshotToString() {
        ExecutionMetricsCollector collector = new ExecutionMetricsCollector("toString-test");
        collector.recordExecution(true, 1000_000_000L);
        collector.recordExecution(false, 2000_000_000L);

        ExecutionMetricsSnapshot snapshot = collector.snapshot();
        String toString = snapshot.toString();

        // Verify toString contains expected information
        assertTrue(toString.contains("ExecutionMetricsSnapshot"));
        assertTrue(toString.contains("totalOperations=2"));
        assertTrue(toString.contains("successfulOperations=1"));
        assertTrue(toString.contains("failedOperations=1"));
        assertTrue(toString.contains("operationsPerSecond"));
        assertTrue(toString.contains("successRatePercent"));
        assertTrue(toString.contains("averageLatencyMs"));
    }

    @Test
    void testMetricKeyGeneration() {
        // Test various metric key patterns
        var providerKey1 = MetricKeys.provider("openai");
        var providerKey2 = MetricKeys.provider("anthropic");
        var toolKey1 = MetricKeys.tool("weather");
        var toolKey2 = MetricKeys.tool("calculator");

        // Keys should be unique
        assertNotEquals(providerKey1.id(), providerKey2.id());
        assertNotEquals(toolKey1.id(), toolKey2.id());
        assertNotEquals(providerKey1.id(), toolKey1.id());

        // Same parameters should generate same keys
        var providerKey1Duplicate = MetricKeys.provider("openai");
        assertEquals(providerKey1.id(), providerKey1Duplicate.id());

        // Keys should contain expected information
        assertTrue(providerKey1.id().contains("provider"));
        assertTrue(providerKey1.id().contains("openai"));
        assertTrue(toolKey1.id().contains("tool"));
        assertTrue(toolKey1.id().contains("weather"));
    }

    @Test
    void testMetricKeyValidation() {
        // Test with various input patterns
        assertDoesNotThrow(() -> MetricKeys.provider("valid-provider"));
        assertDoesNotThrow(() -> MetricKeys.tool("valid_tool"));
        assertDoesNotThrow(() -> MetricKeys.provider("provider123"));
        assertDoesNotThrow(() -> MetricKeys.tool("tool-with-dashes"));

        // Test edge cases
        assertDoesNotThrow(() -> MetricKeys.provider(""));
        assertDoesNotThrow(() -> MetricKeys.tool("a"));
        assertDoesNotThrow(() -> MetricKeys.provider("very-long-provider-name-that-might-cause-issues"));
    }

    @Test
    void testRegistryEdgeCases() {
        DefaultMonitoringRegistry registry = new DefaultMonitoringRegistry();

        // Test with empty registry
        assertTrue(registry.keys().isEmpty());
        assertTrue(registry.getMetricsSnapshots().isEmpty());
        assertTrue(registry.getStatisticsSnapshots().isEmpty());
        assertTrue(registry.getHealthSnapshots().isEmpty());

        // Test multiple collectors with same key
        var collector1 = registry.executionCollector(MetricKeys.provider("test"));
        var collector2 = registry.executionCollector(MetricKeys.provider("test"));

        // Should return the same collector instance
        assertSame(collector1, collector2);

        // Test after recording data
        collector1.recordExecution(true, 1000L);

        List<ExecutionMetricsSnapshot> snapshots = registry.getMetricsSnapshots();
        assertEquals(1, snapshots.size());
        assertEquals(1, snapshots.get(0).total());
    }

    @Test
    void testRegistryReset() {
        DefaultMonitoringRegistry registry = new DefaultMonitoringRegistry();

        // Add some data
        var collector = registry.executionCollector(MetricKeys.provider("test"));
        collector.recordExecution(true, 1000L);
        collector.recordExecution(false, 2000L);

        // Verify data exists
        assertEquals(1, registry.keys().size());
        assertEquals(1, registry.getMetricsSnapshots().size());
        assertEquals(2, registry.getMetricsSnapshots().get(0).total());

        // Reset specific key
        registry.reset(MetricKeys.provider("test"));

        // Verify reset
        assertEquals(1, registry.keys().size()); // Key still exists
        assertEquals(1, registry.getMetricsSnapshots().size());
        assertEquals(0, registry.getMetricsSnapshots().get(0).total()); // But data is reset

        // Add more data and reset all
        collector.recordExecution(true, 1000L);
        registry.resetAll();

        // Verify complete reset
        assertTrue(registry.keys().isEmpty());
        assertTrue(registry.getMetricsSnapshots().isEmpty());
    }

    @Test
    void testMathematicalAccuracy() {
        ExecutionMetricsCollector collector = new ExecutionMetricsCollector("math-test");

        // Record precise test data
        collector.recordExecution(true, 1_000_000_000L); // 1 second
        collector.recordExecution(true, 2_000_000_000L); // 2 seconds
        collector.recordExecution(false, 3_000_000_000L); // 3 seconds
        collector.recordExecution(true, 4_000_000_000L); // 4 seconds

        ExecutionMetricsSnapshot snapshot = collector.snapshot();

        // Verify exact calculations
        assertEquals(4, snapshot.total());
        assertEquals(3, snapshot.successful());
        assertEquals(1, snapshot.failed());
        assertEquals(10_000_000_000L, snapshot.totalDurationNanos()); // 10 seconds total

        // Verify computed metrics with precision
        assertEquals(75.0, snapshot.successRatePercent(), 0.001); // 3/4 = 75%
        assertEquals(25.0, snapshot.failureRatePercent(), 0.001); // 1/4 = 25%
        assertEquals(2500.0, snapshot.averageLatencyMs(), 0.001); // 10s/4 = 2.5s = 2500ms

        // Operations per second calculation
        // 4 operations in 10 seconds = 0.4 ops/sec
        assertEquals(0.4, snapshot.operationsPerSecond(), 0.001);
    }
}
