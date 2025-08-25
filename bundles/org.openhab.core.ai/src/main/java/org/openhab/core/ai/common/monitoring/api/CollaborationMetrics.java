package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for collaboration metrics.
 * 
 * <p>
 * This interface provides collaboration-specific functionality including
 * collaboration efficiency, team coordination, shared resource utilization,
 * and collaboration success metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface CollaborationMetrics {

    /**
     * Get the collaboration efficiency score (0-100).
     * 
     * @return efficiency score
     */
    double collaborationEfficiency();

    /**
     * Get the team coordination rate as a percentage.
     * 
     * @return coordination rate between 0.0 and 100.0
     */
    double teamCoordinationRate();

    /**
     * Get the shared resource utilization as a percentage.
     * 
     * @return resource utilization between 0.0 and 100.0
     */
    double sharedResourceUtilization();

    /**
     * Get the collaboration success rate as a percentage.
     * 
     * @return success rate between 0.0 and 100.0
     */
    double collaborationSuccessRate();

    /**
     * Get the collaboration latency in milliseconds.
     * 
     * @return collaboration latency
     */
    double collaborationLatency();

    /**
     * Get the collaboration throughput in collaborations per hour.
     * 
     * @return collaboration throughput
     */
    double collaborationThroughput();

    /**
     * Get the collaboration error rate as a percentage.
     * 
     * @return error rate between 0.0 and 100.0
     */
    double collaborationErrorRate();

    /**
     * Get the collaboration confidence level (0-100).
     * 
     * @return confidence level
     */
    double collaborationConfidence();

    /**
     * Get the collaboration coverage as a percentage.
     * 
     * @return collaboration coverage between 0.0 and 100.0
     */
    double collaborationCoverage();

    /**
     * Get the collaboration impact score (0-100).
     * 
     * @return impact score
     */
    double collaborationImpact();
}
