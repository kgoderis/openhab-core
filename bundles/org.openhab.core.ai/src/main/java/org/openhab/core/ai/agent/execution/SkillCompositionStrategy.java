package org.openhab.core.ai.agent.execution;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agents.SkillExecutionRequest;

/**
 * Skill Composition Strategy Interface
 * 
 * <p>
 * Defines the contract for skill composition strategies that determine how multiple skills
 * should be combined and executed to achieve complex behaviors.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 */
@NonNullByDefault
public interface SkillCompositionStrategy {

    /**
     * Compose skills according to the strategy
     * 
     * @param skills the list of skills to compose
     * @param context the execution context
     * @return the composition result
     */
    SkillCompositionResult compose(List<SkillExecutionRequest> skills, Map<String, Object> context);

    /**
     * Get the strategy type
     * 
     * @return the strategy type
     */
    String getStrategyType();

    /**
     * Validate if the strategy can handle the given skills
     * 
     * @param skills the skills to validate
     * @return true if the strategy can handle the skills
     */
    boolean canHandle(List<SkillExecutionRequest> skills);

    /**
     * Get the strategy description
     * 
     * @return the strategy description
     */
    String getDescription();

    /**
     * Skill Composition Result
     */
    interface SkillCompositionResult {
        /**
         * Check if the composition was successful
         * 
         * @return true if successful
         */
        boolean isSuccess();

        /**
         * Get the execution plan
         * 
         * @return the execution plan
         */
        List<SkillExecutionStep> getExecutionPlan();

        /**
         * Get the error message if failed
         * 
         * @return the error message
         */
        String getErrorMessage();

        /**
         * Get the estimated execution time
         * 
         * @return the estimated execution time in milliseconds
         */
        long getEstimatedExecutionTime();
    }

    /**
     * Skill Execution Step
     */
    interface SkillExecutionStep {
        /**
         * Get the skill name
         * 
         * @return the skill name
         */
        String getSkillName();

        /**
         * Get the parameters
         * 
         * @return the parameters
         */
        Map<String, Object> getParameters();

        /**
         * Get the dependencies
         * 
         * @return the dependencies
         */
        List<String> getDependencies();

        /**
         * Get the step order
         * 
         * @return the step order
         */
        int getOrder();

        /**
         * Check if the step is required
         * 
         * @return true if required
         */
        boolean isRequired();
    }
}
