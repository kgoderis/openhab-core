package org.openhab.core.ai.reasoning.engine.api;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Represents a planned step in a reasoning process.
 * 
 * This interface is used by the ReasoningOrchestrationService to plan and execute
 * multi-step reasoning processes with dependencies and coordination.
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 4.0.0
 */
@NonNullByDefault
public interface ReasoningPlanStep {

    /**
     * Gets the unique identifier for this reasoning step.
     * 
     * @return The step identifier
     */
    String getId();

    /**
     * Gets the prompt or instruction for this reasoning step.
     * 
     * @return The step prompt
     */
    String getPrompt();

    /**
     * Gets the list of step IDs that this step depends on.
     * 
     * @return List of dependency step IDs
     */
    List<String> getDependencies();

    /**
     * Gets the priority level for this step (lower numbers = higher priority).
     * 
     * @return The priority level
     */
    default int getPriority() {
        return 0;
    }

    /**
     * Gets the estimated execution time in milliseconds.
     * 
     * @return Estimated execution time
     */
    default long getEstimatedExecutionTime() {
        return 1000; // Default 1 second
    }

    /**
     * Gets the maximum number of retries for this step.
     * 
     * @return Maximum retry count
     */
    default int getMaxRetries() {
        return 3;
    }

    /**
     * Gets the timeout for this step in milliseconds.
     * 
     * @return Timeout in milliseconds
     */
    default long getTimeout() {
        return 30000; // Default 30 seconds
    }

    /**
     * Gets additional metadata for this step.
     * 
     * @return Step metadata
     */
    default java.util.Map<String, Object> getMetadata() {
        return java.util.Map.of();
    }
}
