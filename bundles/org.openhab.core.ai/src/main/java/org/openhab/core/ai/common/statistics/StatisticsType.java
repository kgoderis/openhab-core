package org.openhab.core.ai.common.statistics;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Enumeration of statistics types in the openHAB AI system.
 * 
 * <p>
 * This enum defines the different categories of statistics that can be collected
 * and tracked across the AI system.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public enum StatisticsType {

    /**
     * Performance statistics (timing, throughput, etc.).
     */
    PERFORMANCE("performance"),

    /**
     * Security statistics (authentication, authorization, etc.).
     */
    SECURITY("security"),

    /**
     * Execution statistics (success/failure rates, etc.).
     */
    EXECUTION("execution"),

    /**
     * Monitoring statistics (system health, alerts, etc.).
     */
    MONITORING("monitoring"),

    /**
     * Transport statistics (network, communication, etc.).
     */
    TRANSPORT("transport"),

    /**
     * Resource statistics (memory, CPU, etc.).
     */
    RESOURCE("resource"),

    /**
     * Communication statistics (messages, events, etc.).
     */
    COMMUNICATION("communication"),

    /**
     * Error statistics (recovery, failures, etc.).
     */
    ERROR("error"),

    /**
     * System statistics (general system metrics).
     */
    SYSTEM("system"),

    /**
     * Server statistics (servlet, endpoint, etc.).
     */
    SERVER("server"),

    /**
     * Custom statistics (user-defined metrics).
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
