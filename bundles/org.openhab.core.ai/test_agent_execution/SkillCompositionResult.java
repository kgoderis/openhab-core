package org.openhab.core.ai.agent.execution;

import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agents.SkillExecutionRequest;

    public interface SkillCompositionResult {
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