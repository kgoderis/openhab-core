package org.openhab.core.ai.tool.progress.tracking;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.tool.api.validation.ValidationResult;

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

    /**
     * Validate this progress info.
     * 
     * @return validation result
     */
    public ValidationResult validate() {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Validate required fields
        if (operationId == null || operationId.isEmpty()) {
            errors.add("Operation ID cannot be null or empty");
        }
        if (status == null || status.isEmpty()) {
            errors.add("Status cannot be null or empty");
        }
        if (message == null) {
            errors.add("Message cannot be null");
        }

        // Validate numeric constraints
        if (currentStep < 0) {
            errors.add("Current step cannot be negative");
        }
        if (totalSteps < 0) {
            errors.add("Total steps cannot be negative");
        }
        if (currentStep > totalSteps && totalSteps > 0) {
            warnings.add("Current step exceeds total steps");
        }
        if (timestamp < 0) {
            errors.add("Timestamp cannot be negative");
        }

        // Validate status values
        if (!isValidStatus(status)) {
            warnings.add("Unknown status value: " + status);
        }

        boolean isValid = errors.isEmpty();
        Map<String, Object> details = Map.of("errors", errors, "warnings", warnings);
        return new ValidationResult(isValid, errors, warnings, details);
    }

    /**
     * Check if the status is valid.
     * 
     * @param status the status to check
     * @return true if valid
     */
    private boolean isValidStatus(String status) {
        return "PENDING".equals(status) || "IN_PROGRESS".equals(status) || "COMPLETED".equals(status)
                || "FAILED".equals(status) || "CANCELLED".equals(status);
    }

    /**
     * Compare this progress info with another.
     * 
     * @param other the other progress info
     * @return comparison result
     */
    public ProgressComparisonResult compare(ProgressInfo other) {
        if (other == null) {
            return new ProgressComparisonResult(false, false, false, false, false);
        }

        boolean sameOperation = this.operationId.equals(other.operationId);
        boolean sameProgress = this.currentStep == other.currentStep && this.totalSteps == other.totalSteps;
        boolean sameStatus = this.status.equals(other.status);
        boolean sameMessage = this.message.equals(other.message);
        boolean sameTimestamp = this.timestamp == other.timestamp;

        return new ProgressComparisonResult(sameOperation, sameProgress, sameStatus, sameMessage, sameTimestamp);
    }

    /**
     * Check if this progress info is equivalent to another.
     * 
     * @param other the other progress info
     * @return true if equivalent
     */
    public boolean isEquivalent(ProgressInfo other) {
        if (other == null) {
            return false;
        }
        return this.operationId.equals(other.operationId) && this.currentStep == other.currentStep
                && this.totalSteps == other.totalSteps && this.status.equals(other.status)
                && this.message.equals(other.message);
    }

    /**
     * Get progress metrics.
     * 
     * @return progress metrics
     */
    public ProgressMetrics getMetrics() {
        return new ProgressMetrics(currentStep, totalSteps, getProgressPercentage(), isCompleted(), timestamp,
                System.currentTimeMillis());
    }

    /**
     * Result of progress comparison.
     */
    // ProgressComparisonResult extracted to org.openhab.core.ai.tool.progress.tracking.ProgressComparisonResult

    /**
     * Progress metrics.
     */
    // ProgressMetrics extracted to org.openhab.core.ai.tool.progress.tracking.ProgressMetrics
}
