package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Types of statistics that can be collected and monitored.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public enum StatisticsType {
    /**
     * Performance-related statistics
     */
    PERFORMANCE("performance"),

    /**
     * Security-related statistics
     */
    SECURITY("security"),

    /**
     * Execution-related statistics
     */
    EXECUTION("execution"),

    /**
     * Monitoring-related statistics
     */
    MONITORING("monitoring"),

    /**
     * Transport-related statistics
     */
    TRANSPORT("transport"),

    /**
     * Resource-related statistics
     */
    RESOURCE("resource"),

    /**
     * Communication-related statistics
     */
    COMMUNICATION("communication"),

    /**
     * Error-related statistics
     */
    ERROR("error"),

    /**
     * System-related statistics
     */
    SYSTEM("system"),

    /**
     * Custom statistics
     */
    CUSTOM("custom");

    private final String value;

    StatisticsType(String value) {
        this.value = value;
    }

    /**
     * Get the string value of this statistics type.
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
