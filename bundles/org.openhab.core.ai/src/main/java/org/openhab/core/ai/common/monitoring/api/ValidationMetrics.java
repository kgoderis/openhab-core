package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for validation metrics.
 * 
 * <p>
 * This interface provides validation-specific functionality including
 * validation success rates, validation accuracy, validation throughput,
 * and validation efficiency metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ValidationMetrics {

    /**
     * Get the validation success rate as a percentage.
     * 
     * @return success rate between 0.0 and 100.0
     */
    double validationSuccessRate();

    /**
     * Get the validation accuracy as a percentage.
     * 
     * @return validation accuracy between 0.0 and 100.0
     */
    double validationAccuracy();

    /**
     * Get the validation throughput in validations per second.
     * 
     * @return validation throughput
     */
    double validationThroughput();

    /**
     * Get the validation efficiency score (0-100).
     * 
     * @return efficiency score
     */
    double validationEfficiency();

    /**
     * Get the validation error rate as a percentage.
     * 
     * @return error rate between 0.0 and 100.0
     */
    double validationErrorRate();

    /**
     * Get the validation latency in milliseconds.
     * 
     * @return validation latency
     */
    double validationLatency();

    /**
     * Get the validation coverage as a percentage.
     * 
     * @return validation coverage between 0.0 and 100.0
     */
    double validationCoverage();

    /**
     * Get the validation confidence level (0-100).
     * 
     * @return confidence level
     */
    double validationConfidence();

    /**
     * Get the validation rejection rate as a percentage.
     * 
     * @return rejection rate between 0.0 and 100.0
     */
    double validationRejectionRate();

    /**
     * Get the validation compliance rate as a percentage.
     * 
     * @return compliance rate between 0.0 and 100.0
     */
    double validationComplianceRate();
}
