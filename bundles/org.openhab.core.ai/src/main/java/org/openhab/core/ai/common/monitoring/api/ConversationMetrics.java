package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for conversation metrics.
 * 
 * <p>
 * This interface provides conversation-specific functionality including
 * conversation quality, response accuracy, conversation flow, and
 * conversation satisfaction metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ConversationMetrics {

    /**
     * Get the conversation quality score (0-100).
     * 
     * @return quality score
     */
    double conversationQuality();

    /**
     * Get the response accuracy as a percentage.
     * 
     * @return response accuracy between 0.0 and 100.0
     */
    double responseAccuracy();

    /**
     * Get the conversation flow rate as a percentage.
     * 
     * @return flow rate between 0.0 and 100.0
     */
    double conversationFlowRate();

    /**
     * Get the conversation satisfaction score (0-100).
     * 
     * @return satisfaction score
     */
    double conversationSatisfaction();

    /**
     * Get the conversation latency in milliseconds.
     * 
     * @return conversation latency
     */
    double conversationLatency();

    /**
     * Get the conversation throughput in messages per minute.
     * 
     * @return conversation throughput
     */
    double conversationThroughput();

    /**
     * Get the conversation error rate as a percentage.
     * 
     * @return error rate between 0.0 and 100.0
     */
    double conversationErrorRate();

    /**
     * Get the conversation confidence level (0-100).
     * 
     * @return confidence level
     */
    double conversationConfidence();

    /**
     * Get the conversation coverage as a percentage.
     * 
     * @return conversation coverage between 0.0 and 100.0
     */
    double conversationCoverage();

    /**
     * Get the conversation impact score (0-100).
     * 
     * @return impact score
     */
    double conversationImpact();
}
