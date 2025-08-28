package org.openhab.core.ai.tool.resources.monitoring;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.api.ResourceMetrics;

/**
 * Resource Manager metrics snapshot providing counts, latency, and resource utilization data.
 * 
 * <p>
 * This immutable snapshot captures ResourceManager metrics at a specific point in time,
 * including request counts, processing latency, and resource utilization statistics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ResourceManagerSnapshot(long totalRequests, long successfulRequests, long failedRequests,
        long rejectedRequests, long timedOutRequests, long totalDurationNanos, long memoryUsageBytes,
        long maxMemoryBytes, double cpuUsagePercentage, long diskUsageBytes, long totalDiskBytes,
        long networkBytesReceived, long networkBytesSent, int activeThreads, int maxThreads,
        long timestampMs) implements MetricsSnapshot, CountsMetrics, LatencyMetrics, ResourceMetrics {

    /**
     * Create a new ResourceManagerSnapshot.
     * 
     * @param totalRequests total number of requests processed
     * @param successfulRequests number of successful requests
     * @param failedRequests number of failed requests
     * @param rejectedRequests number of rejected requests
     * @param timedOutRequests number of timed out requests
     * @param totalDurationNanos total processing duration in nanoseconds
     * @param memoryUsageBytes current memory usage in bytes
     * @param maxMemoryBytes maximum memory available in bytes
     * @param cpuUsagePercentage current CPU usage percentage
     * @param diskUsageBytes current disk usage in bytes
     * @param totalDiskBytes total disk space in bytes
     * @param networkBytesReceived network bytes received
     * @param networkBytesSent network bytes sent
     * @param activeThreads number of active threads
     * @param maxThreads maximum number of threads
     * @param timestampMs timestamp when snapshot was created
     */
    public ResourceManagerSnapshot {
        // Validation
        if (totalRequests < 0) {
            throw new IllegalArgumentException("totalRequests must not be negative");
        }
        if (successfulRequests < 0) {
            throw new IllegalArgumentException("successfulRequests must not be negative");
        }
        if (failedRequests < 0) {
            throw new IllegalArgumentException("failedRequests must not be negative");
        }
        if (rejectedRequests < 0) {
            throw new IllegalArgumentException("rejectedRequests must not be negative");
        }
        if (timedOutRequests < 0) {
            throw new IllegalArgumentException("timedOutRequests must not be negative");
        }
        if (totalDurationNanos < 0) {
            throw new IllegalArgumentException("totalDurationNanos must not be negative");
        }
        if (memoryUsageBytes < 0) {
            throw new IllegalArgumentException("memoryUsageBytes must not be negative");
        }
        if (maxMemoryBytes < 0) {
            throw new IllegalArgumentException("maxMemoryBytes must not be negative");
        }
        if (cpuUsagePercentage < 0.0 || cpuUsagePercentage > 100.0) {
            throw new IllegalArgumentException("cpuUsagePercentage must be between 0.0 and 100.0");
        }
        if (diskUsageBytes < 0) {
            throw new IllegalArgumentException("diskUsageBytes must not be negative");
        }
        if (totalDiskBytes < 0) {
            throw new IllegalArgumentException("totalDiskBytes must not be negative");
        }
        if (networkBytesReceived < 0) {
            throw new IllegalArgumentException("networkBytesReceived must not be negative");
        }
        if (networkBytesSent < 0) {
            throw new IllegalArgumentException("networkBytesSent must not be negative");
        }
        if (activeThreads < 0) {
            throw new IllegalArgumentException("activeThreads must not be negative");
        }
        if (maxThreads < 0) {
            throw new IllegalArgumentException("maxThreads must not be negative");
        }
    }

    // CountsMetrics implementation
    @Override
    public long total() {
        return totalRequests;
    }

    @Override
    public long success() {
        return successfulRequests;
    }

    @Override
    public long failure() {
        return failedRequests + rejectedRequests + timedOutRequests;
    }

    // LatencyMetrics implementation
    @Override
    public long totalDurationNanos() {
        return totalDurationNanos;
    }

    // ResourceMetrics implementation
    @Override
    public long memoryUsageBytes() {
        return memoryUsageBytes;
    }

    @Override
    public long maxMemoryBytes() {
        return maxMemoryBytes;
    }

    @Override
    public double cpuUsagePercentage() {
        return cpuUsagePercentage;
    }

    @Override
    public long diskUsageBytes() {
        return diskUsageBytes;
    }

    @Override
    public long totalDiskBytes() {
        return totalDiskBytes;
    }

    @Override
    public long networkBytesReceived() {
        return networkBytesReceived;
    }

    @Override
    public long networkBytesSent() {
        return networkBytesSent;
    }

    @Override
    public int activeThreads() {
        return activeThreads;
    }

    @Override
    public int maxThreads() {
        return maxThreads;
    }

    @Override
    public long getTimestampMs() {
        return timestampMs;
    }

    /**
     * Get the number of rejected requests.
     * 
     * @return rejected requests count
     */
    public long getRejectedRequests() {
        return rejectedRequests;
    }

    /**
     * Get the number of timed out requests.
     * 
     * @return timed out requests count
     */
    public long getTimedOutRequests() {
        return timedOutRequests;
    }

    /**
     * Calculate rejection rate as a percentage.
     * 
     * @return rejection rate between 0.0 and 1.0
     */
    public double rejectionRate() {
        return totalRequests > 0 ? (double) rejectedRequests / totalRequests : 0.0;
    }

    /**
     * Calculate timeout rate as a percentage.
     * 
     * @return timeout rate between 0.0 and 1.0
     */
    public double timeoutRate() {
        return totalRequests > 0 ? (double) timedOutRequests / totalRequests : 0.0;
    }

    /**
     * Create a ResourceManagerSnapshot with current system data.
     * 
     * @param totalRequests total number of requests
     * @param successfulRequests number of successful requests
     * @param failedRequests number of failed requests
     * @param rejectedRequests number of rejected requests
     * @param timedOutRequests number of timed out requests
     * @param totalDurationNanos total processing duration
     * @param activeThreads number of active threads
     * @param maxThreads maximum number of threads
     * @return new ResourceManagerSnapshot with current system resource data
     */
    public static ResourceManagerSnapshot createWithSystemResources(long totalRequests, long successfulRequests,
            long failedRequests, long rejectedRequests, long timedOutRequests, long totalDurationNanos,
            int activeThreads, int maxThreads) {

        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();

        // TODO: Implement actual CPU, disk, and network monitoring
        // For now, use placeholder values
        double cpuUsage = 0.0;
        long diskUsage = 0L;
        long totalDisk = 0L;
        long networkReceived = 0L;
        long networkSent = 0L;

        return new ResourceManagerSnapshot(totalRequests, successfulRequests, failedRequests, rejectedRequests,
                timedOutRequests, totalDurationNanos, usedMemory, maxMemory, cpuUsage, diskUsage, totalDisk,
                networkReceived, networkSent, activeThreads, maxThreads, System.currentTimeMillis());
    }
}
