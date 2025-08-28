package org.openhab.core.ai.common.monitoring.snapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.Counts;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.HealthMetrics;
import org.openhab.core.ai.common.monitoring.api.HealthStatus;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;
import org.openhab.core.ai.common.monitoring.api.Timing;

/**
 * Snapshot of tool health monitor metrics at a specific point in time.
 * 
 * This class provides real-time health monitoring metrics for tools and providers
 * implementing CountsMetrics, LatencyMetrics, and HealthMetrics interfaces for
 * clean, type-safe access to health data.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record ToolHealthMonitorSnapshot(Counts counts, Timing timing, long timestampMs, HealthStatus healthStatus,
        String statusMessage, CircuitBreakerState breakerState, long consecutiveFailures,
        long lastHealthCheckTime) implements MetricsSnapshot, CountsMetrics, LatencyMetrics, HealthMetrics {

    public ToolHealthMonitorSnapshot {
        Objects.requireNonNull(counts, "counts");
        Objects.requireNonNull(timing, "timing");
        Objects.requireNonNull(healthStatus, "healthStatus");
        Objects.requireNonNull(statusMessage, "statusMessage");
        Objects.requireNonNull(breakerState, "breakerState");
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

    /**
     * Get the average response time in milliseconds.
     *
     * @return average response time in milliseconds
     */
    public double averageMs() {
        return LatencyMetrics.super.averageMs(total());
    }

    /**
     * Calculate operations per second based on total operations and total duration.
     * 
     * @return operations per second, or 0.0 if no operations or duration
     */
    public double operationsPerSecond() {
        if (total() == 0 || totalDurationNanos() == 0) {
            return 0.0;
        }
        double durationSeconds = totalDurationNanos() / 1_000_000_000.0;
        return total() / durationSeconds;
    }

    // HealthMetrics implementation
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
        Map<String, Object> indicators = new HashMap<>();
        indicators.put("circuitBreakerState", breakerState.name());
        indicators.put("consecutiveFailures", consecutiveFailures);
        indicators.put("lastHealthCheck", lastHealthCheckTime);
        indicators.put("successRate", successRatePercent());
        indicators.put("averageLatencyMs", averageMs());
        indicators.put("operationsPerSecond", operationsPerSecond());
        return indicators;
    }

    /**
     * Get the circuit breaker state.
     * 
     * @return circuit breaker state
     */
    public CircuitBreakerState breakerState() {
        return breakerState;
    }

    /**
     * Calculate overall health score (0.0-1.0).
     * 
     * @return health score based on success rate and circuit breaker state
     */
    public double healthScore() {
        double successRate = total() > 0 ? (double) success() / total() : 0.0;
        double breakerPenalty = breakerState == CircuitBreakerState.OPEN ? 0.5 : 0.0;
        return Math.max(0.0, successRate - breakerPenalty);
    }

    /**
     * Calculate success rate percentage.
     * 
     * @return success rate as percentage (0.0-100.0)
     */
    public double successRatePercent() {
        if (total() == 0) {
            return 0.0;
        }
        return (success() * 100.0) / total();
    }

    /**
     * Get the number of consecutive failures.
     * 
     * @return consecutive failure count
     */
    public long getConsecutiveFailures() {
        return consecutiveFailures;
    }

    /**
     * Circuit breaker state enumeration.
     */
    public enum CircuitBreakerState {
        CLOSED,
        OPEN,
        HALF_OPEN
    }
}
