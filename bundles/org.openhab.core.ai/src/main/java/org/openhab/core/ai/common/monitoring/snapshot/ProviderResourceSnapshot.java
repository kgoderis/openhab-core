/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.core.ai.common.monitoring.snapshot;

import java.time.Instant;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.Counts;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.api.ResourceMetrics;
import org.openhab.core.ai.common.monitoring.api.Timing;

/**
 * Immutable snapshot of provider resource usage metrics.
 * 
 * <p>
 * This class captures provider-specific resource utilization metrics including
 * request counts, response times, concurrent usage, and resource consumption.
 * It combines execution metrics with resource monitoring capabilities.
 * </p>
 * 
 * <h3>Provider Resource Metrics Include:</h3>
 * <ul>
 * <li>Request execution counts (total, success, failure)</li>
 * <li>Response time measurements and averages</li>
 * <li>Concurrent request tracking</li>
 * <li>Resource consumption (memory, CPU, threads)</li>
 * <li>Provider-specific resource limits and usage</li>
 * <li>Last activity timestamps</li>
 * </ul>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ProviderResourceSnapshot(Counts counts, Timing timing, long timestampMs, String providerName,
        long concurrentRequests, long memoryUsageBytes, long maxMemoryBytes, double cpuUsagePercentage,
        long diskUsageBytes, long totalDiskBytes, long networkBytesReceived, long networkBytesSent, int activeThreads,
        int maxThreads,
        Instant lastActivity) implements MetricsSnapshot, CountsMetrics, LatencyMetrics, ResourceMetrics {

    /**
     * Create a new ProviderResourceSnapshot.
     * 
     * @param counts the request counts
     * @param timing the timing information
     * @param timestampMs the timestamp in milliseconds
     * @param providerName the provider name
     * @param concurrentRequests the number of concurrent requests
     * @param memoryUsageBytes the memory usage in bytes
     * @param maxMemoryBytes the maximum memory in bytes
     * @param cpuUsagePercentage the CPU usage percentage
     * @param diskUsageBytes the disk usage in bytes
     * @param totalDiskBytes the total disk space in bytes
     * @param networkBytesReceived the network bytes received
     * @param networkBytesSent the network bytes sent
     * @param activeThreads the number of active threads
     * @param maxThreads the maximum number of threads
     * @param lastActivity the last activity timestamp
     */
    public ProviderResourceSnapshot {
        Objects.requireNonNull(counts, "counts cannot be null");
        Objects.requireNonNull(timing, "timing cannot be null");
        Objects.requireNonNull(providerName, "providerName cannot be null");
        Objects.requireNonNull(lastActivity, "lastActivity cannot be null");

        if (timestampMs < 0) {
            throw new IllegalArgumentException("timestampMs must be non-negative");
        }
        if (concurrentRequests < 0) {
            throw new IllegalArgumentException("concurrentRequests must be non-negative");
        }
        if (memoryUsageBytes < 0) {
            throw new IllegalArgumentException("memoryUsageBytes must be non-negative");
        }
        if (maxMemoryBytes < 0) {
            throw new IllegalArgumentException("maxMemoryBytes must be non-negative");
        }
        if (cpuUsagePercentage < 0.0 || cpuUsagePercentage > 100.0) {
            throw new IllegalArgumentException("cpuUsagePercentage must be between 0.0 and 100.0");
        }
        if (diskUsageBytes < 0) {
            throw new IllegalArgumentException("diskUsageBytes must be non-negative");
        }
        if (totalDiskBytes < 0) {
            throw new IllegalArgumentException("totalDiskBytes must be non-negative");
        }
        if (networkBytesReceived < 0) {
            throw new IllegalArgumentException("networkBytesReceived must be non-negative");
        }
        if (networkBytesSent < 0) {
            throw new IllegalArgumentException("networkBytesSent must be non-negative");
        }
        if (activeThreads < 0) {
            throw new IllegalArgumentException("activeThreads must be non-negative");
        }
        if (maxThreads < 0) {
            throw new IllegalArgumentException("maxThreads must be non-negative");
        }
    }

    // CountsMetrics implementation
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

    // LatencyMetrics implementation
    @Override
    public long totalDurationNanos() {
        return timing.totalDurationNanos();
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

    // Provider-specific methods

    /**
     * Get the provider name.
     * 
     * @return the provider name
     */
    public String getProviderName() {
        return providerName;
    }

    /**
     * Get the number of concurrent requests for this provider.
     * 
     * @return the number of concurrent requests
     */
    public long getConcurrentRequests() {
        return concurrentRequests;
    }

    /**
     * Get the last activity timestamp.
     * 
     * @return the last activity timestamp
     */
    public Instant getLastActivity() {
        return lastActivity;
    }

    /**
     * Check if this provider is currently active.
     * 
     * @return true if the provider has recent activity within the last 5 minutes
     */
    public boolean isActive() {
        return lastActivity.isAfter(Instant.now().minusSeconds(300)); // 5 minutes
    }

    /**
     * Get the provider efficiency score (0.0 - 1.0).
     * 
     * @return efficiency score based on success rate and resource utilization
     */
    public double getProviderEfficiency() {
        double successRate = successRate();
        double resourceEfficiency = 1.0
                - (memoryUsagePercentage() + cpuUsagePercentage + threadUsagePercentage()) / 300.0;
        return (successRate + Math.max(0.0, resourceEfficiency)) / 2.0;
    }

    /**
     * Get the resource utilization score (0.0 - 1.0).
     * 
     * @return combined resource utilization score
     */
    public double getResourceUtilization() {
        return (memoryUsagePercentage() + cpuUsagePercentage + diskUsagePercentage() + threadUsagePercentage()) / 400.0;
    }

    /**
     * Check if resource usage is critical.
     * 
     * @return true if any resource usage exceeds 90%
     */
    public boolean isResourceUsageCritical() {
        return memoryUsagePercentage() > 90.0 || cpuUsagePercentage > 90.0 || diskUsagePercentage() > 90.0
                || threadUsagePercentage() > 90.0;
    }

    /**
     * Get the network throughput in bytes per second (approximation).
     * 
     * @return network throughput estimate
     */
    public long getNetworkThroughputBytesPerSecond() {
        // Simple approximation based on total duration
        long totalDurationSeconds = Math.max(1, totalDurationNanos() / 1_000_000_000L);
        return networkThroughputBytesPerSecond() / totalDurationSeconds;
    }

    /**
     * Create a Builder for constructing ProviderResourceSnapshot instances.
     * 
     * @param providerName the provider name
     * @return a new Builder instance
     */
    public static Builder builder(String providerName) {
        return new Builder(providerName);
    }

    /**
     * Builder for creating ProviderResourceSnapshot instances.
     */
    public static final class Builder {
        private final String providerName;
        private Counts counts = new Counts(0, 0, 0);
        private Timing timing = new Timing(0);
        private long timestampMs = System.currentTimeMillis();
        private long concurrentRequests = 0;
        private long memoryUsageBytes = 0;
        private long maxMemoryBytes = 0;
        private double cpuUsagePercentage = 0.0;
        private long diskUsageBytes = 0;
        private long totalDiskBytes = 0;
        private long networkBytesReceived = 0;
        private long networkBytesSent = 0;
        private int activeThreads = 0;
        private int maxThreads = 0;
        private Instant lastActivity = Instant.now();

        private Builder(String providerName) {
            this.providerName = Objects.requireNonNull(providerName, "providerName cannot be null");
        }

        public Builder withCounts(Counts counts) {
            this.counts = Objects.requireNonNull(counts, "counts cannot be null");
            return this;
        }

        public Builder withTiming(Timing timing) {
            this.timing = Objects.requireNonNull(timing, "timing cannot be null");
            return this;
        }

        public Builder withTimestamp(long timestampMs) {
            this.timestampMs = timestampMs;
            return this;
        }

        public Builder withConcurrentRequests(long concurrentRequests) {
            this.concurrentRequests = concurrentRequests;
            return this;
        }

        public Builder withMemoryUsage(long memoryUsageBytes, long maxMemoryBytes) {
            this.memoryUsageBytes = memoryUsageBytes;
            this.maxMemoryBytes = maxMemoryBytes;
            return this;
        }

        public Builder withCpuUsage(double cpuUsagePercentage) {
            this.cpuUsagePercentage = cpuUsagePercentage;
            return this;
        }

        public Builder withDiskUsage(long diskUsageBytes, long totalDiskBytes) {
            this.diskUsageBytes = diskUsageBytes;
            this.totalDiskBytes = totalDiskBytes;
            return this;
        }

        public Builder withNetworkUsage(long networkBytesReceived, long networkBytesSent) {
            this.networkBytesReceived = networkBytesReceived;
            this.networkBytesSent = networkBytesSent;
            return this;
        }

        public Builder withThreadUsage(int activeThreads, int maxThreads) {
            this.activeThreads = activeThreads;
            this.maxThreads = maxThreads;
            return this;
        }

        public Builder withLastActivity(Instant lastActivity) {
            this.lastActivity = Objects.requireNonNull(lastActivity, "lastActivity cannot be null");
            return this;
        }

        public ProviderResourceSnapshot build() {
            return new ProviderResourceSnapshot(counts, timing, timestampMs, providerName, concurrentRequests,
                    memoryUsageBytes, maxMemoryBytes, cpuUsagePercentage, diskUsageBytes, totalDiskBytes,
                    networkBytesReceived, networkBytesSent, activeThreads, maxThreads, lastActivity);
        }
    }
}
