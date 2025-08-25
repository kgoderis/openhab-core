package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for trend analysis metrics.
 * 
 * <p>
 * This interface provides functionality for analyzing trends in metrics data
 * including trend percentage, direction, and change rate calculations.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface TrendMetrics {

    /**
     * Calculate trend percentage.
     * 
     * @return trend percentage (positive for increasing, negative for decreasing)
     */
    double trendPercentage();

    /**
     * Get trend direction.
     * 
     * @return trend direction ("increasing", "decreasing", or "stable")
     */
    String trendDirection();

    /**
     * Calculate change rate.
     * 
     * @return change rate per time unit
     */
    double changeRate();
}
