package org.openhab.core.ai.tool.error.recovery;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Result of error recovery operations.
 * 
 * This class encapsulates the result of error recovery operations, including
 * success status, recovery details, and error information.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ErrorRecoveryResult {

    private final boolean recovered;
    private final String status;
    private final String message;
    private final Map<String, Object> details;
    private final long timestamp;

    /**
     * Create a new error recovery result.
     * 
     * @param recovered whether the error was recovered from
     * @param status the recovery status
     * @param message the recovery message
     * @param details additional recovery details
     * @param timestamp the timestamp of the recovery attempt
     */
    public ErrorRecoveryResult(boolean recovered, String status, String message, Map<String, Object> details,
            long timestamp) {
        this.recovered = recovered;
        this.status = status;
        this.message = message;
        this.details = details;
        this.timestamp = timestamp;
    }

    /**
     * Create a successful recovery result.
     * 
     * @param message the recovery message
     * @return successful recovery result
     */
    public static ErrorRecoveryResult success(String message) {
        return new ErrorRecoveryResult(true, "RECOVERED", message, Map.of(), System.currentTimeMillis());
    }

    /**
     * Create a failed recovery result.
     * 
     * @param message the recovery message
     * @return failed recovery result
     */
    public static ErrorRecoveryResult failure(String message) {
        return new ErrorRecoveryResult(false, "FAILED", message, Map.of(), System.currentTimeMillis());
    }

    /**
     * Check if the error was recovered from.
     * 
     * @return true if recovered, false otherwise
     */
    public boolean isRecovered() {
        return recovered;
    }

    /**
     * Get the recovery status.
     * 
     * @return the recovery status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Get the recovery message.
     * 
     * @return the recovery message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get additional recovery details.
     * 
     * @return recovery details
     */
    public Map<String, Object> getDetails() {
        return details;
    }

    /**
     * Get the timestamp of the recovery attempt.
     * 
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    // TODO: Implement error recovery result caching
    // TODO: Add support for error recovery result serialization
    // TODO: Implement error recovery result comparison
    // TODO: Add support for error recovery result metrics
}
