package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for coordination metrics.
 * 
 * <p>
 * This interface provides coordination-specific functionality including
 * coordination efficiency, task synchronization, resource coordination,
 * and coordination success metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface CoordinationMetrics {

    /**
     * Get the coordination efficiency score (0-100).
     * 
     * @return efficiency score
     */
    double coordinationEfficiency();

    /**
     * Get the task synchronization rate as a percentage.
     * 
     * @return synchronization rate between 0.0 and 100.0
     */
    double taskSynchronizationRate();

    /**
     * Get the resource coordination rate as a percentage.
     * 
     * @return coordination rate between 0.0 and 100.0
     */
    double resourceCoordinationRate();

    /**
     * Get the coordination success rate as a percentage.
     * 
     * @return success rate between 0.0 and 100.0
     */
    double coordinationSuccessRate();

    /**
     * Get the coordination latency in milliseconds.
     * 
     * @return coordination latency
     */
    double coordinationLatency();

    /**
     * Get the coordination throughput in coordinations per hour.
     * 
     * @return coordination throughput
     */
    double coordinationThroughput();

    /**
     * Get the coordination error rate as a percentage.
     * 
     * @return error rate between 0.0 and 100.0
     */
    double coordinationErrorRate();

    /**
     * Get the coordination confidence level (0-100).
     * 
     * @return confidence level
     */
    double coordinationConfidence();

    /**
     * Get the coordination coverage as a percentage.
     * 
     * @return coordination coverage between 0.0 and 100.0
     */
    double coordinationCoverage();

    /**
     * Get the coordination impact score (0-100).
     * 
     * @return impact score
     */
    double coordinationImpact();
}
