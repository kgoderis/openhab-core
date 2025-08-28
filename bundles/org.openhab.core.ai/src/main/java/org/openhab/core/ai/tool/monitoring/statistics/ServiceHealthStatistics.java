package org.openhab.core.ai.tool.monitoring.statistics;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.HealthMetrics;
import org.openhab.core.ai.common.monitoring.api.HealthStatus;
import org.openhab.core.ai.common.monitoring.api.PercentileMetrics;
import org.openhab.core.ai.common.monitoring.api.TrendMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;

/**
 * Statistics class for service health operations.
 * 
 * <p>
 * This class provides computed statistics and insights about service health
 * operations derived from metrics data over time ranges. It implements
 * capability interfaces for clean, type-safe statistics access including
 * trends, percentiles, and health analysis.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ServiceHealthStatistics(String serviceName, long totalRequests, long successfulRequests,
        long failedRequests, long totalResponseTimeNanos, HealthStatus currentHealthStatus,
        @Nullable String statusMessage, @Nullable Map<String, Object> healthIndicators, Duration timeRange,
        double previousSuccessRate, long previousTotalRequests,
        long timestampMs) implements StatisticsSnapshot, CountsMetrics, TrendMetrics, PercentileMetrics, HealthMetrics {

    public ServiceHealthStatistics {
        // Validation
        Objects.requireNonNull(serviceName, "serviceName");
        Objects.requireNonNull(currentHealthStatus, "currentHealthStatus");
        Objects.requireNonNull(timeRange, "timeRange");
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
        if (previousSuccessRate < 0.0 || previousSuccessRate > 1.0) {
            throw new IllegalArgumentException("previousSuccessRate must be between 0.0 and 1.0");
        }
        if (previousTotalRequests < 0) {
            throw new IllegalArgumentException("previousTotalRequests cannot be negative");
        }

        // Make defensive copy of health indicators if provided
        healthIndicators = healthIndicators != null ? Map.copyOf(healthIndicators) : null;
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

    // ===== TrendMetrics Implementation =====

    @Override
    public double trendPercentage() {
        // Calculate trend based on success rate change from previous period
        double currentSuccessRate = successRate();
        if (previousTotalRequests == 0) {
            return 0.0; // No previous data
        }
        return ((currentSuccessRate - previousSuccessRate) / previousSuccessRate) * 100.0;
    }

    @Override
    public String trendDirection() {
        double trend = trendPercentage();
        if (trend > 1.0) {
            return "increasing";
        }
        if (trend < -1.0) {
            return "decreasing";
        }
        return "stable";
    }

    @Override
    public double changeRate() {
        // Calculate change rate per day
        double trendPercent = trendPercentage();
        long days = Math.max(1, timeRange.toDays()); // Avoid division by zero
        return trendPercent / days;
    }

    // ===== PercentileMetrics Implementation =====

    @Override
    public double percentile50() {
        // For service health statistics, percentiles represent response time distribution
        // This is a simplified implementation - in practice, you'd want actual distribution data
        return getAverageResponseTimeMs();
    }

    @Override
    public double percentile90() {
        // Estimated 90th percentile - typically 1.5x the average for normal distributions
        return percentile50() * 1.5;
    }

    @Override
    public double percentile95() {
        // Estimated 95th percentile - typically 2x the average for normal distributions
        return percentile50() * 2.0;
    }

    @Override
    public double percentile99() {
        // Estimated 99th percentile - typically 3x the average for normal distributions
        return percentile50() * 3.0;
    }

    // ===== HealthMetrics Implementation =====

    @Override
    public HealthStatus healthStatus() {
        return currentHealthStatus;
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
     * Get the service name for this health statistics.
     * 
     * @return the service name
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * Get the time range for these statistics.
     * 
     * @return the time range
     */
    public Duration getTimeRange() {
        return timeRange;
    }

    /**
     * Get the average response time in milliseconds.
     * 
     * @return average response time in milliseconds, or 0.0 if no requests
     */
    public double getAverageResponseTimeMs() {
        if (totalRequests == 0) {
            return 0.0;
        }
        return (double) totalResponseTimeNanos / totalRequests / 1_000_000.0;
    }

    /**
     * Get the total response time in milliseconds.
     * 
     * @return total response time in milliseconds
     */
    public double getTotalResponseTimeMs() {
        return (double) totalResponseTimeNanos / 1_000_000.0;
    }

    /**
     * Get the current success rate as a percentage.
     * 
     * @return success rate percentage (0.0 to 100.0)
     */
    public double getCurrentSuccessRate() {
        return successRate() * 100.0;
    }

    /**
     * Get the previous success rate as a percentage.
     * 
     * @return previous success rate percentage (0.0 to 100.0)
     */
    public double getPreviousSuccessRate() {
        return previousSuccessRate * 100.0;
    }

    /**
     * Get the success rate improvement in percentage points.
     * 
     * @return success rate improvement (positive for improvement, negative for degradation)
     */
    public double getSuccessRateImprovement() {
        return (successRate() - previousSuccessRate) * 100.0;
    }

    /**
     * Get the request volume change compared to previous period.
     * 
     * @return request volume change percentage
     */
    public double getRequestVolumeChange() {
        if (previousTotalRequests == 0) {
            return totalRequests > 0 ? Double.POSITIVE_INFINITY : 0.0;
        }
        return ((double) (totalRequests - previousTotalRequests) / previousTotalRequests) * 100.0;
    }

    /**
     * Get throughput (requests per second) for the time range.
     * 
     * @return throughput in requests per second
     */
    public double getThroughput() {
        double timeRangeSeconds = timeRange.toNanos() / 1_000_000_000.0;
        return timeRangeSeconds > 0 ? totalRequests / timeRangeSeconds : 0.0;
    }

    /**
     * Check if the service is performing well (success rate >= 95% and trending stable/up).
     * 
     * @return true if performing well, false otherwise
     */
    public boolean isPerformingWell() {
        return successRate() >= 0.95 && trendPercentage() >= -5.0; // Allow slight degradation
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
     * Check if the service is degrading (success rate trending down > 10%).
     * 
     * @return true if degrading, false otherwise
     */
    public boolean isDegrading() {
        return trendPercentage() < -10.0;
    }

    /**
     * Check if the service is improving (success rate trending up > 5%).
     * 
     * @return true if improving, false otherwise
     */
    public boolean isImproving() {
        return trendPercentage() > 5.0;
    }

    /**
     * Get a health score (0.0 to 1.0) based on success rate and response time.
     * 
     * @return health score
     */
    public double getHealthScore() {
        double successWeight = 0.7;
        double responseTimeWeight = 0.3;

        double successScore = successRate();
        double responseTimeScore = Math.max(0.0, 1.0 - (getAverageResponseTimeMs() / 10000.0)); // 10s = 0 score

        return (successScore * successWeight) + (responseTimeScore * responseTimeWeight);
    }

    // ===== Factory Methods =====

    /**
     * Create service health statistics from raw data.
     * 
     * @param serviceName the service name
     * @param totalRequests total number of requests
     * @param successfulRequests number of successful requests
     * @param failedRequests number of failed requests
     * @param totalResponseTimeNanos total response time in nanoseconds
     * @param currentHealthStatus current health status
     * @param statusMessage optional status message
     * @param healthIndicators optional health indicators
     * @param timeRange time range for statistics
     * @param previousSuccessRate success rate from previous period
     * @param previousTotalRequests total requests from previous period
     * @return new service health statistics
     */
    public static ServiceHealthStatistics of(String serviceName, long totalRequests, long successfulRequests,
            long failedRequests, long totalResponseTimeNanos, HealthStatus currentHealthStatus,
            @Nullable String statusMessage, @Nullable Map<String, Object> healthIndicators, Duration timeRange,
            double previousSuccessRate, long previousTotalRequests) {
        return new ServiceHealthStatistics(serviceName, totalRequests, successfulRequests, failedRequests,
                totalResponseTimeNanos, currentHealthStatus, statusMessage, healthIndicators, timeRange,
                previousSuccessRate, previousTotalRequests, System.currentTimeMillis());
    }

    /**
     * Create service health statistics with basic metrics.
     * 
     * @param serviceName the service name
     * @param totalRequests total number of requests
     * @param successfulRequests number of successful requests
     * @param totalResponseTimeNanos total response time in nanoseconds
     * @param currentHealthStatus current health status
     * @param timeRange time range for statistics
     * @param previousSuccessRate success rate from previous period
     * @param previousTotalRequests total requests from previous period
     * @return new service health statistics
     */
    public static ServiceHealthStatistics of(String serviceName, long totalRequests, long successfulRequests,
            long totalResponseTimeNanos, HealthStatus currentHealthStatus, Duration timeRange,
            double previousSuccessRate, long previousTotalRequests) {
        long failedRequests = Math.max(0, totalRequests - successfulRequests);
        return of(serviceName, totalRequests, successfulRequests, failedRequests, totalResponseTimeNanos,
                currentHealthStatus, null, null, timeRange, previousSuccessRate, previousTotalRequests);
    }

    /**
     * Create empty service health statistics.
     * 
     * @param serviceName the service name
     * @param timeRange time range for statistics
     * @return empty service health statistics with UNKNOWN status
     */
    public static ServiceHealthStatistics empty(String serviceName, Duration timeRange) {
        return of(serviceName, 0L, 0L, 0L, 0L, HealthStatus.UNKNOWN, "No data available", null, timeRange, 0.0, 0L);
    }

    /**
     * Create service health statistics from current and previous data.
     * 
     * @param serviceName the service name
     * @param currentData current period data
     * @param previousData previous period data
     * @param timeRange time range for statistics
     * @return new service health statistics
     */
    public static ServiceHealthStatistics fromPeriods(String serviceName, ServiceHealthData currentData,
            ServiceHealthData previousData, Duration timeRange) {
        double previousSuccessRate = previousData.totalRequests() > 0
                ? (double) previousData.successfulRequests() / previousData.totalRequests()
                : 0.0;

        return of(serviceName, currentData.totalRequests(), currentData.successfulRequests(),
                currentData.failedRequests(), currentData.totalResponseTimeNanos(), currentData.healthStatus(),
                currentData.statusMessage(), currentData.healthIndicators(), timeRange, previousSuccessRate,
                previousData.totalRequests());
    }

    /**
     * Data class for holding service health data.
     * 
     * @param totalRequests total requests
     * @param successfulRequests successful requests
     * @param failedRequests failed requests
     * @param totalResponseTimeNanos total response time in nanoseconds
     * @param healthStatus health status
     * @param statusMessage status message
     * @param healthIndicators health indicators
     */
    public record ServiceHealthData(long totalRequests, long successfulRequests, long failedRequests,
            long totalResponseTimeNanos, HealthStatus healthStatus, @Nullable String statusMessage,
            @Nullable Map<String, Object> healthIndicators) {

        public ServiceHealthData {
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
    }
}
