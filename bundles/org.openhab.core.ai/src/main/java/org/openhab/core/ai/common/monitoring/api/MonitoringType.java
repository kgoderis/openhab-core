package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Enumeration of monitoring data types in the openHAB AI system.
 * 
 * <p>
 * This enum defines the different types of monitoring information that can be
 * collected and processed by the unified monitoring system.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public enum MonitoringType {

    /**
     * Performance monitoring data.
     * 
     * <p>
     * Focuses on operations, timing, rates, and efficiency metrics.
     * Examples: response times, throughput, operation counts, processing times.
     * </p>
     */
    PERFORMANCE("performance"),

    /**
     * Statistics monitoring data.
     * 
     * <p>
     * Focuses on counts, aggregations, trends, and historical data.
     * Examples: total counts, success/failure rates, averages, distributions.
     * </p>
     */
    STATISTICS("statistics"),

    /**
     * Health monitoring data.
     * 
     * <p>
     * Focuses on status, indicators, alerts, and system health.
     * Examples: component status, error rates, availability, resource usage.
     * </p>
     */
    HEALTH("health");

    private final String value;

    MonitoringType(String value) {
        this.value = value;
    }

    /**
     * Get the string value of this monitoring type.
     * 
     * @return the string value
     */
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
