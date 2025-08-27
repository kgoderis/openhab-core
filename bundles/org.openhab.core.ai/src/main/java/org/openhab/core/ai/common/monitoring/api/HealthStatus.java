package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Enumeration of health status values.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public enum HealthStatus {
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
