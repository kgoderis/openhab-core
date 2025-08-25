package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MemoryMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;

/**
 * Snapshot class for memory usage metrics.
 * 
 * <p>
 * This class provides memory usage metrics including heap usage, non-heap usage,
 * memory allocation rates, and garbage collection metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record MemoryUsageSnapshot(long total, long success, long failure, long totalDurationNanos,
        double heapMemoryUsage, double nonHeapMemoryUsage, double totalMemoryUsage, double memoryAllocationRate,
        double garbageCollectionFrequency, double memoryEfficiency, double memoryFragmentation, long availableMemory,
        long totalMemory, long usedMemory,
        long timestampMs) implements StatisticsSnapshot, CountsMetrics, LatencyMetrics, MemoryMetrics {

    /**
     * Create a memory usage snapshot.
     * 
     * @param total total operations
     * @param success successful operations
     * @param failure failed operations
     * @param totalDurationNanos total duration in nanoseconds
     * @param heapMemoryUsage heap memory usage percentage
     * @param nonHeapMemoryUsage non-heap memory usage percentage
     * @param totalMemoryUsage total memory usage percentage
     * @param memoryAllocationRate memory allocation rate in bytes per second
     * @param garbageCollectionFrequency garbage collection frequency per minute
     * @param memoryEfficiency memory efficiency score
     * @param memoryFragmentation memory fragmentation level
     * @param availableMemory available memory in bytes
     * @param totalMemory total memory in bytes
     * @param usedMemory used memory in bytes
     * @param timestampMs timestamp in milliseconds
     */
    public MemoryUsageSnapshot {
        // Validation
        if (total < 0) {
            throw new IllegalArgumentException("total must be non-negative");
        }
        if (success < 0) {
            throw new IllegalArgumentException("success must be non-negative");
        }
        if (failure < 0) {
            throw new IllegalArgumentException("failure must be non-negative");
        }
        if (totalDurationNanos < 0) {
            throw new IllegalArgumentException("totalDurationNanos must be non-negative");
        }
        if (heapMemoryUsage < 0.0 || heapMemoryUsage > 100.0) {
            throw new IllegalArgumentException("heapMemoryUsage must be between 0.0 and 100.0");
        }
        if (nonHeapMemoryUsage < 0.0 || nonHeapMemoryUsage > 100.0) {
            throw new IllegalArgumentException("nonHeapMemoryUsage must be between 0.0 and 100.0");
        }
        if (totalMemoryUsage < 0.0 || totalMemoryUsage > 100.0) {
            throw new IllegalArgumentException("totalMemoryUsage must be between 0.0 and 100.0");
        }
        if (memoryAllocationRate < 0.0) {
            throw new IllegalArgumentException("memoryAllocationRate must be non-negative");
        }
        if (garbageCollectionFrequency < 0.0) {
            throw new IllegalArgumentException("garbageCollectionFrequency must be non-negative");
        }
        if (memoryEfficiency < 0.0 || memoryEfficiency > 100.0) {
            throw new IllegalArgumentException("memoryEfficiency must be between 0.0 and 100.0");
        }
        if (memoryFragmentation < 0.0 || memoryFragmentation > 100.0) {
            throw new IllegalArgumentException("memoryFragmentation must be between 0.0 and 100.0");
        }
        if (availableMemory < 0) {
            throw new IllegalArgumentException("availableMemory must be non-negative");
        }
        if (totalMemory < 0) {
            throw new IllegalArgumentException("totalMemory must be non-negative");
        }
        if (usedMemory < 0) {
            throw new IllegalArgumentException("usedMemory must be non-negative");
        }
        if (timestampMs < 0) {
            throw new IllegalArgumentException("timestampMs must be non-negative");
        }
    }

    /**
     * Create a memory usage snapshot from basic metrics.
     * 
     * @param total total operations
     * @param success successful operations
     * @param failure failed operations
     * @param totalDurationNanos total duration in nanoseconds
     * @param heapMemoryUsage heap memory usage percentage
     * @param nonHeapMemoryUsage non-heap memory usage percentage
     * @param availableMemory available memory in bytes
     * @param totalMemory total memory in bytes
     * @param usedMemory used memory in bytes
     * @return memory usage snapshot
     */
    public static MemoryUsageSnapshot of(long total, long success, long failure, long totalDurationNanos,
            double heapMemoryUsage, double nonHeapMemoryUsage, long availableMemory, long totalMemory,
            long usedMemory) {
        double totalMemoryUsage = totalMemory > 0 ? (double) usedMemory / totalMemory * 100.0 : 0.0;
        double memoryAllocationRate = totalDurationNanos > 0
                ? (double) usedMemory / (totalDurationNanos / 1_000_000_000.0)
                : 0.0;
        double garbageCollectionFrequency = total > 0 ? (double) total / 60.0 : 0.0; // Simplified calculation
        double memoryEfficiency = totalMemory > 0 ? (double) availableMemory / totalMemory * 100.0 : 0.0;
        double memoryFragmentation = totalMemory > 0
                ? (double) (totalMemory - usedMemory - availableMemory) / totalMemory * 100.0
                : 0.0;

        return new MemoryUsageSnapshot(total, success, failure, totalDurationNanos, heapMemoryUsage, nonHeapMemoryUsage,
                totalMemoryUsage, memoryAllocationRate, garbageCollectionFrequency, memoryEfficiency,
                memoryFragmentation, availableMemory, totalMemory, usedMemory, System.currentTimeMillis());
    }

    /**
     * Create an empty memory usage snapshot.
     * 
     * @return empty memory usage snapshot
     */
    public static MemoryUsageSnapshot empty() {
        return new MemoryUsageSnapshot(0, 0, 0, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0, 0,
                System.currentTimeMillis());
    }

    // CountsMetrics implementation
    @Override
    public long total() {
        return total;
    }

    @Override
    public long success() {
        return success;
    }

    @Override
    public long failure() {
        return failure;
    }

    // LatencyMetrics implementation
    @Override
    public long totalDurationNanos() {
        return totalDurationNanos;
    }

    // MemoryMetrics implementation
    @Override
    public double heapMemoryUsage() {
        return heapMemoryUsage;
    }

    @Override
    public double nonHeapMemoryUsage() {
        return nonHeapMemoryUsage;
    }

    @Override
    public double totalMemoryUsage() {
        return totalMemoryUsage;
    }

    @Override
    public double memoryAllocationRate() {
        return memoryAllocationRate;
    }

    @Override
    public double garbageCollectionFrequency() {
        return garbageCollectionFrequency;
    }

    @Override
    public double memoryEfficiency() {
        return memoryEfficiency;
    }

    @Override
    public double memoryFragmentation() {
        return memoryFragmentation;
    }

    @Override
    public long availableMemory() {
        return availableMemory;
    }

    @Override
    public long totalMemory() {
        return totalMemory;
    }

    @Override
    public long usedMemory() {
        return usedMemory;
    }
}
