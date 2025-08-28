package org.openhab.core.ai.common.monitoring.service.snapshot;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.openhab.core.ai.common.monitoring.api.Counts;
import org.openhab.core.ai.common.monitoring.api.Timing;

/**
 * Unit tests for ToolFileReadSnapshot.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
class ToolFileReadSnapshotTest {

    @Test
    void testConstructorWithValidData() {
        // Given
        Counts counts = new Counts(50, 45, 5);
        Timing timing = new Timing(2000000000L); // 2 seconds
        long timestampMs = System.currentTimeMillis();
        long totalBytesRead = 1024000L; // 1MB
        long totalFilesProcessed = 10L;

        // When
        ToolFileReadSnapshot snapshot = new ToolFileReadSnapshot(counts, timing, timestampMs, totalBytesRead,
                totalFilesProcessed);

        // Then
        assertNotNull(snapshot);
        assertEquals(counts, snapshot.counts());
        assertEquals(timing, snapshot.timing());
        assertEquals(timestampMs, snapshot.getTimestampMs());
        assertEquals(totalBytesRead, snapshot.totalBytesRead());
        assertEquals(totalFilesProcessed, snapshot.totalFilesProcessed());
    }

    @Test
    void testCountsMetricsImplementation() {
        // Given
        Counts counts = new Counts(100, 85, 15);
        Timing timing = new Timing(1000000000L);
        ToolFileReadSnapshot snapshot = new ToolFileReadSnapshot(counts, timing, System.currentTimeMillis(), 2048000L,
                20L);

        // When & Then
        assertEquals(100, snapshot.total());
        assertEquals(85, snapshot.success());
        assertEquals(15, snapshot.failure());
        assertEquals(85.0, snapshot.successRate(), 0.001); // 85% success rate
    }

    @Test
    void testCountsMetricsWithZeroOperations() {
        // Given
        Counts counts = new Counts(0, 0, 0);
        Timing timing = new Timing(0L);
        ToolFileReadSnapshot snapshot = new ToolFileReadSnapshot(counts, timing, System.currentTimeMillis(), 0L, 0L);

        // When & Then
        assertEquals(0, snapshot.total());
        assertEquals(0, snapshot.success());
        assertEquals(0, snapshot.failure());
        assertEquals(0.0, snapshot.successRate(), 0.001);
    }

    @Test
    void testCountsMetricsWithAllSuccess() {
        // Given
        Counts counts = new Counts(25, 25, 0);
        Timing timing = new Timing(500000000L);
        ToolFileReadSnapshot snapshot = new ToolFileReadSnapshot(counts, timing, System.currentTimeMillis(), 512000L,
                25L);

        // When & Then
        assertEquals(25, snapshot.total());
        assertEquals(25, snapshot.success());
        assertEquals(0, snapshot.failure());
        assertEquals(100.0, snapshot.successRate(), 0.001); // 100% success rate
    }

    @Test
    void testCountsMetricsWithAllFailures() {
        // Given
        Counts counts = new Counts(10, 0, 10);
        Timing timing = new Timing(100000000L);
        ToolFileReadSnapshot snapshot = new ToolFileReadSnapshot(counts, timing, System.currentTimeMillis(), 0L, 0L);

        // When & Then
        assertEquals(10, snapshot.total());
        assertEquals(0, snapshot.success());
        assertEquals(10, snapshot.failure());
        assertEquals(0.0, snapshot.successRate(), 0.001); // 0% success rate
    }

    @Test
    void testLatencyMetricsImplementation() {
        // Given
        Counts counts = new Counts(30, 28, 2);
        Timing timing = new Timing(1500000000L); // 1.5 seconds
        ToolFileReadSnapshot snapshot = new ToolFileReadSnapshot(counts, timing, System.currentTimeMillis(), 1536000L,
                30L);

        // When & Then
        assertEquals(1500000000L, snapshot.totalDurationNanos());
        assertEquals(50.0, snapshot.averageMs(), 0.001); // 1500ms / 30 operations = 50ms average
        assertEquals(20.0, snapshot.operationsPerSecond(), 0.001); // 30 operations / 1.5 seconds = 20 ops/sec
    }

    @Test
    void testLatencyMetricsWithZeroDuration() {
        // Given
        Counts counts = new Counts(5, 5, 0);
        Timing timing = new Timing(0L);
        ToolFileReadSnapshot snapshot = new ToolFileReadSnapshot(counts, timing, System.currentTimeMillis(), 256000L,
                5L);

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
        ToolFileReadSnapshot snapshot = new ToolFileReadSnapshot(counts, timing, System.currentTimeMillis(), 0L, 0L);

        // When & Then
        assertEquals(1000000000L, snapshot.totalDurationNanos());
        assertEquals(0.0, snapshot.averageMs(), 0.001);
        assertEquals(0.0, snapshot.operationsPerSecond(), 0.001);
    }

    @Test
    void testToolMetricsImplementation() {
        // Given
        Counts counts = new Counts(40, 38, 2);
        Timing timing = new Timing(2000000000L); // 2 seconds
        long totalBytesRead = 4096000L; // 4MB
        ToolFileReadSnapshot snapshot = new ToolFileReadSnapshot(counts, timing, System.currentTimeMillis(),
                totalBytesRead, 40L);

        // When & Then
        assertEquals(2048000.0, snapshot.throughputPerSecond(), 0.001); // 4MB / 2 seconds = 2MB/sec
        assertEquals(102400.0, snapshot.averageResourceUsage(), 0.001); // 4MB / 40 operations = 100KB per operation
        assertTrue(snapshot.maxConcurrentExecutions() > 0);
    }

    @Test
    void testToolMetricsWithZeroBytesRead() {
        // Given
        Counts counts = new Counts(10, 10, 0);
        Timing timing = new Timing(1000000000L);
        ToolFileReadSnapshot snapshot = new ToolFileReadSnapshot(counts, timing, System.currentTimeMillis(), 0L, 10L);

        // When & Then
        assertEquals(0.0, snapshot.throughputPerSecond(), 0.001);
        assertEquals(0.0, snapshot.averageResourceUsage(), 0.001);
        assertTrue(snapshot.maxConcurrentExecutions() > 0);
    }

    @Test
    void testToolMetricsWithZeroOperations() {
        // Given
        Counts counts = new Counts(0, 0, 0);
        Timing timing = new Timing(1000000000L);
        ToolFileReadSnapshot snapshot = new ToolFileReadSnapshot(counts, timing, System.currentTimeMillis(), 1024000L,
                0L);

        // When & Then
        assertEquals(1024000.0, snapshot.throughputPerSecond(), 0.001); // 1MB / 1 second = 1MB/sec
        assertEquals(0.0, snapshot.averageResourceUsage(), 0.001); // 0 operations, so 0 average
        assertEquals(1, snapshot.maxConcurrentExecutions()); // Default for zero operations
    }

    @Test
    void testToolMetricsWithZeroDuration() {
        // Given
        Counts counts = new Counts(5, 5, 0);
        Timing timing = new Timing(0L);
        ToolFileReadSnapshot snapshot = new ToolFileReadSnapshot(counts, timing, System.currentTimeMillis(), 512000L,
                5L);

        // When & Then
        assertEquals(0.0, snapshot.throughputPerSecond(), 0.001);
        assertEquals(102400.0, snapshot.averageResourceUsage(), 0.001); // 512KB / 5 operations = 100KB per operation
        assertTrue(snapshot.maxConcurrentExecutions() > 0);
    }

    @Test
    void testMaxConcurrentExecutionsWithManyOperations() {
        // Given
        Counts counts = new Counts(100, 95, 5);
        Timing timing = new Timing(5000000000L);
        ToolFileReadSnapshot snapshot = new ToolFileReadSnapshot(counts, timing, System.currentTimeMillis(), 10240000L,
                100L);

        // When
        int maxConcurrent = snapshot.maxConcurrentExecutions();

        // Then
        assertEquals(10, maxConcurrent); // Should be capped at 10 for large operation counts
    }

    @Test
    void testMaxConcurrentExecutionsWithFewOperations() {
        // Given
        Counts counts = new Counts(3, 3, 0);
        Timing timing = new Timing(100000000L);
        ToolFileReadSnapshot snapshot = new ToolFileReadSnapshot(counts, timing, System.currentTimeMillis(), 153600L,
                3L);

        // When
        int maxConcurrent = snapshot.maxConcurrentExecutions();

        // Then
        assertEquals(3, maxConcurrent); // Should be the number of operations for small counts
    }

    @Test
    void testEdgeCasesWithLargeNumbers() {
        // Given
        Counts counts = new Counts(Long.MAX_VALUE, Long.MAX_VALUE - 1, 1);
        Timing timing = new Timing(Long.MAX_VALUE);
        ToolFileReadSnapshot snapshot = new ToolFileReadSnapshot(counts, timing, System.currentTimeMillis(),
                Long.MAX_VALUE, Long.MAX_VALUE);

        // When & Then
        assertEquals(Long.MAX_VALUE, snapshot.total());
        assertEquals(Long.MAX_VALUE - 1, snapshot.success());
        assertEquals(1, snapshot.failure());
        assertTrue(snapshot.successRate() > 99.9); // Should be very close to 100%
        assertEquals(Long.MAX_VALUE, snapshot.totalDurationNanos());
        assertTrue(snapshot.throughputPerSecond() > 0);
        assertTrue(snapshot.averageResourceUsage() > 0);
        assertEquals(10, snapshot.maxConcurrentExecutions()); // Should be capped at 10
    }

    @Test
    void testPrecisionWithSmallValues() {
        // Given
        Counts counts = new Counts(1, 1, 0);
        Timing timing = new Timing(1000000L); // 1ms in nanos
        ToolFileReadSnapshot snapshot = new ToolFileReadSnapshot(counts, timing, System.currentTimeMillis(), 1024L, 1L);

        // When & Then
        assertEquals(1, snapshot.total());
        assertEquals(1, snapshot.success());
        assertEquals(0, snapshot.failure());
        assertEquals(100.0, snapshot.successRate(), 0.001);
        assertEquals(1000000L, snapshot.totalDurationNanos());
        assertEquals(1.0, snapshot.averageMs(), 0.001); // 1ms average
        assertEquals(1000.0, snapshot.operationsPerSecond(), 0.001); // 1 operation per 1ms = 1000 ops/sec
        assertEquals(1024000.0, snapshot.throughputPerSecond(), 0.001); // 1KB / 1ms = 1MB/sec
        assertEquals(1024.0, snapshot.averageResourceUsage(), 0.001); // 1KB per operation
        assertEquals(1, snapshot.maxConcurrentExecutions());
    }

    @Test
    void testRecordImmutability() {
        // Given
        Counts counts = new Counts(10, 8, 2);
        Timing timing = new Timing(1000000000L);
        long timestampMs = System.currentTimeMillis();
        long totalBytesRead = 512000L;
        long totalFilesProcessed = 10L;

        ToolFileReadSnapshot snapshot = new ToolFileReadSnapshot(counts, timing, timestampMs, totalBytesRead,
                totalFilesProcessed);

        // When & Then - Verify that the record is immutable and all fields are accessible
        assertEquals(counts, snapshot.counts());
        assertEquals(timing, snapshot.timing());
        assertEquals(timestampMs, snapshot.getTimestampMs());
        assertEquals(totalBytesRead, snapshot.totalBytesRead());
        assertEquals(totalFilesProcessed, snapshot.totalFilesProcessed());
    }

    @Test
    void testEqualsAndHashCode() {
        // Given
        Counts counts1 = new Counts(10, 8, 2);
        Timing timing1 = new Timing(1000000000L);
        long timestampMs = System.currentTimeMillis();
        long totalBytesRead = 512000L;
        long totalFilesProcessed = 10L;

        ToolFileReadSnapshot snapshot1 = new ToolFileReadSnapshot(counts1, timing1, timestampMs, totalBytesRead,
                totalFilesProcessed);
        ToolFileReadSnapshot snapshot2 = new ToolFileReadSnapshot(counts1, timing1, timestampMs, totalBytesRead,
                totalFilesProcessed);

        // When & Then
        assertEquals(snapshot1, snapshot2);
        assertEquals(snapshot1.hashCode(), snapshot2.hashCode());
    }

    @Test
    void testToString() {
        // Given
        Counts counts = new Counts(5, 4, 1);
        Timing timing = new Timing(500000000L);
        long timestampMs = System.currentTimeMillis();
        long totalBytesRead = 256000L;
        long totalFilesProcessed = 5L;

        ToolFileReadSnapshot snapshot = new ToolFileReadSnapshot(counts, timing, timestampMs, totalBytesRead,
                totalFilesProcessed);

        // When
        String result = snapshot.toString();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("ToolFileReadSnapshot"));
        assertTrue(result.contains("counts="));
        assertTrue(result.contains("timing="));
        assertTrue(result.contains("timestampMs="));
        assertTrue(result.contains("totalBytesRead="));
        assertTrue(result.contains("totalFilesProcessed="));
    }
}
