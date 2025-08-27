package org.openhab.core.ai.common.monitoring.api;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Capability interface for health monitoring metrics.
 * 
 * <p>
 * This interface provides functionality for health monitoring, including status,
 * indicators, and health assessment. It replaces the Monitoring hierarchy's
 * Health interface with a capability-based approach.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface HealthMetrics {

    /**
     * Get the current health status.
     * 
     * @return the health status
     */
    HealthStatus healthStatus();

    /**
     * Get a human-readable status message.
     * 
     * @return the status message, or null if not available
     */
    @Nullable
    String statusMessage();

    /**
     * Get health indicators as key-value pairs.
     * 
     * @return map of health indicators, or null if none available
     */
    @Nullable
    Map<String, Object> healthIndicators();

    /**
     * Get a specific health indicator by name.
     * 
     * @param indicatorName the name of the indicator
     * @return the indicator value, or null if not found
     */
    @Nullable
    default Object getHealthIndicator(String indicatorName) {
        Map<String, Object> indicators = healthIndicators();
        return indicators != null ? indicators.get(indicatorName) : null;
    }

    /**
     * Check if the component is healthy.
     * 
     * @return true if the status is HEALTHY, false otherwise
     */
    default boolean isHealthy() {
        return healthStatus() == HealthStatus.HEALTHY;
    }

    /**
     * Check if the component is degraded.
     * 
     * @return true if the status is DEGRADED, false otherwise
     */
    default boolean isDegraded() {
        return healthStatus() == HealthStatus.DEGRADED;
    }

    /**
     * Check if the component is unhealthy.
     * 
     * @return true if the status is UNHEALTHY, false otherwise
     */
    default boolean isUnhealthy() {
        return healthStatus() == HealthStatus.UNHEALTHY;
    }

    /**
     * Check if the component is offline.
     * 
     * @return true if the status is OFFLINE, false otherwise
     */
    default boolean isOffline() {
        return healthStatus() == HealthStatus.OFFLINE;
    }

    /**
     * Check if the component status is unknown.
     * 
     * @return true if the status is UNKNOWN, false otherwise
     */
    default boolean isUnknown() {
        return healthStatus() == HealthStatus.UNKNOWN;
    }
}
