package org.openhab.core.ai.reasoning.memory;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.base.Counts;
import org.openhab.core.ai.common.monitoring.base.Timing;

/**
 * Snapshot of memory performance metrics at a point in time.
 * 
 * <p>
 * This class provides an immutable view of memory management metrics including store/retrieve counts,
 * consolidation rates, and timing information.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record MemoryPerformanceSnapshot(Counts counts, Timing timing, long timestampMs, long totalMemoryOperations, long totalMemoryConsolidations, double memoryUtilization)
        implements MetricsSnapshot, CountsMetrics, LatencyMetrics {

    /**
     * Create a memory performance snapshot from individual metrics.
     * 
     * @param total total number of operations
     * @param success number of successful operations
     * @param failure number of failed operations
     * @param totalDurationNanos total duration in nanoseconds
     * @param totalMemoryOperations total number of memory operations
     * @param totalMemoryConsolidations total number of memory consolidations
     * @param memoryUtilization memory utilization percentage
     * @return the memory performance snapshot
     */
    public static MemoryPerformanceSnapshot of(long total, long success, long failure, long totalDurationNanos, long totalMemoryOperations, long totalMemoryConsolidations, double memoryUtilization) {
        return new MemoryPerformanceSnapshot(
            new Counts(total, success, failure),
            new Timing(totalDurationNanos),
            System.currentTimeMillis(),
            totalMemoryOperations,
            totalMemoryConsolidations,
            memoryUtilization
        );
    }

    /**
     * Create an empty memory performance snapshot.
     * 
     * @return an empty memory performance snapshot
     */
    public static MemoryPerformanceSnapshot empty() {
        return new MemoryPerformanceSnapshot(
            new Counts(0, 0, 0),
            new Timing(0),
            System.currentTimeMillis(),
            0,
            0,
            0.0
        );
    }

    @Override
    public long total() {
        return counts.total();
    }

    @Override
    public long success() {
        return counts.success();
    }

    @Override
    public long failure() {
        return counts.failure();
    }

    @Override
    public long totalDurationNanos() {
        return timing.totalDurationNanos();
    }

    /**
     * Get the average operation time in milliseconds.
     * 
     * @return average operation time in milliseconds
     */
    public double averageMs() {
        return LatencyMetrics.super.averageMs(total());
    }

    /**
     * Get the total number of memory operations.
     * 
     * @return total number of memory operations
     */
    public long totalMemoryOperations() {
        return totalMemoryOperations;
    }

    /**
     * Get the total number of memory consolidations.
     * 
     * @return total number of memory consolidations
     */
    public long totalMemoryConsolidations() {
        return totalMemoryConsolidations;
    }

    /**
     * Get the memory utilization percentage.
     * 
     * @return memory utilization percentage
     */
    public double memoryUtilization() {
        return memoryUtilization;
    }
}
