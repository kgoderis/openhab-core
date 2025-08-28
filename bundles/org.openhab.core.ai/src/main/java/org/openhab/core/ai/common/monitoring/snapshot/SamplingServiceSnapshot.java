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

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.Counts;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.api.SamplingMetrics;
import org.openhab.core.ai.common.monitoring.api.Timing;

/**
 * Immutable snapshot of sampling service metrics data.
 * 
 * This class provides a thread-safe, immutable view of sampling service-related metrics
 * including counts, latency, and sampling-specific statistics for human-in-the-loop operations.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public record SamplingServiceSnapshot(Counts counts, Timing timing, long totalSamplesGenerated, long cacheHits,
        long cacheMisses, int cacheSize, String modelType, String modelVersion, long approvedRequests,
        long rejectedRequests, long pendingRequests,
        long timestampMs) implements MetricsSnapshot, CountsMetrics, LatencyMetrics, SamplingMetrics {

    public SamplingServiceSnapshot {
        Objects.requireNonNull(counts, "counts");
        Objects.requireNonNull(timing, "timing");
        Objects.requireNonNull(modelType, "modelType");
        Objects.requireNonNull(modelVersion, "modelVersion");
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
    public double successRate() {
        return CountsMetrics.super.successRate();
    }

    @Override
    public long totalDurationNanos() {
        return timing.totalDurationNanos();
    }

    /**
     * Get the average response time in milliseconds.
     *
     * @return average response time in milliseconds
     */
    public double averageMs() {
        return LatencyMetrics.super.averageMs(total());
    }

    // SamplingMetrics implementation
    @Override
    public long totalSamplesGenerated() {
        return totalSamplesGenerated;
    }

    @Override
    public long cacheHits() {
        return cacheHits;
    }

    @Override
    public long cacheMisses() {
        return cacheMisses;
    }

    @Override
    public int cacheSize() {
        return cacheSize;
    }

    @Override
    public String modelType() {
        return modelType;
    }

    @Override
    public String modelVersion() {
        return modelVersion;
    }

    // Additional sampling service specific metrics

    /**
     * Get the number of approved sampling requests.
     * 
     * @return approved requests count
     */
    public long approvedRequests() {
        return approvedRequests;
    }

    /**
     * Get the number of rejected sampling requests.
     * 
     * @return rejected requests count
     */
    public long rejectedRequests() {
        return rejectedRequests;
    }

    /**
     * Get the number of pending sampling requests.
     * 
     * @return pending requests count
     */
    public long pendingRequests() {
        return pendingRequests;
    }

    /**
     * Calculate the approval rate as a percentage.
     * 
     * @return approval rate between 0.0 and 1.0, or 0.0 if no requests processed
     */
    public double approvalRate() {
        long totalProcessed = approvedRequests + rejectedRequests;
        return totalProcessed > 0 ? (double) approvedRequests / totalProcessed : 0.0;
    }

    /**
     * Calculate the approval rate as a percentage (0-100).
     * 
     * @return approval rate percentage between 0.0 and 100.0, or 0.0 if no requests processed
     */
    public double approvalRatePercentage() {
        return approvalRate() * 100.0;
    }

    /**
     * Calculate the rejection rate as a percentage.
     * 
     * @return rejection rate between 0.0 and 1.0, or 0.0 if no requests processed
     */
    public double rejectionRate() {
        long totalProcessed = approvedRequests + rejectedRequests;
        return totalProcessed > 0 ? (double) rejectedRequests / totalProcessed : 0.0;
    }

    /**
     * Calculate the rejection rate as a percentage (0-100).
     * 
     * @return rejection rate percentage between 0.0 and 100.0, or 0.0 if no requests processed
     */
    public double rejectionRatePercentage() {
        return rejectionRate() * 100.0;
    }

    /**
     * Get the total number of requests (approved + rejected + pending).
     * 
     * @return total requests count
     */
    public long totalRequests() {
        return approvedRequests + rejectedRequests + pendingRequests;
    }

    /**
     * Calculate the pending ratio as a percentage.
     * 
     * @return pending ratio between 0.0 and 1.0, or 0.0 if no requests
     */
    public double pendingRatio() {
        long totalReqs = totalRequests();
        return totalReqs > 0 ? (double) pendingRequests / totalReqs : 0.0;
    }

    /**
     * Calculate the pending ratio as a percentage (0-100).
     * 
     * @return pending ratio percentage between 0.0 and 100.0, or 0.0 if no requests
     */
    public double pendingRatioPercentage() {
        return pendingRatio() * 100.0;
    }

    /**
     * Check if the sampling service is performing efficiently.
     * 
     * @return true if approval rate is reasonable and response time is acceptable
     */
    public boolean isEfficient() {
        return approvalRatePercentage() >= 70.0 && averageMs() < 1000.0 && pendingRatioPercentage() < 30.0;
    }

    /**
     * Get efficiency score (0.0-1.0) based on approval rate, response time, and pending ratio.
     * 
     * @return efficiency score combining multiple performance indicators
     */
    public double efficiencyScore() {
        double approvalScore = Math.min(1.0, approvalRate());
        double responseScore = Math.min(1.0, 1000.0 / Math.max(1.0, averageMs()));
        double pendingScore = Math.max(0.0, 1.0 - pendingRatio());
        return (approvalScore + responseScore + pendingScore) / 3.0;
    }

    /**
     * Calculate human-in-the-loop throughput (requests processed per second).
     * 
     * @return throughput in processed requests per second, or 0.0 if no duration
     */
    public double processingThroughput() {
        long processed = approvedRequests + rejectedRequests;
        return throughput(processed);
    }

    /**
     * Builder for SamplingServiceSnapshot.
     */
    public static final class Builder {
        private Counts counts = new Counts(0, 0, 0);
        private Timing timing = new Timing(0);
        private long totalSamplesGenerated = 0L;
        private long cacheHits = 0L;
        private long cacheMisses = 0L;
        private int cacheSize = 0;
        private String modelType = "";
        private String modelVersion = "";
        private long approvedRequests = 0L;
        private long rejectedRequests = 0L;
        private long pendingRequests = 0L;
        private long timestampMs = System.currentTimeMillis();

        public Builder() {
            // Default constructor
        }

        public Builder(SamplingServiceSnapshot source) {
            this.counts = source.counts;
            this.timing = source.timing;
            this.totalSamplesGenerated = source.totalSamplesGenerated;
            this.cacheHits = source.cacheHits;
            this.cacheMisses = source.cacheMisses;
            this.cacheSize = source.cacheSize;
            this.modelType = source.modelType;
            this.modelVersion = source.modelVersion;
            this.approvedRequests = source.approvedRequests;
            this.rejectedRequests = source.rejectedRequests;
            this.pendingRequests = source.pendingRequests;
            this.timestampMs = source.timestampMs;
        }

        public Builder withCounts(Counts counts) {
            this.counts = Objects.requireNonNull(counts, "counts");
            return this;
        }

        public Builder withTiming(Timing timing) {
            this.timing = Objects.requireNonNull(timing, "timing");
            return this;
        }

        public Builder withTotalSamplesGenerated(long totalSamplesGenerated) {
            this.totalSamplesGenerated = totalSamplesGenerated;
            return this;
        }

        public Builder withCacheHits(long cacheHits) {
            this.cacheHits = cacheHits;
            return this;
        }

        public Builder withCacheMisses(long cacheMisses) {
            this.cacheMisses = cacheMisses;
            return this;
        }

        public Builder withCacheSize(int cacheSize) {
            this.cacheSize = cacheSize;
            return this;
        }

        public Builder withModelType(String modelType) {
            this.modelType = Objects.requireNonNull(modelType, "modelType");
            return this;
        }

        public Builder withModelVersion(String modelVersion) {
            this.modelVersion = Objects.requireNonNull(modelVersion, "modelVersion");
            return this;
        }

        public Builder withApprovedRequests(long approvedRequests) {
            this.approvedRequests = approvedRequests;
            return this;
        }

        public Builder withRejectedRequests(long rejectedRequests) {
            this.rejectedRequests = rejectedRequests;
            return this;
        }

        public Builder withPendingRequests(long pendingRequests) {
            this.pendingRequests = pendingRequests;
            return this;
        }

        public Builder withTimestampMs(long timestampMs) {
            this.timestampMs = timestampMs;
            return this;
        }

        public SamplingServiceSnapshot build() {
            validate();
            return new SamplingServiceSnapshot(counts, timing, totalSamplesGenerated, cacheHits, cacheMisses, cacheSize,
                    modelType, modelVersion, approvedRequests, rejectedRequests, pendingRequests, timestampMs);
        }

        private void validate() {
            if (counts.total() < 0) {
                throw new IllegalArgumentException("total count must be non-negative");
            }
            if (counts.success() < 0) {
                throw new IllegalArgumentException("success count must be non-negative");
            }
            if (counts.failure() < 0) {
                throw new IllegalArgumentException("failure count must be non-negative");
            }
            if (timing.totalDurationNanos() < 0) {
                throw new IllegalArgumentException("total duration must be non-negative");
            }
            if (totalSamplesGenerated < 0) {
                throw new IllegalArgumentException("total samples generated must be non-negative");
            }
            if (cacheHits < 0) {
                throw new IllegalArgumentException("cache hits must be non-negative");
            }
            if (cacheMisses < 0) {
                throw new IllegalArgumentException("cache misses must be non-negative");
            }
            if (cacheSize < 0) {
                throw new IllegalArgumentException("cache size must be non-negative");
            }
            if (approvedRequests < 0) {
                throw new IllegalArgumentException("approved requests must be non-negative");
            }
            if (rejectedRequests < 0) {
                throw new IllegalArgumentException("rejected requests must be non-negative");
            }
            if (pendingRequests < 0) {
                throw new IllegalArgumentException("pending requests must be non-negative");
            }
        }
    }

    /**
     * Create a new builder for SamplingServiceSnapshot.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a new builder for SamplingServiceSnapshot from an existing instance.
     *
     * @return a new builder instance initialized with this instance's values
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public String toString() {
        return new StringBuilder("SamplingServiceSnapshot{").append("total=").append(total()).append(", success=")
                .append(success()).append(", failure=").append(failure()).append(", successRate=")
                .append(String.format("%.2f%%", successRatePercentage())).append(", avgLatency=")
                .append(String.format("%.2fms", averageMs())).append(", approved=").append(approvedRequests)
                .append(", rejected=").append(rejectedRequests).append(", pending=").append(pendingRequests)
                .append(", approvalRate=").append(String.format("%.2f%%", approvalRatePercentage()))
                .append(", efficiency=").append(String.format("%.3f", efficiencyScore())).append(", cacheHitRate=")
                .append(String.format("%.2f%%", cacheEfficiency())).append(", timestamp=").append(timestampMs)
                .append("}").toString();
    }

    /**
     * Create a concise summary string for logging.
     * 
     * @return concise summary string
     */
    public String toSummary() {
        return String.format("Sampling[%d reqs, %.1f%% approved, %.1fms avg, %.1f%% efficiency]", totalRequests(),
                approvalRatePercentage(), averageMs(), efficiencyScore() * 100.0);
    }
}
