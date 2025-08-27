package org.openhab.core.ai.common.monitoring.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Capability interface for filter metrics.
 * 
 * <p>
 * This interface provides filter-specific functionality including
 * filtering efficiency, blocking rates, quarantine rates, and rule utilization.
 * </p>
 * 
 * @author Karel Goderis - Initial Contribution
 * @since 1.0.0
 */
@NonNullByDefault
public interface FilterMetrics {

    /**
     * Get the total number of items filtered.
     * 
     * @return total filtered items
     */
    long totalFiltered();

    /**
     * Get the total number of items allowed.
     * 
     * @return total allowed items
     */
    long totalAllowed();

    /**
     * Get the total number of items blocked.
     * 
     * @return total blocked items
     */
    long totalBlocked();

    /**
     * Get the total number of items quarantined.
     * 
     * @return total quarantined items
     */
    long totalQuarantined();

    /**
     * Get the total number of filter rules.
     * 
     * @return total rules
     */
    long totalRules();

    /**
     * Get the filtering efficiency as a percentage.
     * 
     * @return filtering efficiency between 0.0 and 100.0
     */
    double filteringEfficiency();

    /**
     * Get the blocking rate as a percentage.
     * 
     * @return blocking rate between 0.0 and 100.0
     */
    double blockingRate();

    /**
     * Get the quarantine rate as a percentage.
     * 
     * @return quarantine rate between 0.0 and 100.0
     */
    double quarantineRate();

    /**
     * Get the rule utilization as a percentage.
     * 
     * @return rule utilization between 0.0 and 100.0
     */
    double ruleUtilization();
}
