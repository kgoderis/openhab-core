package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for business intelligence metrics.
 * 
 * <p>
 * This interface provides functionality for calculating business-relevant metrics
 * including cost efficiency, resource utilization, and throughput efficiency.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface BusinessMetrics {

    /**
     * Calculate cost efficiency.
     * 
     * @return cost efficiency ratio
     */
    double costEfficiency();

    /**
     * Calculate resource utilization.
     * 
     * @return resource utilization percentage
     */
    double resourceUtilization();

    /**
     * Calculate throughput efficiency.
     * 
     * @return throughput efficiency ratio
     */
    double throughputEfficiency();
}
