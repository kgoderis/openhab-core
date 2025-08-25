package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for percentile analysis metrics.
 * 
 * <p>
 * This interface provides functionality for calculating percentile values
 * from metrics data including median, 90th, 95th, and 99th percentiles.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface PercentileMetrics {

    /**
     * Calculate 50th percentile (median).
     * 
     * @return 50th percentile value
     */
    double percentile50();

    /**
     * Calculate 90th percentile.
     * 
     * @return 90th percentile value
     */
    double percentile90();

    /**
     * Calculate 95th percentile.
     * 
     * @return 95th percentile value
     */
    double percentile95();

    /**
     * Calculate 99th percentile.
     * 
     * @return 99th percentile value
     */
    double percentile99();
}
