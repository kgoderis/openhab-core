package org.openhab.core.ai.tool.monitoring.health;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.HealthMetrics;
import org.openhab.core.ai.common.monitoring.api.HealthStatus;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.MetricsSnapshot;

/**
 * Immutable snapshot of system health check metrics data.
 * 
 * <p>
 * This class provides a point-in-time view of system health check performance
 * including basic counts, latency metrics, and health-specific metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record SystemCheckSnapshot(long total, long success, long failure, long totalDurationNanos,
        long dependenciesCount, long averageDependencyDepth, String healthCheckId, String category,
        long timestampMs) implements MetricsSnapshot, CountsMetrics, LatencyMetrics, HealthMetrics {

    /**
     * Factory method to create a snapshot from metrics data.
     * 
     * @param total total health checks performed
     * @param success successful health checks
     * @param failure failed health checks
     * @param totalDurationNanos total duration in nanoseconds
     * @param dependenciesCount number of dependencies
     * @param averageDependencyDepth average dependency depth
     * @param healthCheckId the health check identifier
     * @param category the health check category
     * @return new SystemCheckSnapshot instance
     */
    public static SystemCheckSnapshot of(long total, long success, long failure, long totalDurationNanos,
            long dependenciesCount, long averageDependencyDepth, String healthCheckId, String category) {
        return new SystemCheckSnapshot(total, success, failure, totalDurationNanos, dependenciesCount,
                averageDependencyDepth, healthCheckId, category, System.currentTimeMillis());
    }

    /**
     * Factory method to create an empty snapshot.
     * 
     * @param healthCheckId the health check identifier
     * @param category the health check category
     * @return empty SystemCheckSnapshot instance
     */
    public static SystemCheckSnapshot empty(String healthCheckId, String category) {
        return new SystemCheckSnapshot(0, 0, 0, 0, 0, 0, healthCheckId, category, System.currentTimeMillis());
    }

    /**
     * Get the timestamp as an Instant.
     * 
     * @return timestamp as Instant
     */
    public Instant timestamp() {
        return Instant.ofEpochMilli(timestampMs);
    }

    /**
     * Calculate health check efficiency score.
     * 
     * @return efficiency score
     */
    public double healthCheckEfficiency() {
        if (total == 0) {
            return 100.0;
        }
        // Combine success rate with dependency efficiency
        double successRateWeight = successRatePercentage() * 0.7;
        double dependencyWeight = (dependenciesCount > 0 ? Math.max(0, 100.0 - (averageDependencyDepth * 10.0)) : 100.0)
                * 0.3;
        return Math.min(100.0, successRateWeight + dependencyWeight);
    }

    // HealthMetrics implementation
    @Override
    public HealthStatus healthStatus() {
        double health = healthCheckEfficiency();
        if (health >= 90.0) {
            return HealthStatus.HEALTHY;
        } else if (health >= 70.0) {
            return HealthStatus.HEALTHY;
        } else if (health >= 50.0) {
            return HealthStatus.DEGRADED;
        } else if (health >= 30.0) {
            return HealthStatus.UNHEALTHY;
        } else if (total > 0) {
            return HealthStatus.UNHEALTHY;
        } else {
            return HealthStatus.UNKNOWN;
        }
    }

    @Override
    public @Nullable String statusMessage() {
        double health = healthCheckEfficiency();
        if (health >= 90.0) {
            return "System health check is performing excellently";
        } else if (health >= 70.0) {
            return "System health check is performing well";
        } else if (health >= 50.0) {
            return "System health check is degraded but functional";
        } else if (health >= 30.0) {
            return "System health check is experiencing issues";
        } else if (total > 0) {
            return "System health check is in critical condition";
        } else {
            return "No health check data available";
        }
    }

    @Override
    public @Nullable Map<String, Object> healthIndicators() {
        return Map.of("successRate", successRatePercentage(), "averageLatencyMs", averageMs(total), "totalChecks",
                total, "dependenciesCount", dependenciesCount, "averageDependencyDepth", averageDependencyDepth,
                "category", category, "healthCheckId", healthCheckId, "efficiency", healthCheckEfficiency());
    }

    /**
     * Create a summary string with key metrics.
     * 
     * @return summary string
     */
    public String toSummary() {
        return String.format(
                "SystemCheckSnapshot{id=%s, category=%s, total=%d, success=%d, avgMs=%.2f, health=%.1f%%, status=%s}",
                healthCheckId, category, total, success, averageMs(total), healthCheckEfficiency(),
                healthStatus().getValue());
    }
}
