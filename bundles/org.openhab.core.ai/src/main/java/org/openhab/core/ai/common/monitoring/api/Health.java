package org.openhab.core.ai.common.monitoring.api;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Interface for health monitoring data in the openHAB AI system.
 * 
 * <p>
 * This interface extends MonitoringData to provide specialized methods for
 * health-related monitoring, focusing on status, indicators, alerts,
 * and system health assessment.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface Health extends Monitoring {

    /**
     * Enumeration of health status values.
     */
    enum HealthStatus {
        /** Component is healthy and functioning normally */
        HEALTHY("healthy"),
        /** Component is degraded but still functional */
        DEGRADED("degraded"),
        /** Component is unhealthy and may have issues */
        UNHEALTHY("unhealthy"),
        /** Component status is unknown */
        UNKNOWN("unknown"),
        /** Component is offline or unavailable */
        OFFLINE("offline");

        private final String value;

        HealthStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    /**
     * Get the current health status.
     * 
     * @return the health status
     */
    HealthStatus getStatus();

    /**
     * Get a human-readable status message.
     * 
     * @return the status message, or null if not available
     */
    @Nullable
    String getStatusMessage();

    /**
     * Get health indicators as key-value pairs.
     * 
     * @return map of health indicators, or null if none available
     */
    @Nullable
    Map<String, Object> getHealthIndicators();

    /**
     * Get a specific health indicator by name.
     * 
     * @param indicatorName the name of the indicator
     * @return the indicator value, or null if not found
     */
    @Nullable
    default Object getHealthIndicator(String indicatorName) {
        Map<String, Object> indicators = getHealthIndicators();
        return indicators != null ? indicators.get(indicatorName) : null;
    }

    /**
     * Check if the component is healthy.
     * 
     * @return true if the status is HEALTHY, false otherwise
     */
    default boolean isHealthy() {
        return getStatus() == HealthStatus.HEALTHY;
    }

    /**
     * Check if the component is degraded.
     * 
     * @return true if the status is DEGRADED, false otherwise
     */
    default boolean isDegraded() {
        return getStatus() == HealthStatus.DEGRADED;
    }

    /**
     * Check if the component is unhealthy.
     * 
     * @return true if the status is UNHEALTHY, false otherwise
     */
    default boolean isUnhealthy() {
        return getStatus() == HealthStatus.UNHEALTHY;
    }

    /**
     * Check if the component is offline.
     * 
     * @return true if the status is OFFLINE, false otherwise
     */
    default boolean isOffline() {
        return getStatus() == HealthStatus.OFFLINE;
    }

    /**
     * Check if the component needs attention.
     * 
     * @return true if the status is DEGRADED, UNHEALTHY, or OFFLINE
     */
    default boolean needsAttention() {
        HealthStatus status = getStatus();
        return status == HealthStatus.DEGRADED || status == HealthStatus.UNHEALTHY || status == HealthStatus.OFFLINE;
    }

    /**
     * Get the timestamp when the health status was last updated.
     * 
     * @return the last update timestamp, or null if not tracked
     */
    @Nullable
    default Instant getLastStatusUpdate() {
        return getTimestamp();
    }

    @Override
    default MonitoringType getType() {
        return MonitoringType.HEALTH;
    }
}
