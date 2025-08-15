package org.openhab.core.ai.tool.resources.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Resource Result for MCP Resources
 * 
 * This class defines execution results for MCP resources
 * including success status, content, and execution time.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class ResourceResult {

    private final boolean success;
    private final Object content;
    private final long executionTimeMs;
    private final String errorMessage;

    /**
     * Create a new ResourceResult instance
     * 
     * @param success Whether the execution was successful
     * @param content The result content
     * @param executionTimeMs The execution time in milliseconds
     * @param errorMessage The error message (if any)
     */
    public ResourceResult(boolean success, Object content, long executionTimeMs, String errorMessage) {
        this.success = success;
        this.content = content;
        this.executionTimeMs = executionTimeMs;
        this.errorMessage = errorMessage;
    }

    /**
     * Create a successful result
     * 
     * @param content The result content
     * @param executionTimeMs The execution time in milliseconds
     * @return A successful result
     */
    public static ResourceResult success(Object content, long executionTimeMs) {
        return new ResourceResult(true, content, executionTimeMs, null);
    }

    /**
     * Create a failed result
     * 
     * @param errorMessage The error message
     * @param executionTimeMs The execution time in milliseconds
     * @return A failed result
     */
    public static ResourceResult failure(String errorMessage, long executionTimeMs) {
        return new ResourceResult(false, null, executionTimeMs, errorMessage);
    }

    /**
     * Check if the execution was successful
     * 
     * @return true if successful, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Get the result content
     * 
     * @return The result content
     */
    public Object getContent() {
        return content;
    }

    /**
     * Get the execution time in milliseconds
     * 
     * @return The execution time
     */
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    /**
     * Get the error message
     * 
     * @return The error message or null if successful
     */
    public String getErrorMessage() {
        return errorMessage;
    }
}
