package org.openhab.core.ai.agent.execution;

import java.util.Map;
import org.eclipse.jdt.annotation.NonNullByDefault;

    public interface ExecutionRequest {
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