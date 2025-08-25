package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for progress-specific metrics.
 * 
 * <p>
 * This interface provides progress-specific functionality including completion rates,
 * velocity, accuracy, efficiency, and throughput metrics.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface ProgressMetrics {

    /**
     * Get the completion rate as a percentage.
     * 
     * @return completion rate between 0.0 and 100.0
     */
    double completionRate();

    /**
     * Get the velocity in operations per hour.
     * 
     * @return velocity
     */
    double velocity();

    /**
     * Get the accuracy as a percentage.
     * 
     * @return accuracy between 0.0 and 100.0
     */
    double accuracy();

    /**
     * Get the efficiency as a percentage.
     * 
     * @return efficiency between 0.0 and 100.0
     */
    double efficiency();

    /**
     * Get the throughput in operations per hour.
     * 
     * @return throughput
     */
    double throughput();
}
