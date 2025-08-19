package org.openhab.core.ai.common.metrics;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.ai.common.metrics.api.Counts;
import org.openhab.core.ai.common.metrics.api.Timing;
import org.openhab.core.ai.common.metrics.collector.ExecutionMetricsCollector;
import org.openhab.core.ai.common.metrics.snapshot.DelegationMetricsSnapshot;

/**
 * Test for delegation metrics functionality.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class DelegationMetricsTest {

    private ExecutionMetricsCollector collector;

    @BeforeEach
    void setUp() {
        collector = new ExecutionMetricsCollector();
    }

    @Test
    void testRecordExecution() {
        // Record some executions
        collector.recordExecution(true, 1000000); // 1ms success
        collector.recordExecution(false, 2000000); // 2ms failure
        collector.recordExecution(true, 1500000); // 1.5ms success

        // Get snapshot
        var snapshot = collector.snapshot();

        // Verify counts
        assertEquals(3, snapshot.total());
        assertEquals(2, snapshot.success());
        assertEquals(1, snapshot.failure());

        // Verify timing (in nanoseconds)
        assertEquals(4500000, snapshot.totalDurationNanos());

        // Verify success rate
        assertEquals(66.67, snapshot.successRate(), 0.01);

        // Verify average time
        assertEquals(1.5, snapshot.averageMs(), 0.01);
    }

    @Test
    void testDelegationMetricsSnapshot() {
        var counts = new Counts(10, 8, 2);
        var timing = new Timing(5000000); // 5ms total
        var snapshot = new DelegationMetricsSnapshot(counts, timing, System.currentTimeMillis(), 3);

        // Test delegation-specific methods
        assertEquals(10, snapshot.getTotalDelegations());
        assertEquals(8, snapshot.getSuccessfulDelegations());
        assertEquals(2, snapshot.getFailedDelegations());
        assertEquals(5000000, snapshot.getTotalDelegationTime());
        assertEquals(3, snapshot.getRegisteredAgents());
        assertEquals(80.0, snapshot.getSuccessRate(), 0.01);
        assertEquals(0.5, snapshot.getAverageDelegationTime(), 0.01);
    }

    @Test
    void testCountsValidation() {
        // Test valid counts
        assertDoesNotThrow(() -> new Counts(10, 8, 2));

        // Test invalid counts
        assertThrows(IllegalArgumentException.class, () -> new Counts(-1, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new Counts(10, -1, 0));
        assertThrows(IllegalArgumentException.class, () -> new Counts(10, 0, -1));
        assertThrows(IllegalArgumentException.class, () -> new Counts(10, 8, 3)); // 8+3 > 10
    }

    @Test
    void testTimingValidation() {
        // Test valid timing
        assertDoesNotThrow(() -> new Timing(1000000));

        // Test invalid timing
        assertThrows(IllegalArgumentException.class, () -> new Timing(-1));
    }
}
