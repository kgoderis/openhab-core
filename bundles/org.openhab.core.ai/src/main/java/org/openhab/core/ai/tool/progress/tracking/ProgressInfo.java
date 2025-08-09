package org.openhab.core.ai.tool.progress.tracking;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Information about operation progress.
 * 
 * This class encapsulates information about the progress of a tool operation,
 * including current step, total steps, and status.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ProgressInfo {

    private final String operationId;
    private final int currentStep;
    private final int totalSteps;
    private final String status;
    private final String message;
    private final long timestamp;
    private final Map<String, Object> metadata;

    /**
     * Create a new progress info.
     * 
     * @param operationId the operation ID
     * @param currentStep the current step
     * @param totalSteps the total number of steps
     * @param status the operation status
     * @param message the progress message
     * @param timestamp the timestamp
     * @param metadata additional progress metadata
     */
    public ProgressInfo(String operationId, int currentStep, int totalSteps, String status, String message,
            long timestamp, Map<String, Object> metadata) {
        this.operationId = operationId;
        this.currentStep = currentStep;
        this.totalSteps = totalSteps;
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
        this.metadata = metadata;
    }

    /**
     * Get the operation ID.
     * 
     * @return the operation ID
     */
    public String getOperationId() {
        return operationId;
    }

    /**
     * Get the current step.
     * 
     * @return the current step
     */
    public int getCurrentStep() {
        return currentStep;
    }

    /**
     * Get the total number of steps.
     * 
     * @return the total number of steps
     */
    public int getTotalSteps() {
        return totalSteps;
    }

    /**
     * Get the operation status.
     * 
     * @return the operation status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Get the progress message.
     * 
     * @return the progress message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Get the timestamp.
     * 
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Get additional progress metadata.
     * 
     * @return progress metadata
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Get the progress percentage.
     * 
     * @return the progress percentage (0-100)
     */
    public double getProgressPercentage() {
        if (totalSteps == 0) {
            return 0.0;
        }
        return (double) currentStep / totalSteps * 100.0;
    }

    /**
     * Check if the operation is completed.
     * 
     * @return true if completed, false otherwise
     */
    public boolean isCompleted() {
        return "COMPLETED".equals(status);
    }

    // TODO: Implement progress info validation
    // TODO: Add support for progress info serialization
    // TODO: Implement progress info comparison
    // TODO: Add support for progress info metrics
}
