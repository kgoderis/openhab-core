package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for efficiency analysis metrics.
 * 
 * <p>
 * This interface provides functionality for calculating efficiency metrics
 * including resource, time, and energy efficiency.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface EfficiencyMetrics {

    /**
     * Calculate resource efficiency.
     * 
     * @return resource efficiency ratio
     */
    double resourceEfficiency();

    /**
     * Calculate time efficiency.
     * 
     * @return time efficiency ratio
     */
    double timeEfficiency();

    /**
     * Calculate energy efficiency.
     * 
     * @return energy efficiency ratio
     */
    double energyEfficiency();
}
