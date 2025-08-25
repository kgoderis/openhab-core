package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for safety monitoring metrics.
 * 
 * <p>
 * This interface provides safety monitoring-specific functionality including
 * safety violation rates, constraint compliance, risk assessment scores,
 * and safety incident metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface SafetyMetrics {

    /**
     * Get the safety violation rate as a percentage.
     * 
     * @return violation rate between 0.0 and 100.0
     */
    double safetyViolationRate();

    /**
     * Get the constraint compliance rate as a percentage.
     * 
     * @return compliance rate between 0.0 and 100.0
     */
    double constraintComplianceRate();

    /**
     * Get the risk assessment score (0-100).
     * 
     * @return risk assessment score
     */
    double riskAssessmentScore();

    /**
     * Get the safety incident rate per hour.
     * 
     * @return incident rate
     */
    double safetyIncidentRate();

    /**
     * Get the safety monitoring coverage (0-100).
     * 
     * @return monitoring coverage percentage
     */
    double safetyMonitoringCoverage();

    /**
     * Get the safety response time in milliseconds.
     * 
     * @return response time
     */
    double safetyResponseTime();

    /**
     * Get the safety alert frequency per hour.
     * 
     * @return alert frequency
     */
    double safetyAlertFrequency();

    /**
     * Get the safety system availability as a percentage.
     * 
     * @return system availability between 0.0 and 100.0
     */
    double safetySystemAvailability();

    /**
     * Get the safety threshold compliance rate as a percentage.
     * 
     * @return threshold compliance between 0.0 and 100.0
     */
    double thresholdComplianceRate();

    /**
     * Get the safety confidence level (0-100).
     * 
     * @return safety confidence level
     */
    double safetyConfidenceLevel();
}
