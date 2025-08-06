package org.openhab.core.ai.action;

import java.time.Duration;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Individual metric for an agent action execution
 * 
 * <p>
 * This class represents a single metric entry for agent action execution:
 * - Action identification and timing
 * - Success/failure status
 * - Error information and context
 * - Performance timing details
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public class ActionMetric {

    private final String actionName;
    private final boolean success;
    private final Duration duration;
    private final @Nullable String error;
    private final Instant timestamp;

    /**
     * Create a new agent metric
     * 
     * @param actionName the name of the action
     * @param success whether the action was successful
     * @param duration the duration of the action execution
     * @param error the error message if the action failed
     * @param timestamp the timestamp when the action was executed
     */
    public ActionMetric(String actionName, boolean success, Duration duration, @Nullable String error,
            Instant timestamp) {
        this.actionName = actionName;
        this.success = success;
        this.duration = duration;
        this.error = error;
        this.timestamp = timestamp;
    }

    /**
     * Get the action name
     * 
     * @return the action name
     */
    public String getActionName() {
        return actionName;
    }

    /**
     * Check if the action was successful
     * 
     * @return true if the action was successful
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Get the duration of the action execution
     * 
     * @return the execution duration
     */
    public Duration getDuration() {
        return duration;
    }

    /**
     * Get the error message if the action failed
     * 
     * @return the error message, or null if the action was successful
     */
    public @Nullable String getError() {
        return error;
    }

    /**
     * Get the timestamp when the action was executed
     * 
     * @return the execution timestamp
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Get the duration in milliseconds
     * 
     * @return the duration in milliseconds
     */
    public long getDurationMs() {
        return duration.toMillis();
    }

    /**
     * Check if the action took longer than a threshold
     * 
     * @param threshold the duration threshold
     * @return true if the action took longer than the threshold
     */
    public boolean isSlow(Duration threshold) {
        return duration.compareTo(threshold) > 0;
    }

    /**
     * Check if the action was recent (within the last hour)
     * 
     * @return true if the action was recent
     */
    public boolean isRecent() {
        return Duration.between(timestamp, Instant.now()).toHours() < 1;
    }

    @Override
    public String toString() {
        return "AgentMetric{" + "actionName='" + actionName + '\'' + ", success=" + success + ", duration=" + duration
                + ", error='" + error + '\'' + ", timestamp=" + timestamp + '}';
    }
}
