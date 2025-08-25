package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for messaging metrics.
 * 
 * <p>
 * This interface provides messaging-specific functionality including
 * message delivery rates, messaging throughput, messaging latency,
 * and messaging reliability metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface MessagingMetrics {

    /**
     * Get the message delivery success rate as a percentage.
     * 
     * @return delivery success rate between 0.0 and 100.0
     */
    double messageDeliverySuccessRate();

    /**
     * Get the messaging throughput in messages per second.
     * 
     * @return messaging throughput
     */
    double messagingThroughput();

    /**
     * Get the messaging latency in milliseconds.
     * 
     * @return messaging latency
     */
    double messagingLatency();

    /**
     * Get the messaging reliability score (0-100).
     * 
     * @return reliability score
     */
    double messagingReliability();

    /**
     * Get the message queue depth.
     * 
     * @return queue depth
     */
    long messageQueueDepth();

    /**
     * Get the messaging error rate as a percentage.
     * 
     * @return error rate between 0.0 and 100.0
     */
    double messagingErrorRate();

    /**
     * Get the message acknowledgment rate as a percentage.
     * 
     * @return acknowledgment rate between 0.0 and 100.0
     */
    double messageAcknowledgmentRate();

    /**
     * Get the messaging retry rate as a percentage.
     * 
     * @return retry rate between 0.0 and 100.0
     */
    double messagingRetryRate();

    /**
     * Get the messaging timeout rate as a percentage.
     * 
     * @return timeout rate between 0.0 and 100.0
     */
    double messagingTimeoutRate();

    /**
     * Get the messaging confidence level (0-100).
     * 
     * @return confidence level
     */
    double messagingConfidence();
}
