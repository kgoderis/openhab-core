package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for communication metrics.
 * 
 * <p>
 * This interface provides communication-specific functionality including
 * communication success rates, message throughput, communication latency,
 * and communication reliability metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface CommunicationMetrics {

    /**
     * Get the communication success rate as a percentage.
     * 
     * @return success rate between 0.0 and 100.0
     */
    double communicationSuccessRate();

    /**
     * Get the message throughput in messages per second.
     * 
     * @return message throughput
     */
    double messageThroughput();

    /**
     * Get the communication latency in milliseconds.
     * 
     * @return communication latency
     */
    double communicationLatency();

    /**
     * Get the communication reliability score (0-100).
     * 
     * @return reliability score
     */
    double communicationReliability();

    /**
     * Get the message delivery rate as a percentage.
     * 
     * @return delivery rate between 0.0 and 100.0
     */
    double messageDeliveryRate();

    /**
     * Get the communication error rate as a percentage.
     * 
     * @return error rate between 0.0 and 100.0
     */
    double communicationErrorRate();

    /**
     * Get the message queue depth.
     * 
     * @return queue depth
     */
    long messageQueueDepth();

    /**
     * Get the communication bandwidth utilization as a percentage.
     * 
     * @return bandwidth utilization between 0.0 and 100.0
     */
    double bandwidthUtilization();

    /**
     * Get the communication retry rate as a percentage.
     * 
     * @return retry rate between 0.0 and 100.0
     */
    double communicationRetryRate();

    /**
     * Get the communication timeout rate as a percentage.
     * 
     * @return timeout rate between 0.0 and 100.0
     */
    double communicationTimeoutRate();
}
