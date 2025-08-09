package org.openhab.core.ai.agent.execution;

import java.util.Map;

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
    ExecutionResult execute(ExecutionRequest request);

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
    boolean canHandle(ExecutionRequest request);

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
    enum ExecutionStrategyType {
        SKILL, // Execute as skill via AgentSkillManager
        ACTION, // Execute as action directly
        COMPOSED // Execute as composed action/skill
    }

    /**
     * Execution Request
     */
    interface ExecutionRequest {
        /**
         * Get the execution type
         * 
         * @return the execution type
         */
        ExecutionStrategyType getType();

        /**
         * Get the target name (skill name or action name)
         * 
         * @return the target name
         */
        String getTargetName();

        /**
         * Get the parameters
         * 
         * @return the parameters
         */
        Map<String, Object> getParameters();

        /**
         * Get the execution priority
         * 
         * @return the execution priority
         */
        ExecutionPriority getPriority();

        /**
         * Get the execution context
         * 
         * @return the execution context
         */
        Map<String, Object> getContext();

        /**
         * Check if the request requires validation
         * 
         * @return true if validation is required
         */
        boolean requiresValidation();

        /**
         * Check if the request requires safety checks
         * 
         * @return true if safety checks are required
         */
        boolean requiresSafetyChecks();
    }

    /**
     * Execution Result
     */
    interface ExecutionResult {
        /**
         * Check if the execution was successful
         * 
         * @return true if successful
         */
        boolean isSuccess();

        /**
         * Get the execution data
         * 
         * @return the execution data
         */
        Object getData();

        /**
         * Get the error message if failed
         * 
         * @return the error message
         */
        String getErrorMessage();

        /**
         * Get the execution time in milliseconds
         * 
         * @return the execution time
         */
        long getExecutionTime();

        /**
         * Get the execution metrics
         * 
         * @return the execution metrics
         */
        ExecutionMetric getMetrics();
    }

    /**
     * Execution Priority
     */
    enum ExecutionPriority {
        LOW(1),
        MEDIUM(2),
        HIGH(3),
        CRITICAL(4);

        private final int value;

        ExecutionPriority(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     * Execution Metric
     */
    interface ExecutionMetric {
        /**
         * Get the strategy type used
         * 
         * @return the strategy type
         */
        ExecutionStrategyType getStrategyType();

        /**
         * Get the execution duration
         * 
         * @return the execution duration in milliseconds
         */
        long getDuration();

        /**
         * Get the memory usage
         * 
         * @return the memory usage in bytes
         */
        long getMemoryUsage();

        /**
         * Get the CPU usage
         * 
         * @return the CPU usage percentage
         */
        double getCpuUsage();

        /**
         * Get the success rate
         * 
         * @return the success rate (0.0 to 1.0)
         */
        double getSuccessRate();
    }
}
