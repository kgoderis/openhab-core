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
 * Snapshot of system health monitor metrics at a specific point in time.
 * 
 * This class provides real-time system health monitoring metrics implementing
 * CountsMetrics, LatencyMetrics, and HealthMetrics interfaces for clean,
 * type-safe access to system-wide health data.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record SystemHealthMonitorSnapshot(Counts counts, Timing timing, long timestampMs, HealthStatus healthStatus,
        String statusMessage, SystemHealthState systemState, long totalProviders, long healthyProviders,
        long totalServices, long healthyServices,
        long lastSystemCheckTime) implements MetricsSnapshot, CountsMetrics, LatencyMetrics, HealthMetrics {

    public SystemHealthMonitorSnapshot {
        Objects.requireNonNull(counts, "counts");
        Objects.requireNonNull(timing, "timing");
        Objects.requireNonNull(healthStatus, "healthStatus");
        Objects.requireNonNull(statusMessage, "statusMessage");
        Objects.requireNonNull(systemState, "systemState");
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
        indicators.put("systemState", systemState.name());
        indicators.put("totalProviders", totalProviders);
        indicators.put("healthyProviders", healthyProviders);
        indicators.put("totalServices", totalServices);
        indicators.put("healthyServices", healthyServices);
        indicators.put("lastSystemCheck", lastSystemCheckTime);
        indicators.put("providerHealthPercentage", getProviderHealthPercentage());
        indicators.put("serviceHealthPercentage", getServiceHealthPercentage());
        indicators.put("overallHealthScore", getOverallHealthScore());
        indicators.put("successRate", successRatePercent());
        indicators.put("averageLatencyMs", averageMs());
        indicators.put("operationsPerSecond", operationsPerSecond());
        return indicators;
    }

    /**
     * Get the system health state.
     * 
     * @return system health state
     */
    public SystemHealthState systemState() {
        return systemState;
    }

    /**
     * Calculate overall system health score (0.0-1.0).
     * 
     * @return health score based on provider and service health
     */
    public double getOverallHealthScore() {
        double providerScore = totalProviders > 0 ? (double) healthyProviders / totalProviders : 1.0;
        double serviceScore = totalServices > 0 ? (double) healthyServices / totalServices : 1.0;
        double operationScore = total() > 0 ? (double) success() / total() : 1.0;

        return (providerScore + serviceScore + operationScore) / 3.0;
    }

    /**
     * Calculate provider health percentage.
     * 
     * @return provider health percentage (0.0-100.0)
     */
    public double getProviderHealthPercentage() {
        if (totalProviders == 0) {
            return 100.0;
        }
        return (healthyProviders * 100.0) / totalProviders;
    }

    /**
     * Calculate service health percentage.
     * 
     * @return service health percentage (0.0-100.0)
     */
    public double getServiceHealthPercentage() {
        if (totalServices == 0) {
            return 100.0;
        }
        return (healthyServices * 100.0) / totalServices;
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
     * Get the number of unhealthy providers.
     * 
     * @return unhealthy provider count
     */
    public long getUnhealthyProviders() {
        return totalProviders - healthyProviders;
    }

    /**
     * Get the number of unhealthy services.
     * 
     * @return unhealthy service count
     */
    public long getUnhealthyServices() {
        return totalServices - healthyServices;
    }

    /**
     * Check if the system is healthy.
     * 
     * @return true if the system is healthy
     */
    public boolean isHealthy() {
        return systemState == SystemHealthState.HEALTHY && getOverallHealthScore() >= 0.8
                && getProviderHealthPercentage() >= 80.0 && getServiceHealthPercentage() >= 80.0;
    }

    /**
     * Check if the system requires immediate attention.
     * 
     * @return true if attention is required
     */
    public boolean requiresImmediateAttention() {
        return systemState == SystemHealthState.CRITICAL || getOverallHealthScore() < 0.5
                || getProviderHealthPercentage() < 50.0 || getServiceHealthPercentage() < 50.0;
    }

    /**
     * System health state enumeration.
     */
    public enum SystemHealthState {
        HEALTHY,
        DEGRADED,
        CRITICAL,
        OFFLINE,
        UNKNOWN
    }
}
