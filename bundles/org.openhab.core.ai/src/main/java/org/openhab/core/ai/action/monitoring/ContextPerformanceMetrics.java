package org.openhab.core.ai.action.monitoring;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.base.AbstractStatistics;

/**
 * Consolidated context performance metrics that extends the unified monitoring framework.
 *
 * <p>
 * This class provides comprehensive performance metrics for context building operations
 * including cache performance, build times, and context-specific analytics.
 * It implements CountsMetrics and LatencyMetrics capability interfaces.
 * </p>
 *
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ContextPerformanceMetrics extends AbstractStatistics implements CountsMetrics, LatencyMetrics {

    private final long totalContextsBuilt;
    private final long cacheHits;
    private final long cacheMisses;
    private final double averageBuildTime;
    private final double cacheHitRate;
    private final long totalProcessingTime;

    /**
     * Create a new ContextPerformanceMetrics instance.
     *
     * @param id unique identifier for this metrics instance
     * @param timestamp timestamp when metrics were collected
     * @param totalOperations total number of context operations
     * @param successfulOperations number of successful operations
     * @param failedOperations number of failed operations
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param lastOperationTime timestamp of last operation
     * @param totalContextsBuilt total number of contexts built
     * @param cacheHits number of cache hits
     * @param cacheMisses number of cache misses
     * @param averageBuildTime average build time in milliseconds
     * @param cacheHitRate cache hit rate as a percentage
     * @param data additional monitoring data
     */
    public ContextPerformanceMetrics(String id, Instant timestamp, long totalOperations, long successfulOperations,
            long failedOperations, long totalProcessingTime, double averageResponseTime,
            @Nullable Instant lastOperationTime, long totalContextsBuilt, long cacheHits, long cacheMisses,
            double averageBuildTime, double cacheHitRate, @Nullable Map<String, Object> data) {
        super(id, timestamp, "action", "context-performance", "Context performance metrics", data, totalOperations,
                successfulOperations, failedOperations, null, null, null);
        this.totalContextsBuilt = totalContextsBuilt;
        this.cacheHits = cacheHits;
        this.cacheMisses = cacheMisses;
        this.averageBuildTime = averageBuildTime;
        this.cacheHitRate = cacheHitRate;
        this.totalProcessingTime = totalProcessingTime;
    }

    /**
     * Create a new ContextPerformanceMetrics instance with current timestamp.
     *
     * @param id unique identifier for this metrics instance
     * @param totalOperations total number of context operations
     * @param successfulOperations number of successful operations
     * @param failedOperations number of failed operations
     * @param totalProcessingTime total processing time in nanoseconds
     * @param averageResponseTime average response time in milliseconds
     * @param totalContextsBuilt total number of contexts built
     * @param cacheHits number of cache hits
     * @param cacheMisses number of cache misses
     * @param averageBuildTime average build time in milliseconds
     * @param cacheHitRate cache hit rate as a percentage
     */
    public ContextPerformanceMetrics(String id, long totalOperations, long successfulOperations, long failedOperations,
            long totalProcessingTime, double averageResponseTime, long totalContextsBuilt, long cacheHits,
            long cacheMisses, double averageBuildTime, double cacheHitRate) {
        this(id, Instant.now(), totalOperations, successfulOperations, failedOperations, totalProcessingTime,
                averageResponseTime, null, totalContextsBuilt, cacheHits, cacheMisses, averageBuildTime, cacheHitRate,
                null);
    }

    /**
     * Get the total number of contexts built.
     *
     * @return total contexts built
     */
    public long getTotalContextsBuilt() {
        return totalContextsBuilt;
    }

    /**
     * Get the number of cache hits.
     *
     * @return cache hits
     */
    public long getCacheHits() {
        return cacheHits;
    }

    /**
     * Get the number of cache misses.
     *
     * @return cache misses
     */
    public long getCacheMisses() {
        return cacheMisses;
    }

    /**
     * Get the average build time in milliseconds.
     *
     * @return average build time
     */
    public double getAverageBuildTime() {
        return averageBuildTime;
    }

    /**
     * Get the cache hit rate as a percentage.
     *
     * @return cache hit rate
     */
    public double getCacheHitRate() {
        return cacheHitRate;
    }

    /**
     * Get the total processing time in nanoseconds.
     *
     * @return total processing time
     */
    public long getTotalProcessingTime() {
        return totalProcessingTime;
    }

    // CountsMetrics interface implementation
    @Override
    public long total() {
        return getTotalCount();
    }

    @Override
    public long success() {
        return getSuccessCount();
    }

    @Override
    public long failure() {
        return getFailureCount();
    }

    // LatencyMetrics interface implementation
    @Override
    public long totalDurationNanos() {
        return getTotalProcessingTime();
    }

    /**
     * Get the total cache operations (hits + misses).
     *
     * @return total cache operations
     */
    public long getTotalCacheOperations() {
        return cacheHits + cacheMisses;
    }

    /**
     * Get the context building efficiency score.
     *
     * @return context building efficiency score between 0.0 and 1.0
     */
    public double getContextBuildingEfficiency() {
        double successRate = successRate();
        double cacheEfficiency = getTotalCacheOperations() > 0 ? cacheHitRate / 100.0 : 1.0;
        double buildTimeScore = averageBuildTime < 100 ? 1.0
                : averageBuildTime < 500 ? 0.8 : averageBuildTime < 1000 ? 0.6 : 0.4;

        return (successRate * 0.4) + (cacheEfficiency * 0.4) + (buildTimeScore * 0.2);
    }

    /**
     * Check if context performance is performing well (high cache hit rate, fast builds).
     *
     * @return true if context performance is performing well
     */
    public boolean isPerformingWell() {
        return successRate() > 0.9 && cacheHitRate > 80.0 && averageBuildTime < 500;
    }

    /**
     * Check if there are critical context issues (low cache hit rate or slow builds).
     *
     * @return true if there are critical context issues
     */
    public boolean hasCriticalIssues() {
        return successRate() < 0.8 || cacheHitRate < 50.0 || averageBuildTime > 2000;
    }

    /**
     * Builder for ContextPerformanceMetrics.
     */
    public static final class Builder {
        private String id = "";
        private long totalOperations = 0;
        private long successfulOperations = 0;
        private long failedOperations = 0;
        private long totalProcessingTime = 0;
        private double averageResponseTime = 0.0;
        private long totalContextsBuilt = 0;
        private long cacheHits = 0;
        private long cacheMisses = 0;
        private double averageBuildTime = 0.0;
        private double cacheHitRate = 0.0;
        private @Nullable Map<String, Object> data;

        public Builder() {
            // Default constructor
        }

        public Builder(ContextPerformanceMetrics source) {
            this.id = source.getId();
            this.totalOperations = source.getTotalCount();
            this.successfulOperations = source.getSuccessCount();
            this.failedOperations = source.getFailureCount();
            this.totalProcessingTime = source.getTotalProcessingTime();
            this.averageResponseTime = 0.0; // Not available in AbstractStatistics
            this.totalContextsBuilt = source.getTotalContextsBuilt();
            this.cacheHits = source.getCacheHits();
            this.cacheMisses = source.getCacheMisses();
            this.averageBuildTime = source.getAverageBuildTime();
            this.cacheHitRate = source.getCacheHitRate();
            this.data = source.getData();
        }

        public Builder withId(String id) {
            this.id = Objects.requireNonNull(id, "id");
            return this;
        }

        public Builder withTotalOperations(long totalOperations) {
            this.totalOperations = totalOperations;
            return this;
        }

        public Builder withSuccessfulOperations(long successfulOperations) {
            this.successfulOperations = successfulOperations;
            return this;
        }

        public Builder withFailedOperations(long failedOperations) {
            this.failedOperations = failedOperations;
            return this;
        }

        public Builder withTotalProcessingTime(long totalProcessingTime) {
            this.totalProcessingTime = totalProcessingTime;
            return this;
        }

        public Builder withAverageResponseTime(double averageResponseTime) {
            this.averageResponseTime = averageResponseTime;
            return this;
        }

        public Builder withTotalContextsBuilt(long totalContextsBuilt) {
            this.totalContextsBuilt = totalContextsBuilt;
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

        public Builder withAverageBuildTime(double averageBuildTime) {
            this.averageBuildTime = averageBuildTime;
            return this;
        }

        public Builder withCacheHitRate(double cacheHitRate) {
            this.cacheHitRate = cacheHitRate;
            return this;
        }

        public Builder withData(@Nullable Map<String, Object> data) {
            this.data = data;
            return this;
        }

        public ContextPerformanceMetrics build() {
            validate();
            return new ContextPerformanceMetrics(id, Instant.now(), totalOperations, successfulOperations,
                    failedOperations, totalProcessingTime, averageResponseTime, null, totalContextsBuilt, cacheHits,
                    cacheMisses, averageBuildTime, cacheHitRate, data);
        }

        private void validate() {
            if (id.isBlank()) {
                throw new IllegalArgumentException("id must not be blank");
            }
            if (totalOperations < 0) {
                throw new IllegalArgumentException("totalOperations must be non-negative");
            }
            if (successfulOperations < 0) {
                throw new IllegalArgumentException("successfulOperations must be non-negative");
            }
            if (failedOperations < 0) {
                throw new IllegalArgumentException("failedOperations must be non-negative");
            }
            if (totalProcessingTime < 0) {
                throw new IllegalArgumentException("totalProcessingTime must be non-negative");
            }
            if (averageResponseTime < 0) {
                throw new IllegalArgumentException("averageResponseTime must be non-negative");
            }
            if (totalContextsBuilt < 0) {
                throw new IllegalArgumentException("totalContextsBuilt must be non-negative");
            }
            if (cacheHits < 0) {
                throw new IllegalArgumentException("cacheHits must be non-negative");
            }
            if (cacheMisses < 0) {
                throw new IllegalArgumentException("cacheMisses must be non-negative");
            }
            if (averageBuildTime < 0) {
                throw new IllegalArgumentException("averageBuildTime must be non-negative");
            }
            if (cacheHitRate < 0 || cacheHitRate > 100) {
                throw new IllegalArgumentException("cacheHitRate must be between 0 and 100");
            }
            if (totalOperations != successfulOperations + failedOperations) {
                throw new IllegalArgumentException(
                        "totalOperations must equal successfulOperations + failedOperations");
            }
        }
    }

    /**
     * Create a new builder for ContextPerformanceMetrics.
     *
     * @return a new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Create a new builder for ContextPerformanceMetrics from an existing instance.
     *
     * @return a new builder instance initialized with this instance's values
     */
    public Builder toBuilder() {
        return new Builder(this);
    }
}
