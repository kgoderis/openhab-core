package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for reasoning execution metrics.
 * 
 * <p>
 * This interface provides reasoning execution-specific functionality including
 * reasoning accuracy, reasoning latency, reasoning complexity, and reasoning
 * efficiency metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ReasoningMetrics {

    /**
     * Get the reasoning accuracy as a percentage.
     * 
     * @return reasoning accuracy between 0.0 and 100.0
     */
    double reasoningAccuracy();

    /**
     * Get the reasoning latency in milliseconds.
     * 
     * @return reasoning latency
     */
    double reasoningLatency();

    /**
     * Get the reasoning complexity score (0-100).
     * 
     * @return complexity score
     */
    double reasoningComplexity();

    /**
     * Get the reasoning efficiency score (0-100).
     * 
     * @return efficiency score
     */
    double reasoningEfficiency();

    /**
     * Get the reasoning throughput in operations per second.
     * 
     * @return reasoning throughput
     */
    double reasoningThroughput();

    /**
     * Get the reasoning error rate as a percentage.
     * 
     * @return error rate between 0.0 and 100.0
     */
    double reasoningErrorRate();

    /**
     * Get the reasoning confidence level (0-100).
     * 
     * @return confidence level
     */
    double reasoningConfidence();

    /**
     * Get the reasoning step count.
     * 
     * @return step count
     */
    long reasoningStepCount();

    /**
     * Get the reasoning backtracking rate as a percentage.
     * 
     * @return backtracking rate between 0.0 and 100.0
     */
    double reasoningBacktrackingRate();

    /**
     * Get the reasoning optimization level (0-100).
     * 
     * @return optimization level
     */
    double reasoningOptimizationLevel();
}
