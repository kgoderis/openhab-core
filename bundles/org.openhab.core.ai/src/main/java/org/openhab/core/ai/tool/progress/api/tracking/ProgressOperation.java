package org.openhab.core.ai.tool.progress.api.tracking;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface for MCP Progress Operation data.
 * 
 * This represents a progress operation with status, steps, and metadata.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface ProgressOperation {

    /**
     * Get the operation ID.
     * 
     * @return the operation ID
     */
    String getId();

    /**
     * Get the operation description.
     * 
     * @return the operation description
     */
    String getDescription();

    /**
     * Get the total number of steps.
     * 
     * @return the total steps
     */
    int getTotalSteps();

    /**
     * Get the current status.
     * 
     * @return the current status
     */
    ProgressStatus getStatus();

    /**
     * Set the current status.
     * 
     * @param status the new status
     */
    void setStatus(ProgressStatus status);

    /**
     * Get the current step.
     * 
     * @return the current step
     */
    int getCurrentStep();

    /**
     * Set the current step.
     * 
     * @param currentStep the new current step
     */
    void setCurrentStep(int currentStep);

    /**
     * Get the current message.
     * 
     * @return the current message
     */
    String getMessage();

    /**
     * Set the current message.
     * 
     * @param message the new message
     */
    void setMessage(String message);

    /**
     * Get the start time.
     * 
     * @return the start time
     */
    long getStartTime();

    /**
     * Get the last update time.
     * 
     * @return the last update time
     */
    long getLastUpdateTime();

    /**
     * Set the last update time.
     * 
     * @param lastUpdateTime the new last update time
     */
    void setLastUpdateTime(long lastUpdateTime);

    /**
     * Get the completion time.
     * 
     * @return the completion time
     */
    long getCompletionTime();

    /**
     * Set the completion time.
     * 
     * @param completionTime the new completion time
     */
    void setCompletionTime(long completionTime);

    /**
     * Get the progress percentage.
     * 
     * @return the progress percentage (0.0 to 100.0)
     */
    double getProgressPercentage();
}
