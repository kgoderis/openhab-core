package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for integration metrics.
 * 
 * <p>
 * This interface provides integration-specific functionality including
 * integration success rates, integration efficiency, integration reliability,
 * and integration performance metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface IntegrationMetrics {

    /**
     * Get the integration success rate as a percentage.
     * 
     * @return success rate between 0.0 and 100.0
     */
    double integrationSuccessRate();

    /**
     * Get the integration efficiency score (0-100).
     * 
     * @return efficiency score
     */
    double integrationEfficiency();

    /**
     * Get the integration reliability score (0-100).
     * 
     * @return reliability score
     */
    double integrationReliability();

    /**
     * Get the integration latency in milliseconds.
     * 
     * @return integration latency
     */
    double integrationLatency();

    /**
     * Get the integration throughput in operations per second.
     * 
     * @return integration throughput
     */
    double integrationThroughput();

    /**
     * Get the integration accuracy as a percentage.
     * 
     * @return integration accuracy between 0.0 and 100.0
     */
    double integrationAccuracy();

    /**
     * Get the integration consistency score (0-100).
     * 
     * @return consistency score
     */
    double integrationConsistency();

    /**
     * Get the integration completeness score (0-100).
     * 
     * @return completeness score
     */
    double integrationCompleteness();

    /**
     * Get the integration validity score (0-100).
     * 
     * @return validity score
     */
    double integrationValidity();

    /**
     * Get the integration optimization level (0-100).
     * 
     * @return optimization level
     */
    double integrationOptimization();
}
