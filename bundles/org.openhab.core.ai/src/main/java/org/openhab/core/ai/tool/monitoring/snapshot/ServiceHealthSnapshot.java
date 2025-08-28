package org.openhab.core.ai.tool.monitoring.snapshot;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.HealthMetrics;
import org.openhab.core.ai.common.monitoring.api.HealthStatus;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;

/**
 * Snapshot implementation for service health metrics.
 * 
 * <p>
 * This class provides an immutable snapshot of service health metrics data,
 * including counts, latency, and health status information. It implements
 * the capability interfaces defined in the common monitoring API for clean,
 * type-safe metrics access.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ServiceHealthSnapshot(String serviceName, long totalRequests, long successfulRequests,
        long failedRequests, long totalResponseTimeNanos, HealthStatus healthStatus, @Nullable String statusMessage,
        @Nullable Map<String, Object> healthIndicators,
        long timestampMs) implements MetricsSnapshot, CountsMetrics, LatencyMetrics, HealthMetrics {

    public ServiceHealthSnapshot {
        // Validation
        Objects.requireNonNull(serviceName, "serviceName");
        Objects.requireNonNull(healthStatus, "healthStatus");
        if (totalRequests < 0) {
            throw new IllegalArgumentException("totalRequests cannot be negative");
        }
        if (successfulRequests < 0) {
            throw new IllegalArgumentException("successfulRequests cannot be negative");
        }
        if (failedRequests < 0) {
            throw new IllegalArgumentException("failedRequests cannot be negative");
        }
        if (totalResponseTimeNanos < 0) {
            throw new IllegalArgumentException("totalResponseTimeNanos cannot be negative");
        }
        if (successfulRequests + failedRequests > totalRequests) {
            throw new IllegalArgumentException("success + failed cannot exceed total requests");
        }

        // Make defensive copy of health indicators if provided
        healthIndicators = healthIndicators != null ? Map.copyOf(healthIndicators) : null;
    }

    // ===== MetricsSnapshot Implementation =====

    @Override
    public long getTimestampMs() {
        return timestampMs;
    }

    // ===== CountsMetrics Implementation =====

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

    // ===== LatencyMetrics Implementation =====

    @Override
    public long totalDurationNanos() {
        return totalResponseTimeNanos;
    }

    // ===== HealthMetrics Implementation =====

    @Override
    public HealthStatus healthStatus() {
        return healthStatus;
    }

    @Override
    public @Nullable String statusMessage() {
        return statusMessage;
    }

    @Override
    public @Nullable Map<String, Object> healthIndicators() {
        return healthIndicators;
    }

    // ===== Service Health Specific Methods =====

    /**
     * Get the service name for this health snapshot.
     * 
     * @return the service name
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Get the average response time in milliseconds.
     * 
     * @return average response time in milliseconds, or 0.0 if no requests
     */
    public double getAverageResponseTimeMs() {
        return averageMs(totalRequests);
    }

    /**
     * Get the total response time in milliseconds.
     * 
     * @return total response time in milliseconds
     */
    public double getTotalResponseTimeMs() {
        return totalDurationMs();
    }

    /**
     * Check if the service is performing well (success rate >= 95%).
     * 
     * @return true if performing well, false otherwise
     */
    public boolean isPerformingWell() {
        return successRate() >= 0.95;
    }

    /**
     * Check if the service response time is acceptable (< 5 seconds average).
     * 
     * @return true if response time is acceptable, false otherwise
     */
    public boolean hasAcceptableResponseTime() {
        return getAverageResponseTimeMs() < 5000.0;
    }

    /**
     * Get throughput (requests per second) if duration data is available.
     * 
     * @return throughput in requests per second, or 0.0 if no duration data
     */
    public double getThroughput() {
        return throughput(totalRequests);
    }

    // ===== Factory Methods =====

    /**
     * Create a service health snapshot from raw data.
     * 
     * @param serviceName the service name
     * @param totalRequests total number of requests
     * @param successfulRequests number of successful requests
     * @param failedRequests number of failed requests
     * @param totalResponseTimeNanos total response time in nanoseconds
     * @param healthStatus current health status
     * @param statusMessage optional status message
     * @param healthIndicators optional health indicators
     * @return new service health snapshot
     */
    public static ServiceHealthSnapshot of(String serviceName, long totalRequests, long successfulRequests,
            long failedRequests, long totalResponseTimeNanos, HealthStatus healthStatus, @Nullable String statusMessage,
            @Nullable Map<String, Object> healthIndicators) {
        return new ServiceHealthSnapshot(serviceName, totalRequests, successfulRequests, failedRequests,
                totalResponseTimeNanos, healthStatus, statusMessage, healthIndicators, System.currentTimeMillis());
    }

    /**
     * Create a service health snapshot with basic metrics.
     * 
     * @param serviceName the service name
     * @param totalRequests total number of requests
     * @param successfulRequests number of successful requests
     * @param totalResponseTimeNanos total response time in nanoseconds
     * @param healthStatus current health status
     * @return new service health snapshot
     */
    public static ServiceHealthSnapshot of(String serviceName, long totalRequests, long successfulRequests,
            long totalResponseTimeNanos, HealthStatus healthStatus) {
        long failedRequests = Math.max(0, totalRequests - successfulRequests);
        return of(serviceName, totalRequests, successfulRequests, failedRequests, totalResponseTimeNanos, healthStatus,
                null, null);
    }

    /**
     * Create an empty service health snapshot.
     * 
     * @param serviceName the service name
     * @return empty service health snapshot with UNKNOWN status
     */
    public static ServiceHealthSnapshot empty(String serviceName) {
        return of(serviceName, 0L, 0L, 0L, 0L, HealthStatus.UNKNOWN, "No data available", null);
    }

    /**
     * Create an offline service health snapshot.
     * 
     * @param serviceName the service name
     * @return offline service health snapshot
     */
    public static ServiceHealthSnapshot offline(String serviceName) {
        return of(serviceName, 0L, 0L, 0L, 0L, HealthStatus.OFFLINE, "Service is offline", null);
    }

    /**
     * Create a builder for constructing ServiceHealthSnapshot instances.
     * 
     * @param serviceName the service name
     * @return a new builder instance
     */
    public static Builder builder(String serviceName) {
        return new Builder(serviceName);
    }

    // ===== Builder Pattern =====

    /**
     * Builder for ServiceHealthSnapshot.
     */
    public static final class Builder {
        private final String serviceName;
        private long totalRequests = 0L;
        private long successfulRequests = 0L;
        private long failedRequests = 0L;
        private long totalResponseTimeNanos = 0L;
        private HealthStatus healthStatus = HealthStatus.UNKNOWN;
        private @Nullable String statusMessage;
        private @Nullable Map<String, Object> healthIndicators;

        private Builder(String serviceName) {
            this.serviceName = Objects.requireNonNull(serviceName, "serviceName");
        }

        /**
         * Set the total number of requests.
         * 
         * @param totalRequests total requests
         * @return this builder
         */
        public Builder withTotalRequests(long totalRequests) {
            this.totalRequests = totalRequests;
            return this;
        }

        /**
         * Set the number of successful requests.
         * 
         * @param successfulRequests successful requests
         * @return this builder
         */
        public Builder withSuccessfulRequests(long successfulRequests) {
            this.successfulRequests = successfulRequests;
            return this;
        }

        /**
         * Set the number of failed requests.
         * 
         * @param failedRequests failed requests
         * @return this builder
         */
        public Builder withFailedRequests(long failedRequests) {
            this.failedRequests = failedRequests;
            return this;
        }

        /**
         * Set counts from total and successful requests.
         * 
         * @param total total requests
         * @param successful successful requests
         * @return this builder
         */
        public Builder withCounts(long total, long successful) {
            this.totalRequests = total;
            this.successfulRequests = successful;
            this.failedRequests = Math.max(0, total - successful);
            return this;
        }

        /**
         * Set the total response time in nanoseconds.
         * 
         * @param totalResponseTimeNanos total response time
         * @return this builder
         */
        public Builder withTotalResponseTimeNanos(long totalResponseTimeNanos) {
            this.totalResponseTimeNanos = totalResponseTimeNanos;
            return this;
        }

        /**
         * Set the total response time in milliseconds.
         * 
         * @param totalResponseTimeMs total response time in milliseconds
         * @return this builder
         */
        public Builder withTotalResponseTimeMs(double totalResponseTimeMs) {
            this.totalResponseTimeNanos = (long) (totalResponseTimeMs * 1_000_000.0);
            return this;
        }

        /**
         * Set the health status.
         * 
         * @param healthStatus health status
         * @return this builder
         */
        public Builder withHealthStatus(HealthStatus healthStatus) {
            this.healthStatus = healthStatus;
            return this;
        }

        /**
         * Set the status message.
         * 
         * @param statusMessage status message
         * @return this builder
         */
        public Builder withStatusMessage(@Nullable String statusMessage) {
            this.statusMessage = statusMessage;
            return this;
        }

        /**
         * Set health indicators.
         * 
         * @param healthIndicators health indicators
         * @return this builder
         */
        public Builder withHealthIndicators(@Nullable Map<String, Object> healthIndicators) {
            this.healthIndicators = healthIndicators;
            return this;
        }

        /**
         * Build the ServiceHealthSnapshot.
         * 
         * @return the new snapshot instance
         */
        public ServiceHealthSnapshot build() {
            return new ServiceHealthSnapshot(serviceName, totalRequests, successfulRequests, failedRequests,
                    totalResponseTimeNanos, healthStatus, statusMessage, healthIndicators, System.currentTimeMillis());
        }
    }
}
