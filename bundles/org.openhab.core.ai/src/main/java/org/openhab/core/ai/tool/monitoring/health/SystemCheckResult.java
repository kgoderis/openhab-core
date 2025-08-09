package org.openhab.core.ai.tool.monitoring.health;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Result of health check operations.
 * 
 * This class encapsulates the result of health check operations, including
 * status, details, and health information.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class SystemCheckResult {

    private final boolean healthy;
    private final String status;
    private final String message;
    private final Map<String, Object> details;
    private final long timestamp;

    /**
     * Create a new health check result.
     * 
     * @param healthy whether the health check passed
     * @param status the health status
     * @param message the health message
     * @param details additional health details
     * @param timestamp the timestamp of the check
     */
    public SystemCheckResult(boolean healthy, String status, String message, Map<String, Object> details,
            long timestamp) {
        this.healthy = healthy;
        this.status = status;
        this.message = message;
        this.details = details;
        this.timestamp = timestamp;
    }

    /**
     * Create a healthy health check result.
     * 
     * @param message the health message
     * @return healthy health check result
     */
    public static SystemCheckResult healthy(String message) {
        return new SystemCheckResult(true, "HEALTHY", message, Map.of(), System.currentTimeMillis());
    }

    /**
     * Create an unhealthy health check result.
     * 
     * @param message the health message
     * @return unhealthy health check result
     */
    public static SystemCheckResult unhealthy(String message) {
        return new SystemCheckResult(false, "UNHEALTHY", message, Map.of(), System.currentTimeMillis());
    }

    /**
     * Check if the health check passed.
     * 
     * @return true if healthy, false otherwise
     */
    public boolean isHealthy() {
        return healthy;
    }

    /**
     * Get the health status.
     * 
     * @return the health status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Get the health message.
     * 
     * @return the health message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get additional health details.
     * 
     * @return health details
     */
    public Map<String, Object> getDetails() {
        return details;
    }

    /**
     * Get the timestamp of the health check.
     * 
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }
}
