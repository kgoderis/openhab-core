package org.openhab.core.ai.tool.error;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Error recovery strategy definition.
 * 
 * <p>
 * This class defines a strategy for recovering from errors including
 * retry attempts, delays, and whether the strategy is enabled.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public class ErrorRecoveryStrategy {
    private final String strategyName;
    private final String description;
    private final int maxRetryAttempts;
    private final long retryDelayMs;
    private final boolean enabled;

    /**
     * Constructor for ErrorRecoveryStrategy.
     * 
     * @param strategyName name of the strategy
     * @param description description of the strategy
     * @param maxRetryAttempts maximum number of retry attempts
     * @param retryDelayMs delay between retries in milliseconds
     * @param enabled whether the strategy is enabled
     */
    public ErrorRecoveryStrategy(String strategyName, String description, int maxRetryAttempts, long retryDelayMs,
            boolean enabled) {
        this.strategyName = strategyName;
        this.description = description;
        this.maxRetryAttempts = maxRetryAttempts;
        this.retryDelayMs = retryDelayMs;
        this.enabled = enabled;
    }

    /**
     * Get the strategy name.
     * 
     * @return strategy name
     */
    public String getStrategyName() {
        return strategyName;
    }

    /**
     * Get the strategy description.
     * 
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get the maximum number of retry attempts.
     * 
     * @return max retry attempts
     */
    public int getMaxRetryAttempts() {
        return maxRetryAttempts;
    }

    /**
     * Get the retry delay in milliseconds.
     * 
     * @return retry delay in milliseconds
     */
    public long getRetryDelayMs() {
        return retryDelayMs;
    }

    /**
     * Check if the strategy is enabled.
     * 
     * @return true if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }
}
