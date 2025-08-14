package org.openhab.core.ai.agent.execution;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Execution Strategy Interface
 * 
 * <p>
 * Defines the contract for execution strategies that determine how skills and actions
 * should be executed based on different criteria and requirements.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface ExecutionStrategy {

    /**
     * Execute the strategy
     * 
     * @param request the execution request
     * @return the execution result
     */
    ExecutionResult execute(ExecutionStrategyRequest request);

    /**
     * Get the strategy type
     * 
     * @return the strategy type
     */
    ExecutionStrategyType getStrategyType();

    /**
     * Validate if the strategy can handle the given request
     * 
     * @param request the request to validate
     * @return true if the strategy can handle the request
     */
    boolean canHandle(ExecutionStrategyRequest request);

    /**
     * Get the strategy description
     * 
     * @return the strategy description
     */
    String getDescription();

    /**
     * Get the strategy priority (higher values take precedence)
     * 
     * @return the strategy priority
     */
    int getPriority();

    /**
     * Execution Strategy Types
     */
    // Extracted: org.openhab.core.ai.agent.execution.ExecutionStrategyType

    /**
     * Execution Request
     */
    // Extracted: org.openhab.core.ai.agent.execution.ExecutionStrategyRequest

    /**
     * Execution Result
     */
    // Extracted: org.openhab.core.ai.agent.execution.ExecutionResult

    /**
     * Execution Priority
     */
    // Extracted: org.openhab.core.ai.agent.execution.ExecutionPriority

    /**
     * Execution Metric
     */
    // Extracted: org.openhab.core.ai.agent.execution.ExecutionMetric
}
