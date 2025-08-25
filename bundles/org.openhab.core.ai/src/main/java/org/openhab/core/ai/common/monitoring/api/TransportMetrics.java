package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for transport metrics.
 * 
 * <p>
 * This interface provides transport-specific functionality including
 * transport reliability, transport throughput, transport latency,
 * and transport efficiency metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface TransportMetrics {

    /**
     * Get the transport reliability score (0-100).
     * 
     * @return reliability score
     */
    double transportReliability();

    /**
     * Get the transport throughput in bytes per second.
     * 
     * @return transport throughput
     */
    double transportThroughput();

    /**
     * Get the transport latency in milliseconds.
     * 
     * @return transport latency
     */
    double transportLatency();

    /**
     * Get the transport efficiency score (0-100).
     * 
     * @return efficiency score
     */
    double transportEfficiency();

    /**
     * Get the transport error rate as a percentage.
     * 
     * @return error rate between 0.0 and 100.0
     */
    double transportErrorRate();

    /**
     * Get the transport connection success rate as a percentage.
     * 
     * @return connection success rate between 0.0 and 100.0
     */
    double connectionSuccessRate();

    /**
     * Get the transport packet loss rate as a percentage.
     * 
     * @return packet loss rate between 0.0 and 100.0
     */
    double packetLossRate();

    /**
     * Get the transport bandwidth utilization as a percentage.
     * 
     * @return bandwidth utilization between 0.0 and 100.0
     */
    double transportBandwidthUtilization();

    /**
     * Get the transport retry rate as a percentage.
     * 
     * @return retry rate between 0.0 and 100.0
     */
    double transportRetryRate();

    /**
     * Get the transport timeout rate as a percentage.
     * 
     * @return timeout rate between 0.0 and 100.0
     */
    double transportTimeoutRate();
}
