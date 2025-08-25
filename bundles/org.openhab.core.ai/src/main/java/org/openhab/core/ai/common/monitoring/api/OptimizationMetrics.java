package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for optimization metrics.
 * 
 * <p>
 * This interface provides optimization-specific functionality including
 * optimization efficiency, performance improvements, resource optimization,
 * and optimization success metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface OptimizationMetrics {

    /**
     * Get the optimization efficiency score (0-100).
     * 
     * @return efficiency score
     */
    double optimizationEfficiency();

    /**
     * Get the performance improvement rate as a percentage.
     * 
     * @return improvement rate between 0.0 and 100.0
     */
    double performanceImprovementRate();

    /**
     * Get the resource optimization rate as a percentage.
     * 
     * @return optimization rate between 0.0 and 100.0
     */
    double resourceOptimizationRate();

    /**
     * Get the optimization success rate as a percentage.
     * 
     * @return success rate between 0.0 and 100.0
     */
    double optimizationSuccessRate();

    /**
     * Get the optimization latency in milliseconds.
     * 
     * @return optimization latency
     */
    double optimizationLatency();

    /**
     * Get the optimization throughput in optimizations per hour.
     * 
     * @return optimization throughput
     */
    double optimizationThroughput();

    /**
     * Get the optimization error rate as a percentage.
     * 
     * @return error rate between 0.0 and 100.0
     */
    double optimizationErrorRate();

    /**
     * Get the optimization confidence level (0-100).
     * 
     * @return confidence level
     */
    double optimizationConfidence();

    /**
     * Get the optimization coverage as a percentage.
     * 
     * @return optimization coverage between 0.0 and 100.0
     */
    double optimizationCoverage();

    /**
     * Get the optimization impact score (0-100).
     * 
     * @return impact score
     */
    double optimizationImpact();
}
