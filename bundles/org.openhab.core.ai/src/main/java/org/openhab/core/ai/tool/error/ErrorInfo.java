package org.openhab.core.ai.tool.error;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Error information container.
 * 
 * <p>
 * This class provides detailed information about a specific error including
 * the error type, message, count, and last occurrence timestamp.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ErrorInfo {
    private final String errorType;
    private final String message;
    private final long count;
    private final long lastOccurrence;

    /**
     * Constructor for ErrorInfo.
     * 
     * @param errorType the type of error
     * @param message the error message
     * @param count the number of occurrences
     * @param lastOccurrence timestamp of last occurrence
     */
    public ErrorInfo(String errorType, String message, long count, long lastOccurrence) {
        this.errorType = errorType;
        this.message = message;
        this.count = count;
        this.lastOccurrence = lastOccurrence;
    }

    /**
     * Get the error type.
     * 
     * @return error type
     */
    public String getErrorType() {
        return errorType;
    }

    /**
     * Get the error message.
     * 
     * @return error message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the number of occurrences.
     * 
     * @return count
     */
    public long getCount() {
        return count;
    }

    /**
     * Get the timestamp of last occurrence.
     * 
     * @return last occurrence timestamp
     */
    public long getLastOccurrence() {
        return lastOccurrence;
    }
}
