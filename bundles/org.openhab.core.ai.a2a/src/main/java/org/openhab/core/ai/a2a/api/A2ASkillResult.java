package org.openhab.core.ai.a2a.api;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Result of A2A skill execution.
 * 
 * @author AI Assistant
 * @since 1.0.0
 */
@NonNullByDefault
public class A2ASkillResult {

    private final boolean success;
    private final @Nullable Map<String, Object> data;
    private final @Nullable String errorMessage;
    private final @Nullable String errorCode;
    private final long executionTime;

    /**
     * Create a successful skill result.
     * 
     * @param data the result data
     * @param executionTime the execution time in milliseconds
     * @return the skill result
     */
    public static A2ASkillResult success(Map<String, Object> data, long executionTime) {
        return new A2ASkillResult(true, data, null, null, executionTime);
    }

    /**
     * Create a successful skill result with no data.
     * 
     * @param executionTime the execution time in milliseconds
     * @return the skill result
     */
    public static A2ASkillResult success(long executionTime) {
        return new A2ASkillResult(true, Map.of(), null, null, executionTime);
    }

    /**
     * Create a failed skill result.
     * 
     * @param errorMessage the error message
     * @param errorCode the error code
     * @param executionTime the execution time in milliseconds
     * @return the skill result
     */
    public static A2ASkillResult failure(String errorMessage, String errorCode, long executionTime) {
        return new A2ASkillResult(false, null, errorMessage, errorCode, executionTime);
    }

    /**
     * Create a failed skill result with default error code.
     * 
     * @param errorMessage the error message
     * @param executionTime the execution time in milliseconds
     * @return the skill result
     */
    public static A2ASkillResult failure(String errorMessage, long executionTime) {
        return new A2ASkillResult(false, null, errorMessage, "SKILL_EXECUTION_ERROR", executionTime);
    }

    /**
     * Private constructor.
     * 
     * @param success whether the execution was successful
     * @param data the result data
     * @param errorMessage the error message
     * @param errorCode the error code
     * @param executionTime the execution time in milliseconds
     */
    private A2ASkillResult(boolean success, @Nullable Map<String, Object> data, @Nullable String errorMessage,
            @Nullable String errorCode, long executionTime) {
        this.success = success;
        this.data = data;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
        this.executionTime = executionTime;
    }

    /**
     * Check if the execution was successful.
     * 
     * @return true if successful, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Get the result data.
     * 
     * @return the result data, or null if execution failed
     */
    public @Nullable Map<String, Object> getData() {
        return data;
    }

    /**
     * Get the error message.
     * 
     * @return the error message, or null if execution was successful
     */
    public @Nullable String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Get the error code.
     * 
     * @return the error code, or null if execution was successful
     */
    public @Nullable String getErrorCode() {
        return errorCode;
    }

    /**
     * Get the execution time in milliseconds.
     * 
     * @return the execution time
     */
    public long getExecutionTime() {
        return executionTime;
    }

    @Override
    public String toString() {
        return "A2ASkillResult{" + "success=" + success + ", data=" + data + ", errorMessage='" + errorMessage + '\''
                + ", errorCode='" + errorCode + '\'' + ", executionTime=" + executionTime + '}';
    }
}
