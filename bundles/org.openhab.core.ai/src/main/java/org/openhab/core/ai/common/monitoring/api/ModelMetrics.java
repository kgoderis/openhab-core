package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for model-specific metrics.
 * 
 * <p>
 * This interface provides functionality for measuring and reporting model-specific
 * metrics including token usage, cost analysis, and throughput calculations.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ModelMetrics {

    /**
     * Calculate tokens processed per second.
     * 
     * @return tokens per second, or 0.0 if no operations
     */
    double tokensPerSecond();

    /**
     * Calculate cost per request.
     * 
     * @return cost per request, or 0.0 if no operations
     */
    double costPerRequest();

    /**
     * Calculate average tokens per request.
     * 
     * @return average tokens per request, or 0.0 if no operations
     */
    double averageTokensPerRequest();
}
