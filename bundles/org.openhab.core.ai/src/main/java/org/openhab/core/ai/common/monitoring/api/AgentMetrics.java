package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for agent-specific metrics.
 * 
 * <p>
 * This interface provides functionality for measuring and reporting agent-specific
 * metrics including decision accuracy, learning rate, and success rate.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface AgentMetrics {

    /**
     * Get decision accuracy.
     * 
     * @return decision accuracy between 0.0 and 1.0
     */
    double decisionAccuracy();

    /**
     * Get learning rate.
     * 
     * @return learning rate between 0.0 and 1.0
     */
    double learningRate();

    /**
     * Get success rate.
     * 
     * @return success rate between 0.0 and 1.0
     */
    double successRate();
}
