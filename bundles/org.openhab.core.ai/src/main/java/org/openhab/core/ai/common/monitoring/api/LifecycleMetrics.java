package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for lifecycle-specific metrics.
 * 
 * <p>
 * This interface provides lifecycle-specific functionality including startup times,
 * shutdown times, uptime, restart frequency, and health scores.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface LifecycleMetrics {

    /**
     * Get the average startup time in milliseconds.
     * 
     * @return startup time in milliseconds
     */
    double startupTime();

    /**
     * Get the average shutdown time in milliseconds.
     * 
     * @return shutdown time in milliseconds
     */
    double shutdownTime();

    /**
     * Get the total uptime in hours.
     * 
     * @return uptime in hours
     */
    double uptime();

    /**
     * Get the restart frequency per hour.
     * 
     * @return restart frequency
     */
    double restartFrequency();

    /**
     * Get the health score as a percentage.
     * 
     * @return health score between 0.0 and 100.0
     */
    double healthScore();
}
