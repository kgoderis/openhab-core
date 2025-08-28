package org.openhab.core.ai.tool.resources.monitoring;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.api.ResourceMetrics;

/**
 * Immutable snapshot of provider resource usage metrics.
 * 
 * <p>
 * This record provides a point-in-time view of resource utilization for a specific
 * model provider, including request counts, latency metrics, and resource consumption.
 * It implements multiple capability interfaces for different types of metric analysis.
 * </p>
 * 
 * @param providerId unique identifier for the provider
 * @param concurrentRequests current number of concurrent requests
 * @param totalRequests total number of requests processed
 * @param successfulRequests number of successful requests
 * @param failedRequests number of failed requests
 * @param totalDurationNanos total execution time in nanoseconds
 * @param memoryUsageBytes current memory usage in bytes
 * @param maxMemoryBytes maximum memory available in bytes
 * @param cpuUsagePercentage current CPU usage percentage
 * @param timestampMs timestamp when snapshot was created
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ProviderResourceSnapshot(String providerId, long concurrentRequests, long totalRequests,
        long successfulRequests, long failedRequests, long totalDurationNanos, long memoryUsageBytes,
        long maxMemoryBytes, double cpuUsagePercentage,
        long timestampMs) implements MetricsSnapshot, CountsMetrics, LatencyMetrics, ResourceMetrics {

    /**
     * Create a ProviderResourceSnapshot with validation.
     */
    public ProviderResourceSnapshot {
        Objects.requireNonNull(providerId, "providerId cannot be null");
        if (concurrentRequests < 0) {
            throw new IllegalArgumentException("concurrentRequests cannot be negative");
        }
        if (totalRequests < 0) {
            throw new IllegalArgumentException("totalRequests cannot be negative");
        }
        if (successfulRequests < 0) {
            throw new IllegalArgumentException("successfulRequests cannot be negative");
        }
        if (failedRequests < 0) {
            throw new IllegalArgumentException("failedRequests cannot be negative");
        }
        if (totalDurationNanos < 0) {
            throw new IllegalArgumentException("totalDurationNanos cannot be negative");
        }
        if (memoryUsageBytes < 0) {
            throw new IllegalArgumentException("memoryUsageBytes cannot be negative");
        }
        if (maxMemoryBytes < 0) {
            throw new IllegalArgumentException("maxMemoryBytes cannot be negative");
        }
        if (cpuUsagePercentage < 0.0 || cpuUsagePercentage > 100.0) {
            throw new IllegalArgumentException("cpuUsagePercentage must be between 0.0 and 100.0");
        }
        if (timestampMs <= 0) {
            throw new IllegalArgumentException("timestampMs must be positive");
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
        return failedRequests;
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
        return 0; // Not tracked for provider resources
    }

    @Override
    public long totalDiskBytes() {
        return 0; // Not tracked for provider resources
    }

    @Override
    public long networkBytesReceived() {
        return 0; // Not tracked for provider resources
    }

    @Override
    public long networkBytesSent() {
        return 0; // Not tracked for provider resources
    }

    @Override
    public int activeThreads() {
        return 0; // Not tracked for provider resources
    }

    @Override
    public int maxThreads() {
        return 0; // Not tracked for provider resources
    }

    // Additional provider-specific metrics

    /**
     * Get memory utilization percentage.
     * 
     * @return memory utilization percentage (0.0-100.0)
     */
    public double memoryUtilizationPercentage() {
        return maxMemoryBytes > 0 ? (double) memoryUsageBytes / maxMemoryBytes * 100.0 : 0.0;
    }

    /**
     * Get current load percentage based on concurrent requests.
     * This can be used for load balancing decisions.
     * 
     * @return load percentage (0.0-100.0)
     */
    public double currentLoadPercentage() {
        // Assume a reasonable maximum of 100 concurrent requests for percentage calculation
        return Math.min(100.0, concurrentRequests / 100.0 * 100.0);
    }

    /**
     * Check if the provider is under heavy load.
     * 
     * @return true if under heavy load
     */
    public boolean isUnderHeavyLoad() {
        return concurrentRequests > 50 || cpuUsagePercentage > 80.0 || memoryUtilizationPercentage() > 85.0;
    }

    /**
     * Create a builder for constructing ProviderResourceSnapshot instances.
     * 
     * @return new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for ProviderResourceSnapshot with validation and defaults.
     */
    public static final class Builder {
        private String providerId = "";
        private long concurrentRequests = 0;
        private long totalRequests = 0;
        private long successfulRequests = 0;
        private long failedRequests = 0;
        private long totalDurationNanos = 0;
        private long memoryUsageBytes = 0;
        private long maxMemoryBytes = 0;
        private double cpuUsagePercentage = 0.0;
        private long timestampMs = System.currentTimeMillis();

        public Builder withProviderId(String providerId) {
            this.providerId = Objects.requireNonNull(providerId, "providerId cannot be null");
            return this;
        }

        public Builder withConcurrentRequests(long concurrentRequests) {
            this.concurrentRequests = concurrentRequests;
            return this;
        }

        public Builder withTotalRequests(long totalRequests) {
            this.totalRequests = totalRequests;
            return this;
        }

        public Builder withSuccessfulRequests(long successfulRequests) {
            this.successfulRequests = successfulRequests;
            return this;
        }

        public Builder withFailedRequests(long failedRequests) {
            this.failedRequests = failedRequests;
            return this;
        }

        public Builder withTotalDurationNanos(long totalDurationNanos) {
            this.totalDurationNanos = totalDurationNanos;
            return this;
        }

        public Builder withMemoryUsageBytes(long memoryUsageBytes) {
            this.memoryUsageBytes = memoryUsageBytes;
            return this;
        }

        public Builder withMaxMemoryBytes(long maxMemoryBytes) {
            this.maxMemoryBytes = maxMemoryBytes;
            return this;
        }

        public Builder withCpuUsagePercentage(double cpuUsagePercentage) {
            this.cpuUsagePercentage = cpuUsagePercentage;
            return this;
        }

        public Builder withTimestampMs(long timestampMs) {
            this.timestampMs = timestampMs;
            return this;
        }

        public ProviderResourceSnapshot build() {
            return new ProviderResourceSnapshot(providerId, concurrentRequests, totalRequests, successfulRequests,
                    failedRequests, totalDurationNanos, memoryUsageBytes, maxMemoryBytes, cpuUsagePercentage,
                    timestampMs);
        }
    }
}
