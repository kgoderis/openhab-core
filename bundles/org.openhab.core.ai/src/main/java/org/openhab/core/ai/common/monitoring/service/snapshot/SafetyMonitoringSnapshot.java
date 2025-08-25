package org.openhab.core.ai.common.monitoring.service.snapshot;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.ai.common.monitoring.api.CountsMetrics;
import org.openhab.core.ai.common.monitoring.api.LatencyMetrics;
import org.openhab.core.ai.common.monitoring.api.SafetyMetrics;
import org.openhab.core.ai.common.monitoring.service.statistics.StatisticsSnapshot;

/**
 * Snapshot class for safety monitoring metrics.
 * 
 * <p>
 * This class provides safety monitoring metrics including safety violation rates,
 * constraint compliance, risk assessment scores, and safety incident metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public record SafetyMonitoringSnapshot(long total, long success, long failure, long totalDurationNanos,
        double safetyViolationRate, double constraintComplianceRate, double riskAssessmentScore,
        double safetyIncidentRate, double safetyMonitoringCoverage, double safetyResponseTime,
        double safetyAlertFrequency, double safetySystemAvailability, double thresholdComplianceRate,
        double safetyConfidenceLevel,
        long timestampMs) implements StatisticsSnapshot, CountsMetrics, LatencyMetrics, SafetyMetrics {

    /**
     * Create a safety monitoring snapshot.
     * 
     * @param total total safety checks
     * @param success successful safety checks
     * @param failure failed safety checks
     * @param totalDurationNanos total duration in nanoseconds
     * @param safetyViolationRate safety violation rate percentage
     * @param constraintComplianceRate constraint compliance rate percentage
     * @param riskAssessmentScore risk assessment score
     * @param safetyIncidentRate safety incident rate per hour
     * @param safetyMonitoringCoverage safety monitoring coverage percentage
     * @param safetyResponseTime safety response time in milliseconds
     * @param safetyAlertFrequency safety alert frequency per hour
     * @param safetySystemAvailability safety system availability percentage
     * @param thresholdComplianceRate threshold compliance rate percentage
     * @param safetyConfidenceLevel safety confidence level score
     * @param timestampMs timestamp in milliseconds
     */
    public SafetyMonitoringSnapshot {
        // Validation
        if (total < 0) {
            throw new IllegalArgumentException("total must be non-negative");
        }
        if (success < 0) {
            throw new IllegalArgumentException("success must be non-negative");
        }
        if (failure < 0) {
            throw new IllegalArgumentException("failure must be non-negative");
        }
        if (totalDurationNanos < 0) {
            throw new IllegalArgumentException("totalDurationNanos must be non-negative");
        }
        if (safetyViolationRate < 0.0 || safetyViolationRate > 100.0) {
            throw new IllegalArgumentException("safetyViolationRate must be between 0.0 and 100.0");
        }
        if (constraintComplianceRate < 0.0 || constraintComplianceRate > 100.0) {
            throw new IllegalArgumentException("constraintComplianceRate must be between 0.0 and 100.0");
        }
        if (riskAssessmentScore < 0.0 || riskAssessmentScore > 100.0) {
            throw new IllegalArgumentException("riskAssessmentScore must be between 0.0 and 100.0");
        }
        if (safetyIncidentRate < 0.0) {
            throw new IllegalArgumentException("safetyIncidentRate must be non-negative");
        }
        if (safetyMonitoringCoverage < 0.0 || safetyMonitoringCoverage > 100.0) {
            throw new IllegalArgumentException("safetyMonitoringCoverage must be between 0.0 and 100.0");
        }
        if (safetyResponseTime < 0.0) {
            throw new IllegalArgumentException("safetyResponseTime must be non-negative");
        }
        if (safetyAlertFrequency < 0.0) {
            throw new IllegalArgumentException("safetyAlertFrequency must be non-negative");
        }
        if (safetySystemAvailability < 0.0 || safetySystemAvailability > 100.0) {
            throw new IllegalArgumentException("safetySystemAvailability must be between 0.0 and 100.0");
        }
        if (thresholdComplianceRate < 0.0 || thresholdComplianceRate > 100.0) {
            throw new IllegalArgumentException("thresholdComplianceRate must be between 0.0 and 100.0");
        }
        if (safetyConfidenceLevel < 0.0 || safetyConfidenceLevel > 100.0) {
            throw new IllegalArgumentException("safetyConfidenceLevel must be between 0.0 and 100.0");
        }
        if (timestampMs < 0) {
            throw new IllegalArgumentException("timestampMs must be non-negative");
        }
    }

    /**
     * Create a safety monitoring snapshot from basic metrics.
     * 
     * @param total total safety checks
     * @param success successful safety checks
     * @param failure failed safety checks
     * @param totalDurationNanos total duration in nanoseconds
     * @param safetyViolationRate safety violation rate percentage
     * @param constraintComplianceRate constraint compliance rate percentage
     * @param riskAssessmentScore risk assessment score
     * @param safetyIncidentRate safety incident rate per hour
     * @param safetyMonitoringCoverage safety monitoring coverage percentage
     * @param safetyResponseTime safety response time in milliseconds
     * @param safetyAlertFrequency safety alert frequency per hour
     * @param safetySystemAvailability safety system availability percentage
     * @param thresholdComplianceRate threshold compliance rate percentage
     * @param safetyConfidenceLevel safety confidence level score
     * @return safety monitoring snapshot
     */
    public static SafetyMonitoringSnapshot of(long total, long success, long failure, long totalDurationNanos,
            double safetyViolationRate, double constraintComplianceRate, double riskAssessmentScore,
            double safetyIncidentRate, double safetyMonitoringCoverage, double safetyResponseTime,
            double safetyAlertFrequency, double safetySystemAvailability, double thresholdComplianceRate,
            double safetyConfidenceLevel) {
        return new SafetyMonitoringSnapshot(total, success, failure, totalDurationNanos, safetyViolationRate,
                constraintComplianceRate, riskAssessmentScore, safetyIncidentRate, safetyMonitoringCoverage,
                safetyResponseTime, safetyAlertFrequency, safetySystemAvailability, thresholdComplianceRate,
                safetyConfidenceLevel, System.currentTimeMillis());
    }

    /**
     * Create an empty safety monitoring snapshot.
     * 
     * @return empty safety monitoring snapshot
     */
    public static SafetyMonitoringSnapshot empty() {
        return new SafetyMonitoringSnapshot(0, 0, 0, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                System.currentTimeMillis());
    }

    // CountsMetrics implementation
    @Override
    public long total() {
        return total;
    }

    @Override
    public long success() {
        return success;
    }

    @Override
    public long failure() {
        return failure;
    }

    // LatencyMetrics implementation
    @Override
    public long totalDurationNanos() {
        return totalDurationNanos;
    }

    // SafetyMetrics implementation
    @Override
    public double safetyViolationRate() {
        return safetyViolationRate;
    }

    @Override
    public double constraintComplianceRate() {
        return constraintComplianceRate;
    }

    @Override
    public double riskAssessmentScore() {
        return riskAssessmentScore;
    }

    @Override
    public double safetyIncidentRate() {
        return safetyIncidentRate;
    }

    @Override
    public double safetyMonitoringCoverage() {
        return safetyMonitoringCoverage;
    }

    @Override
    public double safetyResponseTime() {
        return safetyResponseTime;
    }

    @Override
    public double safetyAlertFrequency() {
        return safetyAlertFrequency;
    }

    @Override
    public double safetySystemAvailability() {
        return safetySystemAvailability;
    }

    @Override
    public double thresholdComplianceRate() {
        return thresholdComplianceRate;
    }

    @Override
    public double safetyConfidenceLevel() {
        return safetyConfidenceLevel;
    }
}
