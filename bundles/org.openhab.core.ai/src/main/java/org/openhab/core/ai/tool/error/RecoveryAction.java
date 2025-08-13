package org.openhab.core.ai.tool.error;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Recovery action definition.
 * 
 * <p>
 * This class defines a recovery action to be taken when an error occurs,
 * including whether to retry and any delay to apply.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class RecoveryAction {
    private final String action;
    private final String description;
    private final boolean retry;
    private final long delayMs;

    /**
     * Constructor for RecoveryAction.
     * 
     * @param action the action to take
     * @param description description of the action
     * @param retry whether to retry the operation
     * @param delayMs delay in milliseconds before retry
     */
    public RecoveryAction(String action, String description, boolean retry, long delayMs) {
        this.action = action;
        this.description = description;
        this.retry = retry;
        this.delayMs = delayMs;
    }

    /**
     * Get the action to take.
     * 
     * @return action
     */
    public String getAction() {
        return action;
    }

    /**
     * Get the action description.
     * 
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Check if retry is enabled.
     * 
     * @return true if retry is enabled
     */
    public boolean isRetry() {
        return retry;
    }

    /**
     * Get the delay in milliseconds.
     * 
     * @return delay in milliseconds
     */
    public long getDelayMs() {
        return delayMs;
    }
}
