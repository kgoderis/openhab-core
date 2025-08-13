package org.openhab.core.ai.agent.execution;

import java.util.Map;
import org.eclipse.jdt.annotation.NonNullByDefault;

    public interface ExecutionMetric {
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