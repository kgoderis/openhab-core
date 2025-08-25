package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for tool-specific metrics.
 * 
 * <p>
 * This interface provides functionality for measuring and reporting tool-specific
 * metrics including throughput, resource usage, and concurrent execution limits.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ToolMetrics {

    /**
     * Calculate throughput per second.
     * 
     * @return throughput per second, or 0.0 if no operations
     */
    double throughputPerSecond();

    /**
     * Calculate average resource usage.
     * 
     * @return average resource usage, or 0.0 if no operations
     */
    double averageResourceUsage();

    /**
     * Get maximum concurrent executions.
     * 
     * @return maximum concurrent executions
     */
    int maxConcurrentExecutions();
}
