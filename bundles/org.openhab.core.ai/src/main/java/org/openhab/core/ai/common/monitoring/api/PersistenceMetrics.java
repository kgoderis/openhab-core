package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for persistence-specific metrics.
 * 
 * <p>
 * This interface provides persistence-specific functionality including data integrity,
 * storage efficiency, recovery success rates, and backup frequency metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface PersistenceMetrics {

    /**
     * Get the data integrity rate as a percentage.
     * 
     * @return data integrity rate between 0.0 and 100.0
     */
    double dataIntegrityRate();

    /**
     * Get the storage efficiency as a percentage.
     * 
     * @return storage efficiency between 0.0 and 100.0
     */
    double storageEfficiency();

    /**
     * Get the recovery success rate as a percentage.
     * 
     * @return recovery success rate between 0.0 and 100.0
     */
    double recoverySuccessRate();

    /**
     * Get the backup frequency in operations per hour.
     * 
     * @return backup frequency
     */
    double backupFrequency();

    /**
     * Get the data retention efficiency as a percentage.
     * 
     * @return data retention efficiency between 0.0 and 100.0
     */
    double dataRetentionEfficiency();
}
