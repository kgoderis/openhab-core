package org.openhab.core.ai.agent.execution;

import java.util.Map;
import org.eclipse.jdt.annotation.NonNullByDefault;

    public interface ExecutionResult {
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