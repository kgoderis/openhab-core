package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Composite capability interface that combines all monitoring capabilities.
 * 
 * <p>
 * This interface extends all the individual capability interfaces to provide
 * a comprehensive view of all available metrics in a single interface.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface FullMetrics extends CountsMetrics, LatencyMetrics, ModelMetrics, HealthMetrics, ErrorMetrics, ThroughputMetrics {

    /**
     * Get a comprehensive health summary.
     * 
     * @return health summary string
     */
    default String getHealthSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Health: ").append(healthStatus()).append(" (Score: ").append(String.format("%.2f", healthScore())).append(")");
        
        if (hasOperations()) {
            summary.append(", Success Rate: ").append(String.format("%.1f%%", successRatePercentage()));
        }
        
        if (hasDuration()) {
            summary.append(", Avg Latency: ").append(String.format("%.2fms", averageMs(total())));
        }
        
        if (hasErrors()) {
            summary.append(", Error Rate: ").append(String.format("%.1f%%", errorRatePercentage(total())));
        }
        
        return summary.toString();
    }

    /**
     * Get a performance summary.
     * 
     * @return performance summary string
     */
    default String getPerformanceSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Throughput: ").append(String.format("%.2f ops/sec", operationsPerSecond()));
        summary.append(", Utilization: ").append(String.format("%.1f%%", throughputUtilizationPercentage()));
        summary.append(", Stability: ").append(String.format("%.2f", throughputStability()));
        
        if (hasDuration()) {
            summary.append(", Avg Response: ").append(String.format("%.2fms", averageMs(total())));
        }
        
        return summary.toString();
    }

    /**
     * Check if the system is performing well overall.
     * 
     * @return true if performing well, false otherwise
     */
    default boolean isPerformingWell() {
        return isHealthy() && 
               successRate() >= 0.95 && 
               errorRatePercentage(total()) <= 5.0 &&
               throughputStability() >= 0.8;
    }

    /**
     * Check if the system needs attention.
     * 
     * @return true if attention is needed, false otherwise
     */
    default boolean needsAttention() {
        return !isHealthy() || 
               successRate() < 0.9 || 
               errorRatePercentage(total()) > 10.0 ||
               throughputStability() < 0.6;
    }

    /**
     * Get a priority score for monitoring (0.0 to 1.0, higher = more urgent).
     * 
     * @return priority score between 0.0 and 1.0
     */
    default double getPriorityScore() {
        double score = 0.0;
        
        // Health status (40% weight)
        score += (1.0 - healthScore()) * 0.4;
        
        // Success rate (30% weight)
        if (hasOperations()) {
            score += (1.0 - successRate()) * 0.3;
        }
        
        // Error rate (20% weight)
        if (hasOperations()) {
            score += Math.min(1.0, errorRatePercentage(total()) / 100.0) * 0.2;
        }
        
        // Throughput stability (10% weight)
        score += (1.0 - throughputStability()) * 0.1;
        
        return Math.min(1.0, score);
    }

    /**
     * Get the overall system status.
     * 
     * @return system status
     */
    default SystemStatus getSystemStatus() {
        if (needsAttention()) {
            return SystemStatus.CRITICAL;
        } else if (!isPerformingWell()) {
            return SystemStatus.WARNING;
        } else {
            return SystemStatus.NORMAL;
        }
    }

    /**
     * System status enumeration.
     */
    enum SystemStatus {
        NORMAL,
        WARNING,
        CRITICAL
    }
}
