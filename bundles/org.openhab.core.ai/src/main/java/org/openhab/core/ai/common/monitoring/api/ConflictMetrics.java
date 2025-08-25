package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for conflict resolution metrics.
 * 
 * <p>
 * This interface provides conflict resolution-specific functionality including
 * conflict detection rates, resolution success rates, conflict complexity,
 * and resolution efficiency metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ConflictMetrics {

    /**
     * Get the conflict detection rate as a percentage.
     * 
     * @return detection rate between 0.0 and 100.0
     */
    double conflictDetectionRate();

    /**
     * Get the conflict resolution success rate as a percentage.
     * 
     * @return resolution success rate between 0.0 and 100.0
     */
    double conflictResolutionSuccessRate();

    /**
     * Get the conflict complexity score (0-100).
     * 
     * @return complexity score
     */
    double conflictComplexity();

    /**
     * Get the conflict resolution efficiency (0-100).
     * 
     * @return resolution efficiency score
     */
    double conflictResolutionEfficiency();

    /**
     * Get the conflict resolution latency in milliseconds.
     * 
     * @return resolution latency
     */
    double conflictResolutionLatency();

    /**
     * Get the conflict frequency per hour.
     * 
     * @return conflict frequency
     */
    double conflictFrequency();

    /**
     * Get the conflict escalation rate as a percentage.
     * 
     * @return escalation rate between 0.0 and 100.0
     */
    double conflictEscalationRate();

    /**
     * Get the conflict resolution confidence level (0-100).
     * 
     * @return confidence level
     */
    double conflictResolutionConfidence();

    /**
     * Get the conflict prevention rate as a percentage.
     * 
     * @return prevention rate between 0.0 and 100.0
     */
    double conflictPreventionRate();

    /**
     * Get the conflict impact score (0-100).
     * 
     * @return impact score
     */
    double conflictImpact();
}
