package org.openhab.core.ai.common.monitoring.service.snapshot;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.openhab.core.ai.common.monitoring.api.Counts;
import org.openhab.core.ai.common.monitoring.api.Timing;

/**
 * Unit tests for ModelCompletionSnapshot.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class ModelCompletionSnapshotTest {

    @Test
    void testConstructorWithValidData() {
        // Given
        Counts counts = new Counts(100, 80, 20);
        Timing timing = new Timing(1000000000L); // 1 second
        long timestampMs = System.currentTimeMillis();
        long totalTokens = 15000;
        double totalCost = 0.15;

        // When
        ModelCompletionSnapshot snapshot = new ModelCompletionSnapshot(counts, timing, timestampMs, totalTokens,
                totalCost);

        // Then
        assertNotNull(snapshot);
        assertEquals(counts, snapshot.counts());
        assertEquals(timing, snapshot.timing());
        assertEquals(timestampMs, snapshot.getTimestampMs());
        assertEquals(totalTokens, snapshot.totalTokens());
        assertEquals(totalCost, snapshot.totalCost());
    }

    @Test
    void testCountsMetricsImplementation() {
        // Given
        Counts counts = new Counts(100, 75, 25);
        Timing timing = new Timing(500000000L);
        ModelCompletionSnapshot snapshot = new ModelCompletionSnapshot(counts, timing, System.currentTimeMillis(), 1000,
                0.01);

        // When & Then
        assertEquals(100, snapshot.total());
        assertEquals(75, snapshot.success());
        assertEquals(25, snapshot.failure());
        assertEquals(75.0, snapshot.successRate(), 0.001); // 75% success rate
    }

    @Test
    void testCountsMetricsWithZeroOperations() {
        // Given
        Counts counts = new Counts(0, 0, 0);
        Timing timing = new Timing(0L);
        ModelCompletionSnapshot snapshot = new ModelCompletionSnapshot(counts, timing, System.currentTimeMillis(), 0,
                0.0);

        // When & Then
        assertEquals(0, snapshot.total());
        assertEquals(0, snapshot.success());
        assertEquals(0, snapshot.failure());
        assertEquals(0.0, snapshot.successRate(), 0.001);
    }

    @Test
    void testCountsMetricsWithAllSuccess() {
        // Given
        Counts counts = new Counts(50, 50, 0);
        Timing timing = new Timing(1000000000L);
        ModelCompletionSnapshot snapshot = new ModelCompletionSnapshot(counts, timing, System.currentTimeMillis(), 5000,
                0.05);

        // When & Then
        assertEquals(50, snapshot.total());
        assertEquals(50, snapshot.success());
        assertEquals(0, snapshot.failure());
        assertEquals(100.0, snapshot.successRate(), 0.001); // 100% success rate
    }

    @Test
    void testCountsMetricsWithAllFailures() {
        // Given
        Counts counts = new Counts(30, 0, 30);
        Timing timing = new Timing(500000000L);
        ModelCompletionSnapshot snapshot = new ModelCompletionSnapshot(counts, timing, System.currentTimeMillis(), 0,
                0.0);

        // When & Then
        assertEquals(30, snapshot.total());
        assertEquals(0, snapshot.success());
        assertEquals(30, snapshot.failure());
        assertEquals(0.0, snapshot.successRate(), 0.001); // 0% success rate
    }

    @Test
    void testLatencyMetricsImplementation() {
        // Given
        Counts counts = new Counts(10, 8, 2);
        Timing timing = new Timing(2000000000L); // 2 seconds
        ModelCompletionSnapshot snapshot = new ModelCompletionSnapshot(counts, timing, System.currentTimeMillis(), 5000,
                0.05);

        // When & Then
        assertEquals(2000000000L, snapshot.totalDurationNanos());
        assertEquals(200.0, snapshot.averageMs(), 0.001); // 200ms average
        assertEquals(5.0, snapshot.operationsPerSecond(), 0.001); // 5 ops/sec
    }

    @Test
    void testLatencyMetricsWithZeroDuration() {
        // Given
        Counts counts = new Counts(5, 5, 0);
        Timing timing = new Timing(0L);
        ModelCompletionSnapshot snapshot = new ModelCompletionSnapshot(counts, timing, System.currentTimeMillis(), 1000,
                0.01);

        // When & Then
        assertEquals(0L, snapshot.totalDurationNanos());
        assertEquals(0.0, snapshot.averageMs(), 0.001);
        assertEquals(0.0, snapshot.operationsPerSecond(), 0.001);
    }

    @Test
    void testLatencyMetricsWithZeroOperations() {
        // Given
        Counts counts = new Counts(0, 0, 0);
        Timing timing = new Timing(1000000000L);
        ModelCompletionSnapshot snapshot = new ModelCompletionSnapshot(counts, timing, System.currentTimeMillis(), 0,
                0.0);

        // When & Then
        assertEquals(1000000000L, snapshot.totalDurationNanos());
        assertEquals(0.0, snapshot.averageMs(), 0.001);
        assertEquals(0.0, snapshot.operationsPerSecond(), 0.001);
    }

    @Test
    void testModelMetricsImplementation() {
        // Given
        Counts counts = new Counts(20, 18, 2);
        Timing timing = new Timing(1000000000L); // 1 second
        long totalTokens = 10000;
        double totalCost = 0.10;
        ModelCompletionSnapshot snapshot = new ModelCompletionSnapshot(counts, timing, System.currentTimeMillis(),
                totalTokens, totalCost);

        // When & Then
        assertEquals(10000.0, snapshot.tokensPerSecond(), 0.001); // 10k tokens/sec
        assertEquals(0.005, snapshot.costPerRequest(), 0.001); // $0.005 per request
        assertEquals(500.0, snapshot.averageTokensPerRequest(), 0.001); // 500 tokens per request
    }

    @Test
    void testModelMetricsWithZeroTokens() {
        // Given
        Counts counts = new Counts(10, 10, 0);
        Timing timing = new Timing(500000000L);
        ModelCompletionSnapshot snapshot = new ModelCompletionSnapshot(counts, timing, System.currentTimeMillis(), 0,
                0.0);

        // When & Then
        assertEquals(0.0, snapshot.tokensPerSecond(), 0.001);
        assertEquals(0.0, snapshot.costPerRequest(), 0.001);
        assertEquals(0.0, snapshot.averageTokensPerRequest(), 0.001);
    }

    @Test
    void testModelMetricsWithZeroOperations() {
        // Given
        Counts counts = new Counts(0, 0, 0);
        Timing timing = new Timing(1000000000L);
        ModelCompletionSnapshot snapshot = new ModelCompletionSnapshot(counts, timing, System.currentTimeMillis(), 5000,
                0.05);

        // When & Then
        assertEquals(0.0, snapshot.tokensPerSecond(), 0.001);
        assertEquals(0.0, snapshot.costPerRequest(), 0.001);
        assertEquals(0.0, snapshot.averageTokensPerRequest(), 0.001);
    }

    @Test
    void testModelMetricsWithHighThroughput() {
        // Given
        Counts counts = new Counts(1000, 950, 50);
        Timing timing = new Timing(1000000000L); // 1 second
        long totalTokens = 500000; // 500k tokens
        double totalCost = 5.0; // $5.00
        ModelCompletionSnapshot snapshot = new ModelCompletionSnapshot(counts, timing, System.currentTimeMillis(),
                totalTokens, totalCost);

        // When & Then
        assertEquals(500000.0, snapshot.tokensPerSecond(), 0.001); // 500k tokens/sec
        assertEquals(0.005, snapshot.costPerRequest(), 0.001); // $0.005 per request
        assertEquals(500.0, snapshot.averageTokensPerRequest(), 0.001); // 500 tokens per request
        assertEquals(1000.0, snapshot.operationsPerSecond(), 0.001); // 1000 ops/sec
    }

    @Test
    void testModelMetricsWithLowThroughput() {
        // Given
        Counts counts = new Counts(1, 1, 0);
        Timing timing = new Timing(1000000000L); // 1 second for 1 operation
        long totalTokens = 100;
        double totalCost = 0.001;
        ModelCompletionSnapshot snapshot = new ModelCompletionSnapshot(counts, timing, System.currentTimeMillis(),
                totalTokens, totalCost);

        // When & Then
        assertEquals(100.0, snapshot.tokensPerSecond(), 0.001); // 100 tokens/sec
        assertEquals(0.001, snapshot.costPerRequest(), 0.001); // $0.001 per request
        assertEquals(100.0, snapshot.averageTokensPerRequest(), 0.001); // 100 tokens per request
        assertEquals(1.0, snapshot.operationsPerSecond(), 0.001); // 1 op/sec
    }

    @Test
    void testRecordEquality() {
        // Given
        Counts counts1 = new Counts(100, 80, 20);
        Timing timing1 = new Timing(1000000000L);
        long timestampMs = System.currentTimeMillis();
        long totalTokens = 5000;
        double totalCost = 0.05;

        ModelCompletionSnapshot snapshot1 = new ModelCompletionSnapshot(counts1, timing1, timestampMs, totalTokens,
                totalCost);
        ModelCompletionSnapshot snapshot2 = new ModelCompletionSnapshot(counts1, timing1, timestampMs, totalTokens,
                totalCost);

        // When & Then
        assertEquals(snapshot1, snapshot2);
        assertEquals(snapshot1.hashCode(), snapshot2.hashCode());
    }

    @Test
    void testRecordInequality() {
        // Given
        Counts counts1 = new Counts(100, 80, 20);
        Counts counts2 = new Counts(100, 90, 10);
        Timing timing = new Timing(1000000000L);
        long timestampMs = System.currentTimeMillis();
        long totalTokens = 5000;
        double totalCost = 0.05;

        ModelCompletionSnapshot snapshot1 = new ModelCompletionSnapshot(counts1, timing, timestampMs, totalTokens,
                totalCost);
        ModelCompletionSnapshot snapshot2 = new ModelCompletionSnapshot(counts2, timing, timestampMs, totalTokens,
                totalCost);

        // When & Then
        assertNotEquals(snapshot1, snapshot2);
        assertNotEquals(snapshot1.hashCode(), snapshot2.hashCode());
    }

    @Test
    void testToString() {
        // Given
        Counts counts = new Counts(100, 80, 20);
        Timing timing = new Timing(1000000000L);
        long timestampMs = System.currentTimeMillis();
        long totalTokens = 5000;
        double totalCost = 0.05;

        ModelCompletionSnapshot snapshot = new ModelCompletionSnapshot(counts, timing, timestampMs, totalTokens,
                totalCost);

        // When
        String result = snapshot.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("ModelCompletionSnapshot"));
        assertTrue(result.contains("counts="));
        assertTrue(result.contains("timing="));
        assertTrue(result.contains("timestampMs="));
        assertTrue(result.contains("totalTokens="));
        assertTrue(result.contains("totalCost="));
    }

    @Test
    void testEdgeCases() {
        // Test with maximum values
        Counts maxCounts = new Counts(Long.MAX_VALUE, Long.MAX_VALUE - 1, 1);
        Timing maxTiming = new Timing(Long.MAX_VALUE);
        ModelCompletionSnapshot maxSnapshot = new ModelCompletionSnapshot(maxCounts, maxTiming, Long.MAX_VALUE,
                Long.MAX_VALUE, Double.MAX_VALUE);

        assertNotNull(maxSnapshot);
        assertEquals(Long.MAX_VALUE, maxSnapshot.total());
        assertEquals(Long.MAX_VALUE - 1, maxSnapshot.success());
        assertEquals(1, maxSnapshot.failure());
        assertEquals(Long.MAX_VALUE, maxSnapshot.totalDurationNanos());
        assertEquals(Long.MAX_VALUE, maxSnapshot.totalTokens());
        assertEquals(Double.MAX_VALUE, maxSnapshot.totalCost());

        // Test with minimum values
        Counts minCounts = new Counts(0, 0, 0);
        Timing minTiming = new Timing(0L);
        ModelCompletionSnapshot minSnapshot = new ModelCompletionSnapshot(minCounts, minTiming, 0L, 0L, 0.0);

        assertNotNull(minSnapshot);
        assertEquals(0, minSnapshot.total());
        assertEquals(0, minSnapshot.success());
        assertEquals(0, minSnapshot.failure());
        assertEquals(0L, minSnapshot.totalDurationNanos());
        assertEquals(0L, minSnapshot.totalTokens());
        assertEquals(0.0, minSnapshot.totalCost());
    }
}
