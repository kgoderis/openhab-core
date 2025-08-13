package org.openhab.core.ai.agent.execution;

import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.agents.SkillExecutionRequest;

    public interface SkillExecutionStep {
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