package org.openhab.core.ai.tool.progress;

import java.time.Instant;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.ai.tool.progress.tracking.ProgressStatus;

/**
 * Tracker for tool execution progress.
 * 
 * Provides progress tracking capabilities for tool operations.
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class ToolProgressTracker {

    private final String trackerId;
    private final String toolId;
    private final Instant startTime;
    private Instant lastUpdateTime;
    private ProgressStatus status;
    private int currentStep;
    private int totalSteps;
    private String currentMessage;
    private @Nullable String errorMessage;
    private final Map<String, Object> metadata;

    /**
     * Create a new progress tracker.
     * 
     * @param trackerId the tracker ID
     * @param toolId the tool ID
     * @param totalSteps the total number of steps
     * @param metadata additional metadata
     */
    public ToolProgressTracker(String trackerId, String toolId, int totalSteps, Map<String, Object> metadata) {
        this.trackerId = trackerId;
        this.toolId = toolId;
        this.startTime = Instant.now();
        this.lastUpdateTime = Instant.now();
        this.status = ProgressStatus.IN_PROGRESS;
        this.currentStep = 0;
        this.totalSteps = totalSteps;
        this.currentMessage = "Initializing...";
        this.metadata = metadata;
    }

    /**
     * Get the tracker ID.
     * 
     * @return the tracker ID
     */
    public String getTrackerId() {
        return trackerId;
    }

    /**
     * Get the tool ID.
     * 
     * @return the tool ID
     */
    public String getToolId() {
        return toolId;
    }

    /**
     * Get the start time.
     * 
     * @return the start time
     */
    public Instant getStartTime() {
        return startTime;
    }

    /**
     * Get the last update time.
     * 
     * @return the last update time
     */
    public Instant getLastUpdateTime() {
        return lastUpdateTime;
    }

    /**
     * Get the current status.
     * 
     * @return the current status
     */
    public ProgressStatus getStatus() {
        return status;
    }

    /**
     * Set the current status.
     * 
     * @param status the new status
     */
    public void setStatus(ProgressStatus status) {
        this.status = status;
        this.lastUpdateTime = Instant.now();
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
     * Set the current step.
     * 
     * @param currentStep the new current step
     */
    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
        this.lastUpdateTime = Instant.now();
    }

    /**
     * Get the total steps.
     * 
     * @return the total steps
     */
    public int getTotalSteps() {
        return totalSteps;
    }

    /**
     * Get the current message.
     * 
     * @return the current message
     */
    public String getCurrentMessage() {
        return currentMessage;
    }

    /**
     * Set the current message.
     * 
     * @param currentMessage the new current message
     */
    public void setCurrentMessage(String currentMessage) {
        this.currentMessage = currentMessage;
        this.lastUpdateTime = Instant.now();
    }

    /**
     * Get the error message.
     * 
     * @return the error message or null if no error
     */
    @Nullable
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Set the error message.
     * 
     * @param errorMessage the error message
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        this.lastUpdateTime = Instant.now();
    }

    /**
     * Get the metadata.
     * 
     * @return the metadata
     */
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Get the progress percentage.
     * 
     * @return the progress percentage (0-100)
     */
    public int getProgressPercentage() {
        if (totalSteps <= 0) {
            return 0;
        }
        return Math.min(100, (currentStep * 100) / totalSteps);
    }

    /**
     * Get the elapsed time in milliseconds.
     * 
     * @return the elapsed time in milliseconds
     */
    public long getElapsedTimeMs() {
        return Instant.now().toEpochMilli() - startTime.toEpochMilli();
    }

    /**
     * Check if the tracker is complete.
     * 
     * @return true if the tracker is complete
     */
    public boolean isComplete() {
        return status == ProgressStatus.COMPLETED || status == ProgressStatus.FAILED;
    }

    /**
     * Mark the tracker as completed.
     */
    public void markCompleted() {
        this.status = ProgressStatus.COMPLETED;
        this.currentStep = this.totalSteps;
        this.lastUpdateTime = Instant.now();
    }

    /**
     * Mark the tracker as failed.
     * 
     * @param errorMessage the error message
     */
    public void markFailed(String errorMessage) {
        this.status = ProgressStatus.FAILED;
        this.errorMessage = errorMessage;
        this.lastUpdateTime = Instant.now();
    }

    @Override
    public String toString() {
        return "ToolProgressTracker{trackerId='" + trackerId + "', toolId='" + toolId + "', status=" + status
                + ", progress=" + getProgressPercentage() + "%}";
    }
}
