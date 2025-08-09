package org.openhab.core.ai.tool.progress.tracking;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Tracker for monitoring tool operation progress.
 * 
 * This interface defines the contract for progress trackers that can monitor
 * and report the progress of long-running tool operations.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ProgressTracker {

    /**
     * Get the tracker ID.
     * 
     * @return the tracker ID
     */
    String getTrackerId();

    /**
     * Get the tracker name.
     * 
     * @return the tracker name
     */
    String getTrackerName();

    /**
     * Get the tracker description.
     * 
     * @return the tracker description
     */
    String getTrackerDescription();

    /**
     * Start tracking progress for an operation.
     * 
     * @param operationId the operation ID
     * @param totalSteps the total number of steps
     * @return the progress tracker instance
     */
    ProgressTracker startTracking(String operationId, int totalSteps);

    /**
     * Update the progress for an operation.
     * 
     * @param operationId the operation ID
     * @param currentStep the current step
     * @param message the progress message
     */
    void updateProgress(String operationId, int currentStep, String message);

    /**
     * Complete the progress tracking for an operation.
     * 
     * @param operationId the operation ID
     * @param message the completion message
     */
    void completeProgress(String operationId, String message);

    /**
     * Get the current progress for an operation.
     * 
     * @param operationId the operation ID
     * @return the current progress
     */
    ProgressInfo getProgress(String operationId);

    /**
     * Get the tracker configuration.
     * 
     * @return the tracker configuration
     */
    Map<String, Object> getConfiguration();

    /**
     * Update the tracker configuration.
     * 
     * @param configuration the new configuration
     */
    void updateConfiguration(Map<String, Object> configuration);

    // TODO: Implement progress tracking logic
    // TODO: Add support for progress persistence
    // TODO: Implement progress notifications
    // TODO: Add support for progress analytics
}
