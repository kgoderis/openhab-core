package org.openhab.core.ai.tool.error.recovery;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.error.ErrorRecoveryResult;

/**
 * Strategy for recovering from tool errors.
 * 
 * This interface defines the contract for error recovery strategies that can
 * handle and recover from various types of tool errors.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ErrorRecoveryStrategy {

    /**
     * Get the strategy ID.
     * 
     * @return the strategy ID
     */
    String getStrategyId();

    /**
     * Get the strategy name.
     * 
     * @return the strategy name
     */
    String getStrategyName();

    /**
     * Get the strategy description.
     * 
     * @return the strategy description
     */
    String getStrategyDescription();

    /**
     * Get the error types this strategy can handle.
     * 
     * @return list of supported error types
     */
    String[] getSupportedErrorTypes();

    /**
     * Get the strategy priority.
     * 
     * @return the strategy priority (higher values = higher priority)
     */
    int getPriority();

    /**
     * Check if the strategy is enabled.
     * 
     * @return true if the strategy is enabled
     */
    boolean isEnabled();

    /**
     * Check if this strategy can handle the given error.
     * 
     * @param error the error to check
     * @return true if the strategy can handle the error
     */
    boolean canHandle(Throwable error);

    /**
     * Attempt to recover from the given error.
     * 
     * @param error the error to recover from
     * @return recovery result
     */
    ErrorRecoveryResult recover(Throwable error);

    /**
     * Get the strategy configuration.
     * 
     * @return the strategy configuration
     */
    Map<String, Object> getConfiguration();

    /**
     * Update the strategy configuration.
     * 
     * @param configuration the new configuration
     */
    void updateConfiguration(Map<String, Object> configuration);

    // TODO: Implement error recovery logic
    // TODO: Add support for error recovery chaining
    // TODO: Implement error recovery performance monitoring
    // TODO: Add support for error recovery versioning
}
