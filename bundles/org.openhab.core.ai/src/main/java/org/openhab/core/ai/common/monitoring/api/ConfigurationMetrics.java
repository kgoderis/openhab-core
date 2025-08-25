package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for configuration metrics.
 * 
 * <p>
 * This interface provides configuration-specific functionality including
 * configuration validation rates, configuration change frequency,
 * configuration compliance, and configuration efficiency metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ConfigurationMetrics {

    /**
     * Get the configuration validation success rate as a percentage.
     * 
     * @return validation success rate between 0.0 and 100.0
     */
    double configurationValidationRate();

    /**
     * Get the configuration change frequency per hour.
     * 
     * @return change frequency
     */
    double configurationChangeFrequency();

    /**
     * Get the configuration compliance rate as a percentage.
     * 
     * @return compliance rate between 0.0 and 100.0
     */
    double configurationComplianceRate();

    /**
     * Get the configuration efficiency score (0-100).
     * 
     * @return configuration efficiency score
     */
    double configurationEfficiency();

    /**
     * Get the configuration error rate as a percentage.
     * 
     * @return error rate between 0.0 and 100.0
     */
    double configurationErrorRate();

    /**
     * Get the configuration update latency in milliseconds.
     * 
     * @return update latency
     */
    double configurationUpdateLatency();

    /**
     * Get the configuration consistency rate as a percentage.
     * 
     * @return consistency rate between 0.0 and 100.0
     */
    double configurationConsistencyRate();

    /**
     * Get the configuration backup success rate as a percentage.
     * 
     * @return backup success rate between 0.0 and 100.0
     */
    double configurationBackupRate();

    /**
     * Get the configuration restore success rate as a percentage.
     * 
     * @return restore success rate between 0.0 and 100.0
     */
    double configurationRestoreRate();

    /**
     * Get the configuration versioning accuracy (0-100).
     * 
     * @return versioning accuracy score
     */
    double configurationVersioningAccuracy();
}
