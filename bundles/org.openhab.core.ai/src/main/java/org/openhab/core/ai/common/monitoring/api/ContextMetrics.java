package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for context processing metrics.
 * 
 * <p>
 * This interface provides context processing-specific functionality including
 * context relevance scores, context processing efficiency, context accuracy,
 * and context adaptation metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ContextMetrics {

    /**
     * Get the context relevance score (0-100).
     * 
     * @return context relevance score
     */
    double contextRelevanceScore();

    /**
     * Get the context processing efficiency (0-100).
     * 
     * @return processing efficiency score
     */
    double contextProcessingEfficiency();

    /**
     * Get the context accuracy as a percentage.
     * 
     * @return context accuracy between 0.0 and 100.0
     */
    double contextAccuracy();

    /**
     * Get the context adaptation rate as a percentage.
     * 
     * @return adaptation rate between 0.0 and 100.0
     */
    double contextAdaptationRate();

    /**
     * Get the context processing latency in milliseconds.
     * 
     * @return processing latency
     */
    double contextProcessingLatency();

    /**
     * Get the context throughput in contexts per second.
     * 
     * @return context throughput
     */
    double contextThroughput();

    /**
     * Get the context error rate as a percentage.
     * 
     * @return error rate between 0.0 and 100.0
     */
    double contextErrorRate();

    /**
     * Get the context consistency rate as a percentage.
     * 
     * @return consistency rate between 0.0 and 100.0
     */
    double contextConsistencyRate();

    /**
     * Get the context completeness score (0-100).
     * 
     * @return completeness score
     */
    double contextCompleteness();

    /**
     * Get the context confidence level (0-100).
     * 
     * @return context confidence level
     */
    double contextConfidence();
}
